package dev.hrutvik.fraud.review;

import dev.hrutvik.fraud.decision.TransactionDecision;
import dev.hrutvik.fraud.outbox.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class ReviewService {
    private final ReviewCaseRepository cases; private final ReviewAuditEventRepository audit; private final OutboxEventRepository outbox; private final Clock clock;
    public ReviewService(ReviewCaseRepository cases,ReviewAuditEventRepository audit,OutboxEventRepository outbox){this(cases,audit,outbox,Clock.systemUTC());}
    ReviewService(ReviewCaseRepository cases,ReviewAuditEventRepository audit,OutboxEventRepository outbox,Clock clock){this.cases=cases;this.audit=audit;this.outbox=outbox;this.clock=clock;}
    public void open(TransactionDecision decision,Instant now){cases.save(ReviewCase.from(decision,now));}
    @Transactional(readOnly=true) public List<ReviewResponse> queue(String tenant){return cases.findTop100ByTenantIdAndStatusOrderByCreatedAtAsc(tenant,ReviewStatus.OPEN).stream().map(ReviewResponse::from).toList();}
    @Transactional public ReviewResponse claim(String tenant,UUID id,String analyst,long leaseSeconds){
        if(leaseSeconds<30||leaseSeconds>3600) throw new IllegalArgumentException("Lease must be between 30 and 3600 seconds");
        var review=locked(tenant,id); var now=clock.instant();
        if(review.getStatus()==ReviewStatus.RESOLVED) throw new ReviewConflictException("Review is already resolved");
        if(review.getStatus()==ReviewStatus.CLAIMED&&review.getLeaseUntil().isAfter(now)&&!analyst.equals(review.getAssignedTo())) throw new ReviewConflictException("Review is leased by another analyst");
        review.claim(analyst,now.plusSeconds(leaseSeconds)); audit.save(new ReviewAuditEvent(id,"CLAIMED",analyst,now)); return ReviewResponse.from(review);
    }
    @Transactional public ReviewResponse resolve(String tenant,UUID id,String analyst,ReviewResolution resolution,String note){
        var review=locked(tenant,id); var now=clock.instant();
        if(review.getStatus()!=ReviewStatus.CLAIMED||!analyst.equals(review.getAssignedTo())||!review.getLeaseUntil().isAfter(now)) throw new ReviewConflictException("An active lease owned by this analyst is required");
        review.resolve(resolution,note,now); audit.save(new ReviewAuditEvent(id,"RESOLVED_"+resolution,analyst,now)); outbox.save(OutboxEvent.forReviewResolution(id,review.getDecisionId(),resolution.name(),analyst,now)); return ReviewResponse.from(review);
    }
    private ReviewCase locked(String tenant,UUID id){return cases.lock(id,tenant).orElseThrow(ReviewNotFoundException::new);}
}

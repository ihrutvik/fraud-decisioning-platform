package dev.hrutvik.fraud.feedback;

import dev.hrutvik.fraud.decision.*;
import dev.hrutvik.fraud.outbox.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

@Service
public class FeedbackService {
    private final DecisionFeedbackRepository feedback; private final TransactionDecisionRepository decisions; private final OutboxEventRepository outbox; private final MeterRegistry metrics; private final Clock clock;
    public FeedbackService(DecisionFeedbackRepository feedback,TransactionDecisionRepository decisions,OutboxEventRepository outbox,MeterRegistry metrics){this(feedback,decisions,outbox,metrics,Clock.systemUTC());}
    FeedbackService(DecisionFeedbackRepository feedback,TransactionDecisionRepository decisions,OutboxEventRepository outbox,MeterRegistry metrics,Clock clock){this.feedback=feedback;this.decisions=decisions;this.outbox=outbox;this.metrics=metrics;this.clock=clock;}
    @Transactional public FeedbackResponse ingest(String tenant,String key,FeedbackRequest request){
        String fingerprint=fingerprint(request); var existing=feedback.findByTenantIdAndIdempotencyKey(tenant,key); if(existing.isPresent()) return replay(existing.get(),fingerprint);
        decisions.findByIdAndTenantId(request.decisionId(),tenant).orElseThrow(DecisionNotFoundException::new);
        var now=clock.instant(); var record=new DecisionFeedback(UUID.randomUUID(),tenant,request.decisionId(),key,fingerprint,request.label(),request.source(),request.externalReference().trim(),request.observedAt(),now);
        try{feedback.saveAndFlush(record);}catch(DataIntegrityViolationException race){var winner=feedback.findByTenantIdAndIdempotencyKey(tenant,key).or(()->feedback.findByTenantIdAndSourceAndExternalReference(tenant,request.source(),request.externalReference().trim())).orElseThrow(()->race);return replay(winner,fingerprint);}
        outbox.save(OutboxEvent.forFeedback(record)); metrics.counter("fraud.feedback.ingested","label",request.label().name(),"source",request.source().name()).increment(); return FeedbackResponse.from(record,false);
    }
    @Transactional(readOnly=true) public List<FeedbackResponse> history(String tenant,UUID decisionId){decisions.findByIdAndTenantId(decisionId,tenant).orElseThrow(DecisionNotFoundException::new);return feedback.findByTenantIdAndDecisionIdOrderByObservedAtAsc(tenant,decisionId).stream().map(f->FeedbackResponse.from(f,false)).toList();}
    private FeedbackResponse replay(DecisionFeedback existing,String fingerprint){if(!MessageDigest.isEqual(existing.getRequestFingerprint().getBytes(StandardCharsets.UTF_8),fingerprint.getBytes(StandardCharsets.UTF_8)))throw new FeedbackConflictException();return FeedbackResponse.from(existing,true);}
    private String fingerprint(FeedbackRequest r){String value=r.decisionId()+"|"+r.label()+"|"+r.source()+"|"+r.externalReference().trim()+"|"+r.observedAt();try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception impossible){throw new IllegalStateException(impossible);}}
}

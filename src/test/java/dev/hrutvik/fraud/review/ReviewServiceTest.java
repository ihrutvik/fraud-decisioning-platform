package dev.hrutvik.fraud.review;

import dev.hrutvik.fraud.outbox.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {
    private final ReviewCaseRepository cases=mock(ReviewCaseRepository.class);
    private final ReviewAuditEventRepository audit=mock(ReviewAuditEventRepository.class);
    private final OutboxEventRepository outbox=mock(OutboxEventRepository.class);
    private final Instant now=Instant.parse("2026-10-07T12:00:00Z");
    private final ReviewService service=new ReviewService(cases,audit,outbox,Clock.fixed(now,ZoneOffset.UTC));

    @Test void claimCreatesExclusiveLeaseAndAudit(){var review=open();when(cases.lock(review.getId(),"tenant-a")).thenReturn(Optional.of(review));var response=service.claim("tenant-a",review.getId(),"analyst-1",300);assertThat(response.status()).isEqualTo(ReviewStatus.CLAIMED);assertThat(response.leaseUntil()).isEqualTo(now.plusSeconds(300));verify(audit).save(any());}
    @Test void activeLeaseRejectsAnotherAnalyst(){var review=open();review.claim("analyst-1",now.plusSeconds(60));when(cases.lock(review.getId(),"tenant-a")).thenReturn(Optional.of(review));assertThatThrownBy(()->service.claim("tenant-a",review.getId(),"analyst-2",300)).isInstanceOf(ReviewConflictException.class);}
    @Test void ownerCanResolveAndEmitOutboxEvent(){var review=open();review.claim("analyst-1",now.plusSeconds(60));when(cases.lock(review.getId(),"tenant-a")).thenReturn(Optional.of(review));var response=service.resolve("tenant-a",review.getId(),"analyst-1",ReviewResolution.DECLINE,"confirmed account takeover");assertThat(response.status()).isEqualTo(ReviewStatus.RESOLVED);assertThat(response.resolution()).isEqualTo(ReviewResolution.DECLINE);verify(audit).save(any());verify(outbox).save(any());}
    @Test void expiredLeaseCannotResolve(){var review=open();review.claim("analyst-1",now.minusSeconds(1));when(cases.lock(review.getId(),"tenant-a")).thenReturn(Optional.of(review));assertThatThrownBy(()->service.resolve("tenant-a",review.getId(),"analyst-1",ReviewResolution.APPROVE,"false positive")).isInstanceOf(ReviewConflictException.class);verifyNoInteractions(outbox);}
    private ReviewCase open(){return new ReviewCase(UUID.randomUUID(),"tenant-a",UUID.randomUUID(),now.minusSeconds(30));}
}

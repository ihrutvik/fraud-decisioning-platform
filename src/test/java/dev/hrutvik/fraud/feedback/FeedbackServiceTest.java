package dev.hrutvik.fraud.feedback;

import dev.hrutvik.fraud.decision.*;
import dev.hrutvik.fraud.outbox.OutboxEventRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackServiceTest {
    private final DecisionFeedbackRepository feedback=mock(DecisionFeedbackRepository.class);
    private final TransactionDecisionRepository decisions=mock(TransactionDecisionRepository.class);
    private final OutboxEventRepository outbox=mock(OutboxEventRepository.class);
    private final SimpleMeterRegistry metrics=new SimpleMeterRegistry();
    private final Instant now=Instant.parse("2026-10-09T12:00:00Z");
    private final FeedbackService service=new FeedbackService(feedback,decisions,outbox,metrics,Clock.fixed(now,ZoneOffset.UTC));
    private final UUID decisionId=UUID.randomUUID();

    @Test void storesImmutableLabelEventAndMetric(){when(decisions.findByIdAndTenantId(decisionId,"tenant-a")).thenReturn(Optional.of(decision()));when(feedback.findByTenantIdAndIdempotencyKey("tenant-a","feedback-1")).thenReturn(Optional.empty());var response=service.ingest("tenant-a","feedback-1",request(FeedbackLabel.CONFIRMED_FRAUD));assertThat(response.replayed()).isFalse();verify(feedback).saveAndFlush(any());verify(outbox).save(any());assertThat(metrics.get("fraud.feedback.ingested").counter().count()).isEqualTo(1);}
    @Test void exactRetryReturnsOriginalWithoutDuplicateEvent(){when(decisions.findByIdAndTenantId(decisionId,"tenant-a")).thenReturn(Optional.of(decision()));when(feedback.findByTenantIdAndIdempotencyKey("tenant-a","feedback-1")).thenReturn(Optional.empty());var first=service.ingest("tenant-a","feedback-1",request(FeedbackLabel.CONFIRMED_FRAUD));var saved=org.mockito.ArgumentCaptor.forClass(DecisionFeedback.class);verify(feedback).saveAndFlush(saved.capture());reset(feedback,outbox,decisions);when(feedback.findByTenantIdAndIdempotencyKey("tenant-a","feedback-1")).thenReturn(Optional.of(saved.getValue()));var replay=service.ingest("tenant-a","feedback-1",request(FeedbackLabel.CONFIRMED_FRAUD));assertThat(replay.feedbackId()).isEqualTo(first.feedbackId());assertThat(replay.replayed()).isTrue();verifyNoInteractions(outbox,decisions);}
    @Test void mismatchedRetryIsRejected(){when(decisions.findByIdAndTenantId(decisionId,"tenant-a")).thenReturn(Optional.of(decision()));when(feedback.findByTenantIdAndIdempotencyKey("tenant-a","feedback-1")).thenReturn(Optional.empty());service.ingest("tenant-a","feedback-1",request(FeedbackLabel.CONFIRMED_FRAUD));var saved=org.mockito.ArgumentCaptor.forClass(DecisionFeedback.class);verify(feedback).saveAndFlush(saved.capture());reset(feedback,outbox,decisions);when(feedback.findByTenantIdAndIdempotencyKey("tenant-a","feedback-1")).thenReturn(Optional.of(saved.getValue()));assertThatThrownBy(()->service.ingest("tenant-a","feedback-1",request(FeedbackLabel.CONFIRMED_LEGIT))).isInstanceOf(FeedbackConflictException.class);}
    @Test void rejectsCrossTenantOrUnknownDecision(){when(feedback.findByTenantIdAndIdempotencyKey("tenant-b","feedback-1")).thenReturn(Optional.empty());when(decisions.findByIdAndTenantId(decisionId,"tenant-b")).thenReturn(Optional.empty());assertThatThrownBy(()->service.ingest("tenant-b","feedback-1",request(FeedbackLabel.CONFIRMED_FRAUD))).isInstanceOf(DecisionNotFoundException.class);verifyNoInteractions(outbox);}
    private FeedbackRequest request(FeedbackLabel label){return new FeedbackRequest(decisionId,label,FeedbackSource.CHARGEBACK,"cb-1001",now.minusSeconds(60));}
    private TransactionDecision decision(){return new TransactionDecision(decisionId,"tenant-a","key","fingerprint","tx",BigDecimal.TEN,"INR","acct",DecisionOutcome.APPROVE,0,"",1,now.minusSeconds(3600));}
}

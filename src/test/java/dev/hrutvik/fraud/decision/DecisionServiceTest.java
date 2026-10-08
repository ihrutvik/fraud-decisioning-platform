package dev.hrutvik.fraud.decision;

import dev.hrutvik.fraud.outbox.OutboxEventRepository;
import dev.hrutvik.fraud.velocity.*;
import dev.hrutvik.fraud.rules.*;
import dev.hrutvik.fraud.review.ReviewService;
import dev.hrutvik.fraud.shadow.ShadowEvaluationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DecisionServiceTest {
    @Test void persistsDecisionOutboxAndShadowEvaluationAtomically(){var decisions=mock(TransactionDecisionRepository.class);var outbox=mock(OutboxEventRepository.class);var velocity=velocity();var rules=rules();var reviews=mock(ReviewService.class);var shadow=mock(ShadowEvaluationService.class);var service=new DecisionService(decisions,outbox,new RiskEngine(),velocity,rules,reviews,shadow,Clock.fixed(Instant.parse("2026-10-04T12:00:00Z"),ZoneOffset.UTC));when(decisions.findByTenantIdAndIdempotencyKey("merchant-a","key-1")).thenReturn(Optional.empty());var result=service.decide("merchant-a","key-1",request("10"));assertThat(result.outcome()).isEqualTo(DecisionOutcome.APPROVE);assertThat(result.ruleSetVersion()).isEqualTo(1);verify(decisions).saveAndFlush(any());verify(outbox).save(any());verifyNoInteractions(reviews);verify(shadow).evaluate(any(),eq(request("10")),any(),any());}
    @Test void exactReplayReturnsOriginalWithoutSideEffects(){var decisions=mock(TransactionDecisionRepository.class);var outbox=mock(OutboxEventRepository.class);var velocity=velocity();var rules=rules();var reviews=mock(ReviewService.class);var shadow=mock(ShadowEvaluationService.class);var request=request("10.00");var seed=new DecisionService(decisions,outbox,new RiskEngine(),velocity,rules,reviews,shadow);when(decisions.findByTenantIdAndIdempotencyKey("m","k")).thenReturn(Optional.empty());var first=seed.decide("m","k",request);var saved=org.mockito.ArgumentCaptor.forClass(TransactionDecision.class);verify(decisions).saveAndFlush(saved.capture());reset(decisions,outbox,velocity,rules,reviews,shadow);when(decisions.findByTenantIdAndIdempotencyKey("m","k")).thenReturn(Optional.of(saved.getValue()));var replay=new DecisionService(decisions,outbox,new RiskEngine(),velocity,rules,reviews,shadow).decide("m","k",request("10.0"));assertThat(replay.decisionId()).isEqualTo(first.decisionId());assertThat(replay.replayed()).isTrue();verifyNoInteractions(outbox,velocity,rules,reviews,shadow);}
    @Test void mismatchedReplayIsRejected(){var decisions=mock(TransactionDecisionRepository.class);var outbox=mock(OutboxEventRepository.class);var velocity=velocity();var rules=rules();var reviews=mock(ReviewService.class);var shadow=mock(ShadowEvaluationService.class);var firstService=new DecisionService(decisions,outbox,new RiskEngine(),velocity,rules,reviews,shadow);when(decisions.findByTenantIdAndIdempotencyKey("m","k")).thenReturn(Optional.empty());firstService.decide("m","k",request("10"));var saved=org.mockito.ArgumentCaptor.forClass(TransactionDecision.class);verify(decisions).saveAndFlush(saved.capture());reset(decisions,outbox,velocity,rules,reviews,shadow);when(decisions.findByTenantIdAndIdempotencyKey("m","k")).thenReturn(Optional.of(saved.getValue()));assertThatThrownBy(()->new DecisionService(decisions,outbox,new RiskEngine(),velocity,rules,reviews,shadow).decide("m","k",request("11"))).isInstanceOf(IdempotencyConflictException.class);}
    private VelocityService velocity(){var service=mock(VelocityService.class);when(service.recordAndCount(anyString(),anyString(),any())).thenReturn(VelocitySnapshot.empty());return service;}
    private RuleSetService rules(){var service=mock(RuleSetService.class);var set=new FraudRuleSet(UUID.randomUUID(),"_default",1,2000,7,5,10,4,35,70,Instant.now());set.activate(Instant.now());when(service.active(anyString())).thenReturn(set);return service;}
    private DecisionRequest request(String amount){return new DecisionRequest("tx-1","acct-1","device-1","203.0.113.9",new BigDecimal(amount),"INR","IN","IN",true,100);}
}

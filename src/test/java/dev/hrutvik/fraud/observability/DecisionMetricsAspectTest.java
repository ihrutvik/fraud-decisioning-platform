package dev.hrutvik.fraud.observability;

import dev.hrutvik.fraud.decision.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DecisionMetricsAspectTest {
    @Test void recordsOutcomeReplayAndLatency()throws Throwable{var registry=new SimpleMeterRegistry();var aspect=new DecisionMetricsAspect(registry);var call=mock(ProceedingJoinPoint.class);when(call.proceed()).thenReturn(new DecisionResponse(UUID.randomUUID(),"tx",DecisionOutcome.REVIEW,45,2,List.of("COUNTRY_MISMATCH"),Instant.now(),true));aspect.measure(call);assertThat(registry.get("fraud.decisions").tags("outcome","REVIEW","replayed","true").counter().count()).isEqualTo(1);assertThat(registry.get("fraud.decision.latency").timer().count()).isEqualTo(1);}
    @Test void recordsFailuresAndRethrows()throws Throwable{var registry=new SimpleMeterRegistry();var aspect=new DecisionMetricsAspect(registry);var call=mock(ProceedingJoinPoint.class);when(call.proceed()).thenThrow(new IllegalStateException("boom"));assertThatThrownBy(()->aspect.measure(call)).isInstanceOf(IllegalStateException.class);assertThat(registry.get("fraud.decision.errors").tag("exception","IllegalStateException").counter().count()).isEqualTo(1);}
}

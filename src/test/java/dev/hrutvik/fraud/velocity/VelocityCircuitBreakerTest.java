package dev.hrutvik.fraud.velocity;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.assertThat;

class VelocityCircuitBreakerTest {
    @Test void opensAfterThresholdAndRejectsWithoutAnotherDependencyCall() {
        var circuit = circuit(3, 30, new MutableClock());
        for (int attempt = 0; attempt < 3; attempt++) {
            var permit = circuit.acquire();
            assertThat(permit).isEqualTo(VelocityCircuitBreaker.Permit.NORMAL);
            circuit.failure(permit);
        }
        assertThat(circuit.state()).isEqualTo(VelocityCircuitBreaker.State.OPEN);
        assertThat(circuit.acquire()).isEqualTo(VelocityCircuitBreaker.Permit.REJECTED);
    }

    @Test void allowsOnlyOneProbeAndClosesAfterRecovery() {
        var clock = new MutableClock();
        var circuit = circuit(1, 30, clock);
        circuit.failure(circuit.acquire());
        clock.advance(Duration.ofSeconds(30));
        var probe = circuit.acquire();
        assertThat(probe).isEqualTo(VelocityCircuitBreaker.Permit.PROBE);
        assertThat(circuit.acquire()).isEqualTo(VelocityCircuitBreaker.Permit.REJECTED);
        circuit.success(probe);
        assertThat(circuit.state()).isEqualTo(VelocityCircuitBreaker.State.CLOSED);
        assertThat(circuit.acquire()).isEqualTo(VelocityCircuitBreaker.Permit.NORMAL);
    }

    @Test void failedProbeReopensForAFullRecoveryWindow() {
        var clock = new MutableClock();
        var circuit = circuit(1, 30, clock);
        circuit.failure(circuit.acquire());
        clock.advance(Duration.ofSeconds(30));
        circuit.failure(circuit.acquire());
        assertThat(circuit.state()).isEqualTo(VelocityCircuitBreaker.State.OPEN);
        assertThat(circuit.acquire()).isEqualTo(VelocityCircuitBreaker.Permit.REJECTED);
    }

    private VelocityCircuitBreaker circuit(int threshold, long seconds, Clock clock) {
        return new VelocityCircuitBreaker(new VelocityProperties(300, VelocityProperties.FailurePolicy.FAIL_REVIEW, threshold, seconds), new SimpleMeterRegistry(), clock);
    }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-14T12:00:00Z");
        void advance(Duration duration) { now = now.plus(duration); }
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return now; }
    }
}

package dev.hrutvik.fraud.velocity;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.Instant;

@Component
public class VelocityCircuitBreaker {
    enum Permit { NORMAL, PROBE, REJECTED }
    enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final long openMillis;
    private final Clock clock;
    private final MeterRegistry metrics;
    private State state = State.CLOSED;
    private int consecutiveFailures;
    private Instant openedAt;
    private boolean probeInFlight;

    public VelocityCircuitBreaker(VelocityProperties properties, MeterRegistry metrics) {
        this(properties, metrics, Clock.systemUTC());
    }

    VelocityCircuitBreaker(VelocityProperties properties, MeterRegistry metrics, Clock clock) {
        this.failureThreshold = properties.circuitFailureThreshold();
        this.openMillis = properties.circuitOpenSeconds() * 1000;
        this.metrics = metrics;
        this.clock = clock;
    }

    synchronized Permit acquire() {
        if (state == State.CLOSED) return Permit.NORMAL;
        if (state == State.OPEN && elapsedOpenTime()) {
            state = State.HALF_OPEN;
            probeInFlight = true;
            transition("half_open");
            return Permit.PROBE;
        }
        if (state == State.HALF_OPEN && !probeInFlight) {
            probeInFlight = true;
            return Permit.PROBE;
        }
        metrics.counter("fraud.velocity.circuit.rejections").increment();
        return Permit.REJECTED;
    }

    synchronized void success(Permit permit) {
        if (permit == Permit.REJECTED) return;
        consecutiveFailures = 0;
        probeInFlight = false;
        if (state != State.CLOSED) {
            state = State.CLOSED;
            transition("closed");
        }
    }

    synchronized void failure(Permit permit) {
        if (permit == Permit.REJECTED) return;
        probeInFlight = false;
        consecutiveFailures++;
        if (permit == Permit.PROBE || consecutiveFailures >= failureThreshold) open();
    }

    synchronized State state() { return state; }

    private boolean elapsedOpenTime() {
        return openedAt != null && clock.millis() - openedAt.toEpochMilli() >= openMillis;
    }

    private void open() {
        boolean changed = state != State.OPEN;
        state = State.OPEN;
        openedAt = clock.instant();
        if (changed) transition("open");
    }

    private void transition(String target) {
        metrics.counter("fraud.velocity.circuit.transitions", "state", target).increment();
    }
}

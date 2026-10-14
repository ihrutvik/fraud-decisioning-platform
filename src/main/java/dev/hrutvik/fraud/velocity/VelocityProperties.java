package dev.hrutvik.fraud.velocity;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fraud.velocity")
public record VelocityProperties(long windowSeconds, FailurePolicy failurePolicy, int circuitFailureThreshold,
                                 long circuitOpenSeconds) {
    public VelocityProperties {
        if (windowSeconds <= 0) windowSeconds = 300;
        if (failurePolicy == null) failurePolicy = FailurePolicy.FAIL_REVIEW;
        if (circuitFailureThreshold <= 0) circuitFailureThreshold = 3;
        if (circuitOpenSeconds <= 0) circuitOpenSeconds = 30;
    }
    public enum FailurePolicy { FAIL_OPEN, FAIL_REVIEW }
}

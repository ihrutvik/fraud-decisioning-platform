package dev.hrutvik.fraud.velocity;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fraud.velocity")
public record VelocityProperties(long windowSeconds, FailurePolicy failurePolicy) {
    public VelocityProperties {
        if (windowSeconds <= 0) windowSeconds = 300;
        if (failurePolicy == null) failurePolicy = FailurePolicy.FAIL_REVIEW;
    }
    public enum FailurePolicy { FAIL_OPEN, FAIL_REVIEW }
}

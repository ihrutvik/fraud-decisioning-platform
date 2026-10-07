package dev.hrutvik.fraud.velocity;

public record VelocitySnapshot(long accountAttempts, long ipAttempts, long deviceAttempts, boolean degraded, boolean failReview) {
    public static VelocitySnapshot empty() { return new VelocitySnapshot(0, 0, 0, false, false); }
    public static VelocitySnapshot unavailable(boolean failReview) { return new VelocitySnapshot(0, 0, 0, true, failReview); }
}

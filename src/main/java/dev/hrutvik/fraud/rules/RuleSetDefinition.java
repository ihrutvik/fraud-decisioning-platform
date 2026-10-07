package dev.hrutvik.fraud.rules;

import jakarta.validation.constraints.*;

public record RuleSetDefinition(
        @Min(1) int highValueThreshold,
        @Min(1) @Max(3650) int newAccountDays,
        @Min(1) int accountVelocityLimit,
        @Min(1) int ipVelocityLimit,
        @Min(1) int deviceVelocityLimit,
        @Min(1) @Max(100) int reviewThreshold,
        @Min(1) @Max(100) int declineThreshold) {
    public RuleSetDefinition { if(declineThreshold<=reviewThreshold)throw new IllegalArgumentException("declineThreshold must exceed reviewThreshold"); }
}

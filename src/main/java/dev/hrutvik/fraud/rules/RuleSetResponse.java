package dev.hrutvik.fraud.rules;

import java.time.Instant;
import java.util.UUID;

public record RuleSetResponse(UUID id,long version,RuleSetStatus status,RuleSetDefinition definition,Instant createdAt,Instant activatedAt) {
    static RuleSetResponse from(FraudRuleSet r){return new RuleSetResponse(r.getId(),r.getRuleVersion(),r.getStatus(),new RuleSetDefinition(r.getHighValueThreshold(),r.getNewAccountDays(),r.getAccountVelocityLimit(),r.getIpVelocityLimit(),r.getDeviceVelocityLimit(),r.getReviewThreshold(),r.getDeclineThreshold()),r.getCreatedAt(),r.getActivatedAt());}
}

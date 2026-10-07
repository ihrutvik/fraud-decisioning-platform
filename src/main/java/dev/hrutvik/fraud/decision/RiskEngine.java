package dev.hrutvik.fraud.decision;

import dev.hrutvik.fraud.velocity.VelocitySnapshot;
import dev.hrutvik.fraud.rules.FraudRuleSet;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class RiskEngine {
    public RiskAssessment assess(DecisionRequest request, VelocitySnapshot velocity, FraudRuleSet rules) {
        int score = 0;
        List<String> reasons = new ArrayList<>();
        if (!request.billingCountry().equalsIgnoreCase(request.ipCountry())) { score += 35; reasons.add("COUNTRY_MISMATCH"); }
        if (request.amount().compareTo(BigDecimal.valueOf(rules.getHighValueThreshold())) >= 0) { score += 35; reasons.add("HIGH_VALUE"); }
        if (!request.cardPresent()) { score += 15; reasons.add("CARD_NOT_PRESENT"); }
        if (request.accountAgeDays() < rules.getNewAccountDays()) { score += 30; reasons.add("NEW_ACCOUNT"); }
        if (velocity.accountAttempts() >= rules.getAccountVelocityLimit()) { score += 40; reasons.add("ACCOUNT_VELOCITY"); }
        if (velocity.ipAttempts() >= rules.getIpVelocityLimit()) { score += 35; reasons.add("IP_VELOCITY"); }
        if (velocity.deviceAttempts() >= rules.getDeviceVelocityLimit()) { score += 30; reasons.add("DEVICE_VELOCITY"); }
        if (velocity.degraded() && velocity.failReview()) { score += 50; reasons.add("VELOCITY_CHECK_UNAVAILABLE"); }
        DecisionOutcome outcome = score >= rules.getDeclineThreshold() ? DecisionOutcome.DECLINE : score >= rules.getReviewThreshold() ? DecisionOutcome.REVIEW : DecisionOutcome.APPROVE;
        return new RiskAssessment(Math.min(score, 100), outcome, List.copyOf(reasons));
    }
    public record RiskAssessment(int score, DecisionOutcome outcome, List<String> reasonCodes) {}
}

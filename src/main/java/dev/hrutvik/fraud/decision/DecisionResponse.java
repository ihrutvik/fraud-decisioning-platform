package dev.hrutvik.fraud.decision;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DecisionResponse(UUID decisionId, String transactionId, DecisionOutcome outcome, int score, long ruleSetVersion,
                               List<String> reasonCodes, Instant decidedAt, boolean replayed) {
    static DecisionResponse from(TransactionDecision d, boolean replayed) {
        var reasons = d.getReasonCodes().isBlank() ? List.<String>of() : List.of(d.getReasonCodes().split(","));
        return new DecisionResponse(d.getId(), d.getTransactionId(), d.getOutcome(), d.getScore(), d.getRuleSetVersion(), reasons, d.getCreatedAt(), replayed);
    }
}

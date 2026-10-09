package dev.hrutvik.fraud.feedback;

import java.time.Instant;
import java.util.UUID;

public record FeedbackResponse(UUID feedbackId,UUID decisionId,FeedbackLabel label,FeedbackSource source,String externalReference,Instant observedAt,Instant createdAt,boolean replayed){
    static FeedbackResponse from(DecisionFeedback f,boolean replayed){return new FeedbackResponse(f.getId(),f.getDecisionId(),f.getLabel(),f.getSource(),f.getExternalReference(),f.getObservedAt(),f.getCreatedAt(),replayed);}
}

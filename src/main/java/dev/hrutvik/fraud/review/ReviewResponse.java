package dev.hrutvik.fraud.review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(UUID reviewId,UUID decisionId,ReviewStatus status,ReviewResolution resolution,String assignedTo,Instant leaseUntil,String analystNote,Instant createdAt,Instant resolvedAt){
    static ReviewResponse from(ReviewCase r){return new ReviewResponse(r.getId(),r.getDecisionId(),r.getStatus(),r.getResolution(),r.getAssignedTo(),r.getLeaseUntil(),r.getAnalystNote(),r.getCreatedAt(),r.getResolvedAt());}
}

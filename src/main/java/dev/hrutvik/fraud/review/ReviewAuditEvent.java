package dev.hrutvik.fraud.review;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="review_audit_events")
public class ReviewAuditEvent {
    @Id private UUID id;
    @Column(name="review_id",nullable=false) private UUID reviewId;
    @Column(name="event_type",nullable=false,length=40) private String eventType;
    @Column(name="actor_id",nullable=false,length=100) private String actorId;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected ReviewAuditEvent(){}
    ReviewAuditEvent(UUID reviewId,String eventType,String actorId,Instant at){id=UUID.randomUUID();this.reviewId=reviewId;this.eventType=eventType;this.actorId=actorId;this.createdAt=at;}
}

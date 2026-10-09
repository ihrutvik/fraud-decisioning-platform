package dev.hrutvik.fraud.feedback;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="decision_feedback",uniqueConstraints={@UniqueConstraint(name="uk_feedback_tenant_key",columnNames={"tenant_id","idempotency_key"}),@UniqueConstraint(name="uk_feedback_source_reference",columnNames={"tenant_id","source","external_reference"})})
public class DecisionFeedback {
    @Id private UUID id;
    @Column(name="tenant_id",nullable=false,length=80) private String tenantId;
    @Column(name="decision_id",nullable=false) private UUID decisionId;
    @Column(name="idempotency_key",nullable=false,length=100) private String idempotencyKey;
    @Column(name="request_fingerprint",nullable=false,length=64) private String requestFingerprint;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private FeedbackLabel label;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private FeedbackSource source;
    @Column(name="external_reference",nullable=false,length=160) private String externalReference;
    @Column(name="observed_at",nullable=false) private Instant observedAt;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected DecisionFeedback(){}
    DecisionFeedback(UUID id,String tenantId,UUID decisionId,String key,String fingerprint,FeedbackLabel label,FeedbackSource source,String reference,Instant observedAt,Instant createdAt){this.id=id;this.tenantId=tenantId;this.decisionId=decisionId;this.idempotencyKey=key;this.requestFingerprint=fingerprint;this.label=label;this.source=source;this.externalReference=reference;this.observedAt=observedAt;this.createdAt=createdAt;}
    public UUID getId(){return id;} public UUID getDecisionId(){return decisionId;} public String getRequestFingerprint(){return requestFingerprint;} public FeedbackLabel getLabel(){return label;} public FeedbackSource getSource(){return source;} public String getExternalReference(){return externalReference;} public Instant getObservedAt(){return observedAt;} public Instant getCreatedAt(){return createdAt;}
}

package dev.hrutvik.fraud.outbox;

import dev.hrutvik.fraud.decision.TransactionDecision;
import dev.hrutvik.fraud.feedback.DecisionFeedback;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="outbox_events")
public class OutboxEvent {
    @Id private UUID id;
    @Column(name="aggregate_id",nullable=false) private UUID aggregateId;
    @Column(name="event_type",nullable=false,length=80) private String eventType;
    @Column(nullable=false,columnDefinition="text") private String payload;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="published_at") private Instant publishedAt;
    protected OutboxEvent(){}
    private OutboxEvent(UUID id,UUID aggregateId,String eventType,String payload,Instant createdAt){this.id=id;this.aggregateId=aggregateId;this.eventType=eventType;this.payload=payload;this.createdAt=createdAt;}
    public static OutboxEvent forDecision(TransactionDecision d){String json="{\"decisionId\":\""+d.getId()+"\",\"transactionId\":\""+d.getTransactionId()+"\",\"outcome\":\""+d.getOutcome()+"\",\"score\":"+d.getScore()+"}";return new OutboxEvent(UUID.randomUUID(),d.getId(),"FRAUD_DECISION_CREATED",json,d.getCreatedAt());}
    public static OutboxEvent forReviewResolution(UUID reviewId, UUID decisionId, String outcome, String analystId, Instant at){
        String json="{\"reviewId\":\""+reviewId+"\",\"decisionId\":\""+decisionId+"\",\"outcome\":\""+outcome+"\",\"analystId\":\""+analystId+"\"}";
        return new OutboxEvent(UUID.randomUUID(),reviewId,"FRAUD_REVIEW_RESOLVED",json,at);
    }
    public static OutboxEvent forFeedback(DecisionFeedback f){String json="{\"feedbackId\":\""+f.getId()+"\",\"decisionId\":\""+f.getDecisionId()+"\",\"label\":\""+f.getLabel()+"\",\"source\":\""+f.getSource()+"\"}";return new OutboxEvent(UUID.randomUUID(),f.getDecisionId(),"FRAUD_FEEDBACK_INGESTED",json,f.getCreatedAt());}
    public UUID getId(){return id;} public UUID getAggregateId(){return aggregateId;} public String getPayload(){return payload;} public Instant getCreatedAt(){return createdAt;}
    public void markPublished(Instant when){publishedAt=when;}
}

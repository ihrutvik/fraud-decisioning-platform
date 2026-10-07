package dev.hrutvik.fraud.review;

import dev.hrutvik.fraud.decision.TransactionDecision;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="review_cases", uniqueConstraints=@UniqueConstraint(name="uk_review_decision", columnNames="decision_id"))
public class ReviewCase {
    @Id private UUID id;
    @Column(name="tenant_id",nullable=false,length=80) private String tenantId;
    @Column(name="decision_id",nullable=false) private UUID decisionId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private ReviewStatus status;
    @Enumerated(EnumType.STRING) @Column(length=16) private ReviewResolution resolution;
    @Column(name="assigned_to",length=100) private String assignedTo;
    @Column(name="lease_until") private Instant leaseUntil;
    @Column(name="analyst_note",length=1000) private String analystNote;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="resolved_at") private Instant resolvedAt;
    @Version private long version;
    protected ReviewCase(){}
    ReviewCase(UUID id,String tenantId,UUID decisionId,Instant createdAt){this.id=id;this.tenantId=tenantId;this.decisionId=decisionId;this.createdAt=createdAt;this.status=ReviewStatus.OPEN;}
    static ReviewCase from(TransactionDecision d,Instant now){return new ReviewCase(UUID.randomUUID(),d.getTenantId(),d.getId(),now);}
    void claim(String analyst,Instant until){status=ReviewStatus.CLAIMED;assignedTo=analyst;leaseUntil=until;}
    void resolve(ReviewResolution result,String note,Instant now){status=ReviewStatus.RESOLVED;resolution=result;analystNote=note;resolvedAt=now;leaseUntil=null;}
    public UUID getId(){return id;} public String getTenantId(){return tenantId;} public UUID getDecisionId(){return decisionId;} public ReviewStatus getStatus(){return status;} public ReviewResolution getResolution(){return resolution;} public String getAssignedTo(){return assignedTo;} public Instant getLeaseUntil(){return leaseUntil;} public String getAnalystNote(){return analystNote;} public Instant getCreatedAt(){return createdAt;} public Instant getResolvedAt(){return resolvedAt;}
}

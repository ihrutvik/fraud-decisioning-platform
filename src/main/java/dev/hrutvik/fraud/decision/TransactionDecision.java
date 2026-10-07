package dev.hrutvik.fraud.decision;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_decisions", uniqueConstraints = @UniqueConstraint(name = "uk_tenant_idempotency", columnNames = {"tenant_id", "idempotency_key"}))
public class TransactionDecision {
    @Id private UUID id;
    @Column(name="tenant_id", nullable=false, length=80) private String tenantId;
    @Column(name="idempotency_key", nullable=false, length=100) private String idempotencyKey;
    @Column(name="request_fingerprint", nullable=false, length=64) private String requestFingerprint;
    @Column(name="transaction_id", nullable=false, length=100) private String transactionId;
    @Column(nullable=false, precision=19, scale=4) private BigDecimal amount;
    @Column(nullable=false, length=3) private String currency;
    @Column(name="account_id", nullable=false, length=100) private String accountId;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private DecisionOutcome outcome;
    @Column(nullable=false) private int score;
    @Column(name="reason_codes", nullable=false, columnDefinition="text") private String reasonCodes;
    @Column(name="rule_set_version",nullable=false) private long ruleSetVersion;
    @Column(name="created_at", nullable=false) private Instant createdAt;

    protected TransactionDecision() {}
    public TransactionDecision(UUID id, String tenantId, String idempotencyKey, String requestFingerprint, String transactionId,
            BigDecimal amount, String currency, String accountId, DecisionOutcome outcome, int score, String reasonCodes, long ruleSetVersion, Instant createdAt) {
        this.id=id; this.tenantId=tenantId; this.idempotencyKey=idempotencyKey; this.requestFingerprint=requestFingerprint;
        this.transactionId=transactionId; this.amount=amount; this.currency=currency; this.accountId=accountId;
        this.outcome=outcome; this.score=score; this.reasonCodes=reasonCodes; this.ruleSetVersion=ruleSetVersion; this.createdAt=createdAt;
    }
    public UUID getId(){return id;} public String getTenantId(){return tenantId;} public String getIdempotencyKey(){return idempotencyKey;}
    public String getRequestFingerprint(){return requestFingerprint;} public String getTransactionId(){return transactionId;}
    public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;} public String getAccountId(){return accountId;}
    public DecisionOutcome getOutcome(){return outcome;} public int getScore(){return score;} public String getReasonCodes(){return reasonCodes;}
    public long getRuleSetVersion(){return ruleSetVersion;}
    public Instant getCreatedAt(){return createdAt;}
}

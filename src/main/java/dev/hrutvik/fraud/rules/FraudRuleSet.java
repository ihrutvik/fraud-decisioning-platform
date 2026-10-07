package dev.hrutvik.fraud.rules;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="fraud_rule_sets", uniqueConstraints=@UniqueConstraint(name="uk_rule_tenant_version",columnNames={"tenant_id","rule_version"}))
public class FraudRuleSet {
    @Id private UUID id;
    @Column(name="tenant_id",nullable=false,length=80) private String tenantId;
    @Column(name="rule_version",nullable=false) private long ruleVersion;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private RuleSetStatus status;
    @Column(name="high_value_threshold",nullable=false) private int highValueThreshold;
    @Column(name="new_account_days",nullable=false) private int newAccountDays;
    @Column(name="account_velocity_limit",nullable=false) private int accountVelocityLimit;
    @Column(name="ip_velocity_limit",nullable=false) private int ipVelocityLimit;
    @Column(name="device_velocity_limit",nullable=false) private int deviceVelocityLimit;
    @Column(name="review_threshold",nullable=false) private int reviewThreshold;
    @Column(name="decline_threshold",nullable=false) private int declineThreshold;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="activated_at") private Instant activatedAt;
    @Version private long lockVersion;
    protected FraudRuleSet() {}
    public FraudRuleSet(UUID id,String tenantId,long ruleVersion,int highValueThreshold,int newAccountDays,int accountVelocityLimit,
            int ipVelocityLimit,int deviceVelocityLimit,int reviewThreshold,int declineThreshold,Instant createdAt) {
        this.id=id;this.tenantId=tenantId;this.ruleVersion=ruleVersion;this.status=RuleSetStatus.DRAFT;
        this.highValueThreshold=highValueThreshold;this.newAccountDays=newAccountDays;this.accountVelocityLimit=accountVelocityLimit;
        this.ipVelocityLimit=ipVelocityLimit;this.deviceVelocityLimit=deviceVelocityLimit;this.reviewThreshold=reviewThreshold;
        this.declineThreshold=declineThreshold;this.createdAt=createdAt;
    }
    public void activate(Instant now){if(status!=RuleSetStatus.DRAFT)throw new IllegalStateException("Only draft rule sets can be activated");status=RuleSetStatus.ACTIVE;activatedAt=now;}
    public void retire(){if(status==RuleSetStatus.ACTIVE)status=RuleSetStatus.RETIRED;}
    public UUID getId(){return id;} public String getTenantId(){return tenantId;} public long getRuleVersion(){return ruleVersion;}
    public RuleSetStatus getStatus(){return status;} public int getHighValueThreshold(){return highValueThreshold;}
    public int getNewAccountDays(){return newAccountDays;} public int getAccountVelocityLimit(){return accountVelocityLimit;}
    public int getIpVelocityLimit(){return ipVelocityLimit;} public int getDeviceVelocityLimit(){return deviceVelocityLimit;}
    public int getReviewThreshold(){return reviewThreshold;} public int getDeclineThreshold(){return declineThreshold;}
    public Instant getCreatedAt(){return createdAt;} public Instant getActivatedAt(){return activatedAt;}
}

package dev.hrutvik.fraud.shadow;

import dev.hrutvik.fraud.decision.DecisionOutcome;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="shadow_evaluations",uniqueConstraints=@UniqueConstraint(name="uk_shadow_decision_version",columnNames={"decision_id","model_version"}))
public class ShadowEvaluation {
    @Id private UUID id;
    @Column(name="tenant_id",nullable=false,length=80) private String tenantId;
    @Column(name="decision_id",nullable=false) private UUID decisionId;
    @Column(name="model_version",nullable=false,length=80) private String modelVersion;
    @Column(name="shadow_score",nullable=false) private int shadowScore;
    @Enumerated(EnumType.STRING) @Column(name="shadow_outcome",nullable=false,length=16) private DecisionOutcome shadowOutcome;
    @Enumerated(EnumType.STRING) @Column(name="production_outcome",nullable=false,length=16) private DecisionOutcome productionOutcome;
    @Column(nullable=false) private boolean disagreed;
    @Column(nullable=false,columnDefinition="text") private String signals;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected ShadowEvaluation(){}
    ShadowEvaluation(UUID id,String tenantId,UUID decisionId,String modelVersion,int shadowScore,DecisionOutcome shadowOutcome,DecisionOutcome productionOutcome,String signals,Instant createdAt){this.id=id;this.tenantId=tenantId;this.decisionId=decisionId;this.modelVersion=modelVersion;this.shadowScore=shadowScore;this.shadowOutcome=shadowOutcome;this.productionOutcome=productionOutcome;this.disagreed=shadowOutcome!=productionOutcome;this.signals=signals;this.createdAt=createdAt;}
    public UUID getDecisionId(){return decisionId;} public String getModelVersion(){return modelVersion;} public int getShadowScore(){return shadowScore;} public DecisionOutcome getShadowOutcome(){return shadowOutcome;} public DecisionOutcome getProductionOutcome(){return productionOutcome;} public boolean isDisagreed(){return disagreed;} public String getSignals(){return signals;} public Instant getCreatedAt(){return createdAt;}
}

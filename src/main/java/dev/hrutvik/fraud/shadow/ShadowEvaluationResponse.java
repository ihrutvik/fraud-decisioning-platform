package dev.hrutvik.fraud.shadow;

import dev.hrutvik.fraud.decision.DecisionOutcome;
import java.time.Instant;
import java.util.*;

public record ShadowEvaluationResponse(UUID decisionId,String modelVersion,int shadowScore,DecisionOutcome shadowOutcome,DecisionOutcome productionOutcome,boolean disagreed,List<String> signals,Instant createdAt){
    static ShadowEvaluationResponse from(ShadowEvaluation e){return new ShadowEvaluationResponse(e.getDecisionId(),e.getModelVersion(),e.getShadowScore(),e.getShadowOutcome(),e.getProductionOutcome(),e.isDisagreed(),e.getSignals().isBlank()?List.of():List.of(e.getSignals().split(",")),e.getCreatedAt());}
}

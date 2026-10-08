package dev.hrutvik.fraud.shadow;

import dev.hrutvik.fraud.decision.*;
import dev.hrutvik.fraud.velocity.VelocitySnapshot;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class ShadowEvaluationService {
    private final ShadowEvaluationRepository evaluations; private final ShadowRiskModel model; private final ShadowModelProperties properties; private final MeterRegistry metrics;
    public ShadowEvaluationService(ShadowEvaluationRepository evaluations,ShadowRiskModel model,ShadowModelProperties properties,MeterRegistry metrics){this.evaluations=evaluations;this.model=model;this.properties=properties;this.metrics=metrics;}
    public void evaluate(TransactionDecision decision,DecisionRequest request,VelocitySnapshot velocity,Instant now){
        if(!properties.isEnabled()) return;
        var result=model.assess(request,velocity); boolean disagreed=result.outcome()!=decision.getOutcome();
        evaluations.save(new ShadowEvaluation(UUID.randomUUID(),decision.getTenantId(),decision.getId(),result.modelVersion(),result.score(),result.outcome(),decision.getOutcome(),String.join(",",result.signals()),now));
        metrics.counter("fraud.shadow.evaluations","model_version",result.modelVersion(),"disagreed",Boolean.toString(disagreed)).increment();
    }
    @Transactional(readOnly=true) public ShadowEvaluationResponse get(String tenant,UUID decisionId){return ShadowEvaluationResponse.from(evaluations.findByTenantIdAndDecisionId(tenant,decisionId).orElseThrow(ShadowEvaluationNotFoundException::new));}
}

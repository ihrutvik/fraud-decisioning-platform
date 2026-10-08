package dev.hrutvik.fraud.shadow;

import dev.hrutvik.fraud.decision.*;
import dev.hrutvik.fraud.velocity.VelocitySnapshot;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;

@Component
public class ShadowRiskModel {
    private final ShadowModelProperties properties;
    public ShadowRiskModel(ShadowModelProperties properties){this.properties=properties;}
    public ShadowAssessment assess(DecisionRequest request,VelocitySnapshot velocity){
        int score=0; var signals=new ArrayList<String>();
        if(!request.billingCountry().equalsIgnoreCase(request.ipCountry())){score+=30;signals.add("GEO_INCONSISTENCY");}
        if(request.amount().compareTo(BigDecimal.valueOf(1500))>=0){score+=25;signals.add("AMOUNT_ANOMALY");}
        if(!request.cardPresent()){score+=12;signals.add("REMOTE_PAYMENT");}
        if(request.accountAgeDays()<14){score+=28;signals.add("ACCOUNT_NOVELTY");}
        if(velocity.accountAttempts()>=4){score+=35;signals.add("ACCOUNT_BURST");}
        if(velocity.ipAttempts()>=8){score+=30;signals.add("IP_BURST");}
        if(velocity.deviceAttempts()>=3){score+=25;signals.add("DEVICE_BURST");}
        if(velocity.degraded()){score+=20;signals.add("FEATURE_DEGRADED");}
        score=Math.min(score,100);
        var outcome=score>=properties.getDeclineThreshold()?DecisionOutcome.DECLINE:score>=properties.getReviewThreshold()?DecisionOutcome.REVIEW:DecisionOutcome.APPROVE;
        return new ShadowAssessment(properties.getVersion(),score,outcome,List.copyOf(signals));
    }
    public record ShadowAssessment(String modelVersion,int score,DecisionOutcome outcome,List<String> signals){}
}

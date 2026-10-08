package dev.hrutvik.fraud.shadow;

import dev.hrutvik.fraud.decision.*;
import dev.hrutvik.fraud.velocity.VelocitySnapshot;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class ShadowRiskModelTest {
    private final ShadowModelProperties properties=new ShadowModelProperties();
    private final ShadowRiskModel model=new ShadowRiskModel(properties);
    @Test void lowRiskTransactionRemainsApproval(){var result=model.assess(request("100","IN","IN",true,500),VelocitySnapshot.empty());assertThat(result.outcome()).isEqualTo(DecisionOutcome.APPROVE);assertThat(result.score()).isZero();}
    @Test void candidateCanDisagreeWithProductionThresholds(){var result=model.assess(request("1700","IN","IN",false,500),VelocitySnapshot.empty());assertThat(result.outcome()).isEqualTo(DecisionOutcome.APPROVE);assertThat(result.score()).isEqualTo(37);assertThat(result.signals()).containsExactly("AMOUNT_ANOMALY","REMOTE_PAYMENT");}
    @Test void combinesNoveltyGeoAndVelocityIntoDecline(){var velocity=new VelocitySnapshot(4,1,1,false,false);var result=model.assess(request("100","IN","DE",false,2),velocity);assertThat(result.outcome()).isEqualTo(DecisionOutcome.DECLINE);assertThat(result.score()).isEqualTo(100);}
    private DecisionRequest request(String amount,String billing,String ip,boolean present,int age){return new DecisionRequest("tx","acct","device","203.0.113.10",new BigDecimal(amount),"INR",billing,ip,present,age);}
}

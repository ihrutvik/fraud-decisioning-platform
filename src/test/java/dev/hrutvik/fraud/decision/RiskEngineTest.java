package dev.hrutvik.fraud.decision;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import dev.hrutvik.fraud.velocity.VelocitySnapshot;
import dev.hrutvik.fraud.rules.FraudRuleSet;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class RiskEngineTest {
    private final RiskEngine engine=new RiskEngine();
    @Test void approvesEstablishedLowRiskTransaction(){var result=engine.assess(request("25","IN","IN",true,900),VelocitySnapshot.empty(),rules());assertThat(result.outcome()).isEqualTo(DecisionOutcome.APPROVE);assertThat(result.score()).isZero();}
    @Test void routesCountryMismatchToReview(){var result=engine.assess(request("25","IN","DE",true,900),VelocitySnapshot.empty(),rules());assertThat(result.outcome()).isEqualTo(DecisionOutcome.REVIEW);assertThat(result.reasonCodes()).containsExactly("COUNTRY_MISMATCH");}
    @Test void declinesHighValueNewAccount(){var result=engine.assess(request("2500","IN","IN",false,1),VelocitySnapshot.empty(),rules());assertThat(result.outcome()).isEqualTo(DecisionOutcome.DECLINE);assertThat(result.score()).isEqualTo(80);}
    @Test void declinesBurstingAccountWithExplainableReason(){var result=engine.assess(request("25","IN","IN",true,900),new VelocitySnapshot(5,1,1,false,false),rules());assertThat(result.outcome()).isEqualTo(DecisionOutcome.REVIEW);assertThat(result.reasonCodes()).contains("ACCOUNT_VELOCITY");}
    @Test void sendsToReviewWhenVelocityStoreFailsClosed(){var result=engine.assess(request("25","IN","IN",true,900),VelocitySnapshot.unavailable(true),rules());assertThat(result.outcome()).isEqualTo(DecisionOutcome.REVIEW);assertThat(result.reasonCodes()).containsExactly("VELOCITY_CHECK_UNAVAILABLE");}
    private FraudRuleSet rules(){var r=new FraudRuleSet(UUID.randomUUID(),"_default",1,2000,7,5,10,4,35,70,Instant.now());r.activate(Instant.now());return r;}
    private DecisionRequest request(String amount,String billing,String ip,boolean present,int age){return new DecisionRequest("tx-1","acct-1","device-1","203.0.113.9",new BigDecimal(amount),"inr",billing,ip,present,age);}
}

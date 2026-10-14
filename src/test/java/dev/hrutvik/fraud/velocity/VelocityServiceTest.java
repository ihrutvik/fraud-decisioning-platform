package dev.hrutvik.fraud.velocity;

import dev.hrutvik.fraud.decision.DecisionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.math.BigDecimal;
import java.time.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VelocityServiceTest {
    @Test void returnsCountsAcrossAllRiskDimensions(){var redis=mock(StringRedisTemplate.class);when(redis.execute(any(),anyList(),anyString(),anyString(),anyString(),anyString())).thenReturn(2L,3L,4L);var result=new VelocityService(redis,new VelocityProperties(300,VelocityProperties.FailurePolicy.FAIL_REVIEW,3,30),clock()).recordAndCount("tenant-a","idem-1",request());assertThat(result).isEqualTo(new VelocitySnapshot(2,3,4,false,false));verify(redis,times(3)).execute(any(),anyList(),anyString(),anyString(),eq("idem-1"),eq("360"));}
    @Test void appliesFailReviewPolicyDuringRedisOutage(){var redis=mock(StringRedisTemplate.class);when(redis.execute(any(),anyList(),anyString(),anyString(),anyString(),anyString())).thenThrow(new RedisConnectionFailureException("down"));var result=new VelocityService(redis,new VelocityProperties(300,VelocityProperties.FailurePolicy.FAIL_REVIEW,3,30),clock()).recordAndCount("tenant-a","idem-1",request());assertThat(result).isEqualTo(VelocitySnapshot.unavailable(true));}
    @Test void supportsExplicitFailOpenPolicy(){var redis=mock(StringRedisTemplate.class);when(redis.execute(any(),anyList(),anyString(),anyString(),anyString(),anyString())).thenThrow(new RedisConnectionFailureException("down"));var result=new VelocityService(redis,new VelocityProperties(300,VelocityProperties.FailurePolicy.FAIL_OPEN,3,30),clock()).recordAndCount("tenant-a","idem-1",request());assertThat(result).isEqualTo(VelocitySnapshot.unavailable(false));}
    @Test void openCircuitSkipsRedisAndPreservesFailurePolicy(){var redis=mock(StringRedisTemplate.class);when(redis.execute(any(),anyList(),anyString(),anyString(),anyString(),anyString())).thenThrow(new RedisConnectionFailureException("down"));var properties=new VelocityProperties(300,VelocityProperties.FailurePolicy.FAIL_REVIEW,1,30);var service=new VelocityService(redis,properties,clock());service.recordAndCount("tenant-a","idem-1",request());var result=service.recordAndCount("tenant-a","idem-2",request());assertThat(result).isEqualTo(VelocitySnapshot.unavailable(true));verify(redis,times(1)).execute(any(),anyList(),anyString(),anyString(),anyString(),anyString());}
    private Clock clock(){return Clock.fixed(Instant.parse("2026-10-05T12:00:00Z"),ZoneOffset.UTC);}
    private DecisionRequest request(){return new DecisionRequest("tx-1","acct-1","device-1","203.0.113.9",BigDecimal.TEN,"USD","US","US",false,100);}
}

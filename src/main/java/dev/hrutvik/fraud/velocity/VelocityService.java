package dev.hrutvik.fraud.velocity;

import dev.hrutvik.fraud.decision.DecisionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.util.List;

@Service
public class VelocityService {
    private static final Logger log = LoggerFactory.getLogger(VelocityService.class);
    private static final DefaultRedisScript<Long> WINDOW_SCRIPT = new DefaultRedisScript<>("""
            local key = KEYS[1]
            local cutoff = ARGV[1]
            local now = ARGV[2]
            local member = ARGV[3]
            local ttl = ARGV[4]
            redis.call('ZREMRANGEBYSCORE', key, '-inf', cutoff)
            redis.call('ZADD', key, now, member)
            redis.call('EXPIRE', key, ttl)
            return redis.call('ZCARD', key)
            """, Long.class);

    private final StringRedisTemplate redis; private final VelocityProperties properties; private final Clock clock; private final VelocityCircuitBreaker circuit;
    public VelocityService(StringRedisTemplate redis, VelocityProperties properties, VelocityCircuitBreaker circuit) { this(redis, properties, Clock.systemUTC(), circuit); }
    VelocityService(StringRedisTemplate redis, VelocityProperties properties, Clock clock) { this(redis, properties, clock, new VelocityCircuitBreaker(properties, new SimpleMeterRegistry(), clock)); }
    VelocityService(StringRedisTemplate redis, VelocityProperties properties, Clock clock, VelocityCircuitBreaker circuit) { this.redis=redis; this.properties=properties; this.clock=clock; this.circuit=circuit; }

    public VelocitySnapshot recordAndCount(String tenant, String idempotencyKey, DecisionRequest request) {
        var permit = circuit.acquire();
        if (permit == VelocityCircuitBreaker.Permit.REJECTED) {
            log.warn("Velocity circuit open; applying {} policy without calling Redis", properties.failurePolicy());
            return unavailable();
        }
        long now = clock.millis(); long cutoff = now - properties.windowSeconds() * 1000; long ttl = properties.windowSeconds() + 60;
        try {
            long account = count(key(tenant,"account",request.accountId()), cutoff, now, idempotencyKey, ttl);
            long ip = count(key(tenant,"ip",request.ipAddress()), cutoff, now, idempotencyKey, ttl);
            long device = count(key(tenant,"device",request.deviceId()), cutoff, now, idempotencyKey, ttl);
            circuit.success(permit);
            return new VelocitySnapshot(account, ip, device, false, false);
        } catch (RedisConnectionFailureException ex) {
            circuit.failure(permit);
            log.warn("Velocity store unavailable; applying {} policy", properties.failurePolicy());
            return unavailable();
        }
    }
    private VelocitySnapshot unavailable() {
        return VelocitySnapshot.unavailable(properties.failurePolicy() == VelocityProperties.FailurePolicy.FAIL_REVIEW);
    }
    private long count(String key,long cutoff,long now,String member,long ttl) {
        Long value=redis.execute(WINDOW_SCRIPT, List.of(key), Long.toString(cutoff), Long.toString(now), member, Long.toString(ttl));
        if(value==null) throw new RedisConnectionFailureException("Velocity script returned no result");
        return value;
    }
    private String key(String tenant,String dimension,String value){return "fraud:velocity:"+tenant+":"+dimension+":"+value;}
}

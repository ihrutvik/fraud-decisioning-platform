package dev.hrutvik.fraud.velocity;

import dev.hrutvik.fraud.decision.DecisionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
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

    private final StringRedisTemplate redis; private final VelocityProperties properties; private final Clock clock;
    public VelocityService(StringRedisTemplate redis, VelocityProperties properties) { this(redis, properties, Clock.systemUTC()); }
    VelocityService(StringRedisTemplate redis, VelocityProperties properties, Clock clock) { this.redis=redis; this.properties=properties; this.clock=clock; }

    public VelocitySnapshot recordAndCount(String tenant, String idempotencyKey, DecisionRequest request) {
        long now = clock.millis(); long cutoff = now - properties.windowSeconds() * 1000; long ttl = properties.windowSeconds() + 60;
        try {
            long account = count(key(tenant,"account",request.accountId()), cutoff, now, idempotencyKey, ttl);
            long ip = count(key(tenant,"ip",request.ipAddress()), cutoff, now, idempotencyKey, ttl);
            long device = count(key(tenant,"device",request.deviceId()), cutoff, now, idempotencyKey, ttl);
            return new VelocitySnapshot(account, ip, device, false, false);
        } catch (RedisConnectionFailureException ex) {
            boolean review = properties.failurePolicy() == VelocityProperties.FailurePolicy.FAIL_REVIEW;
            log.warn("Velocity store unavailable; applying {} policy", properties.failurePolicy());
            return VelocitySnapshot.unavailable(review);
        }
    }
    private long count(String key,long cutoff,long now,String member,long ttl) {
        Long value=redis.execute(WINDOW_SCRIPT, List.of(key), Long.toString(cutoff), Long.toString(now), member, Long.toString(ttl));
        if(value==null) throw new RedisConnectionFailureException("Velocity script returned no result");
        return value;
    }
    private String key(String tenant,String dimension,String value){return "fraud:velocity:"+tenant+":"+dimension+":"+value;}
}

package dev.hrutvik.fraud.decision;

import dev.hrutvik.fraud.outbox.OutboxEvent;
import dev.hrutvik.fraud.outbox.OutboxEventRepository;
import dev.hrutvik.fraud.velocity.VelocityService;
import dev.hrutvik.fraud.rules.RuleSetService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class DecisionService {
    private final TransactionDecisionRepository decisions; private final OutboxEventRepository outbox; private final RiskEngine riskEngine; private final VelocityService velocity; private final RuleSetService ruleSets; private final Clock clock;
    public DecisionService(TransactionDecisionRepository decisions, OutboxEventRepository outbox, RiskEngine riskEngine, VelocityService velocity,RuleSetService ruleSets) { this(decisions,outbox,riskEngine,velocity,ruleSets,Clock.systemUTC()); }
    DecisionService(TransactionDecisionRepository decisions, OutboxEventRepository outbox, RiskEngine riskEngine, VelocityService velocity,RuleSetService ruleSets,Clock clock) { this.decisions=decisions; this.outbox=outbox; this.riskEngine=riskEngine; this.velocity=velocity; this.ruleSets=ruleSets; this.clock=clock; }

    @Transactional
    public DecisionResponse decide(String tenantId, String idempotencyKey, DecisionRequest request) {
        String fingerprint = fingerprint(request);
        var existing = decisions.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey);
        if (existing.isPresent()) return replay(existing.get(), fingerprint);
        var velocitySnapshot = velocity.recordAndCount(tenantId, idempotencyKey, request);
        var rules = ruleSets.active(tenantId);
        var risk = riskEngine.assess(request, velocitySnapshot, rules); Instant now = clock.instant(); UUID id = UUID.randomUUID();
        var decision = new TransactionDecision(id, tenantId, idempotencyKey, fingerprint, request.transactionId(), request.amount(),
                request.currency().toUpperCase(Locale.ROOT), request.accountId(), risk.outcome(), risk.score(), String.join(",", risk.reasonCodes()), rules.getRuleVersion(), now);
        try {
            decisions.saveAndFlush(decision);
        } catch (DataIntegrityViolationException race) {
            var winner = decisions.findByTenantIdAndIdempotencyKey(tenantId,idempotencyKey).orElseThrow(() -> race);
            return replay(winner, fingerprint);
        }
        outbox.save(OutboxEvent.forDecision(decision));
        return DecisionResponse.from(decision, false);
    }
    @Transactional(readOnly=true)
    public DecisionResponse get(String tenantId, UUID id) { return DecisionResponse.from(decisions.findByIdAndTenantId(id,tenantId).orElseThrow(DecisionNotFoundException::new), false); }
    private DecisionResponse replay(TransactionDecision decision, String fingerprint) {
        if (!MessageDigest.isEqual(decision.getRequestFingerprint().getBytes(StandardCharsets.UTF_8), fingerprint.getBytes(StandardCharsets.UTF_8))) throw new IdempotencyConflictException();
        return DecisionResponse.from(decision, true);
    }
    private String fingerprint(DecisionRequest r) {
        String canonical = String.join("|", r.transactionId().trim(), r.accountId().trim(), r.deviceId().trim(), r.ipAddress().trim(), r.amount().stripTrailingZeros().toPlainString(),
                r.currency().toUpperCase(Locale.ROOT), r.billingCountry().toUpperCase(Locale.ROOT), r.ipCountry().toUpperCase(Locale.ROOT), Boolean.toString(r.cardPresent()), Integer.toString(r.accountAgeDays()));
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception impossible) { throw new IllegalStateException(impossible); }
    }
}

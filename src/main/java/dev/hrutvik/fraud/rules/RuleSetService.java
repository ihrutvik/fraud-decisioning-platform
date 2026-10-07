package dev.hrutvik.fraud.rules;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.UUID;

@Service
public class RuleSetService {
    private final FraudRuleSetRepository repository; private final Clock clock;
    public RuleSetService(FraudRuleSetRepository repository){this(repository,Clock.systemUTC());}
    RuleSetService(FraudRuleSetRepository repository,Clock clock){this.repository=repository;this.clock=clock;}
    @Transactional(readOnly=true)
    public FraudRuleSet active(String tenant){return repository.findFirstByTenantIdAndStatusOrderByRuleVersionDesc(tenant,RuleSetStatus.ACTIVE).orElseGet(()->repository.findFirstByTenantIdAndStatusOrderByRuleVersionDesc("_default",RuleSetStatus.ACTIVE).orElseThrow(()->new IllegalStateException("No active rule set")));}
    @Transactional
    public RuleSetResponse createDraft(String tenant,RuleSetDefinition d){long version=repository.maxVersion(tenant)+1;var r=new FraudRuleSet(UUID.randomUUID(),tenant,version,d.highValueThreshold(),d.newAccountDays(),d.accountVelocityLimit(),d.ipVelocityLimit(),d.deviceVelocityLimit(),d.reviewThreshold(),d.declineThreshold(),clock.instant());return RuleSetResponse.from(repository.save(r));}
    @Transactional
    public RuleSetResponse activate(String tenant,UUID id){var target=repository.findByIdAndTenantId(id,tenant).orElseThrow(RuleSetNotFoundException::new);repository.lockActive(tenant).forEach(FraudRuleSet::retire);target.activate(clock.instant());return RuleSetResponse.from(target);}
    @Transactional(readOnly=true)
    public RuleSetResponse getActive(String tenant){return RuleSetResponse.from(active(tenant));}
}

package dev.hrutvik.fraud.rules;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RuleSetServiceTest {
    private final Instant now=Instant.parse("2026-10-06T12:00:00Z");
    @Test void createsMonotonicTenantVersion(){var repo=mock(FraudRuleSetRepository.class);when(repo.maxVersion("merchant-a")).thenReturn(3L);when(repo.save(any())).thenAnswer(i->i.getArgument(0));var result=new RuleSetService(repo,Clock.fixed(now,ZoneOffset.UTC)).createDraft("merchant-a",definition());assertThat(result.version()).isEqualTo(4);assertThat(result.status()).isEqualTo(RuleSetStatus.DRAFT);}
    @Test void atomicallyRetiresPreviousActiveRule(){var repo=mock(FraudRuleSetRepository.class);var oldSet=set(1,true);var draft=set(2,false);when(repo.findByIdAndTenantId(draft.getId(),"merchant-a")).thenReturn(Optional.of(draft));when(repo.lockActive("merchant-a")).thenReturn(List.of(oldSet));var result=new RuleSetService(repo,Clock.fixed(now,ZoneOffset.UTC)).activate("merchant-a",draft.getId());assertThat(oldSet.getStatus()).isEqualTo(RuleSetStatus.RETIRED);assertThat(result.status()).isEqualTo(RuleSetStatus.ACTIVE);assertThat(result.activatedAt()).isEqualTo(now);}
    private RuleSetDefinition definition(){return new RuleSetDefinition(2000,7,5,10,4,35,70);}
    private FraudRuleSet set(long version,boolean active){var set=new FraudRuleSet(UUID.randomUUID(),"merchant-a",version,2000,7,5,10,4,35,70,now);if(active)set.activate(now);return set;}
}

package dev.hrutvik.fraud.rules;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface FraudRuleSetRepository extends JpaRepository<FraudRuleSet,UUID> {
    Optional<FraudRuleSet> findFirstByTenantIdAndStatusOrderByRuleVersionDesc(String tenantId,RuleSetStatus status);
    Optional<FraudRuleSet> findByIdAndTenantId(UUID id,String tenantId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from FraudRuleSet r where r.tenantId=:tenant and r.status=dev.hrutvik.fraud.rules.RuleSetStatus.ACTIVE")
    List<FraudRuleSet> lockActive(@Param("tenant") String tenant);
    @Query("select coalesce(max(r.ruleVersion),0) from FraudRuleSet r where r.tenantId=:tenant")
    long maxVersion(@Param("tenant") String tenant);
}

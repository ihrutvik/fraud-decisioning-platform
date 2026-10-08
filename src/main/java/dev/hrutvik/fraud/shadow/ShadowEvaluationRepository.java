package dev.hrutvik.fraud.shadow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ShadowEvaluationRepository extends JpaRepository<ShadowEvaluation,UUID> {
    Optional<ShadowEvaluation> findByTenantIdAndDecisionId(String tenantId,UUID decisionId);
}

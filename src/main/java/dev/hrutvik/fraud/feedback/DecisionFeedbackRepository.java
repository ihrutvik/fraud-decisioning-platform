package dev.hrutvik.fraud.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DecisionFeedbackRepository extends JpaRepository<DecisionFeedback,UUID> {
    Optional<DecisionFeedback> findByTenantIdAndIdempotencyKey(String tenantId,String key);
    Optional<DecisionFeedback> findByTenantIdAndSourceAndExternalReference(String tenantId,FeedbackSource source,String externalReference);
    List<DecisionFeedback> findByTenantIdAndDecisionIdOrderByObservedAtAsc(String tenantId,UUID decisionId);
}

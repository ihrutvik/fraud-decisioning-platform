package dev.hrutvik.fraud.decision;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TransactionDecisionRepository extends JpaRepository<TransactionDecision, UUID> {
    Optional<TransactionDecision> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);
    Optional<TransactionDecision> findByIdAndTenantId(UUID id, String tenantId);
}

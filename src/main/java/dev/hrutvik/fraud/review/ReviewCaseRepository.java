package dev.hrutvik.fraud.review;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ReviewCaseRepository extends JpaRepository<ReviewCase,UUID> {
    Optional<ReviewCase> findByDecisionId(UUID decisionId);
    List<ReviewCase> findTop100ByTenantIdAndStatusOrderByCreatedAtAsc(String tenantId,ReviewStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReviewCase r where r.id=:id and r.tenantId=:tenant")
    Optional<ReviewCase> lock(@Param("id") UUID id,@Param("tenant") String tenant);
}

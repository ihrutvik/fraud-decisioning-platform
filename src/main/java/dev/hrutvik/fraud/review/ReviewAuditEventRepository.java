package dev.hrutvik.fraud.review;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReviewAuditEventRepository extends JpaRepository<ReviewAuditEvent,UUID> {}

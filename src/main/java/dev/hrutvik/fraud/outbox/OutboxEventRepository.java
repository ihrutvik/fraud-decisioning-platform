package dev.hrutvik.fraud.outbox;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query(value="select * from outbox_events where published_at is null order by created_at limit :batch for update skip locked",nativeQuery=true)
    List<OutboxEvent> lease(@Param("batch") int batch);
}

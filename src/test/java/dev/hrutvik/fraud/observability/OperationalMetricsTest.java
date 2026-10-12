package dev.hrutvik.fraud.observability;

import dev.hrutvik.fraud.outbox.OutboxEventRepository;
import dev.hrutvik.fraud.review.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OperationalMetricsTest {
    @Test void snapshotsBacklogAndReviewPressure(){var outbox=mock(OutboxEventRepository.class);var reviews=mock(ReviewCaseRepository.class);when(outbox.countByPublishedAtIsNull()).thenReturn(17L);when(reviews.countByStatus(ReviewStatus.OPEN)).thenReturn(9L);when(reviews.countByStatus(ReviewStatus.CLAIMED)).thenReturn(3L);var registry=new SimpleMeterRegistry();var metrics=new OperationalMetrics(outbox,reviews,registry);metrics.refresh();assertThat(registry.get("fraud.outbox.backlog").gauge().value()).isEqualTo(17);assertThat(registry.get("fraud.review.queue").tag("status","OPEN").gauge().value()).isEqualTo(9);}
}

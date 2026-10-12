package dev.hrutvik.fraud.observability;

import dev.hrutvik.fraud.outbox.OutboxEventRepository;
import dev.hrutvik.fraud.review.*;
import io.micrometer.core.instrument.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OperationalMetrics {
    private final OutboxEventRepository outbox; private final ReviewCaseRepository reviews; private final AtomicLong outboxBacklog=new AtomicLong(); private final AtomicLong openReviews=new AtomicLong(); private final AtomicLong claimedReviews=new AtomicLong();
    public OperationalMetrics(OutboxEventRepository outbox,ReviewCaseRepository reviews,MeterRegistry registry){this.outbox=outbox;this.reviews=reviews;Gauge.builder("fraud.outbox.backlog",outboxBacklog,AtomicLong::get).register(registry);Gauge.builder("fraud.review.queue",openReviews,AtomicLong::get).tag("status","OPEN").register(registry);Gauge.builder("fraud.review.queue",claimedReviews,AtomicLong::get).tag("status","CLAIMED").register(registry);}
    @Scheduled(fixedDelayString="${fraud.metrics.refresh-ms:10000}")
    public void refresh(){outboxBacklog.set(outbox.countByPublishedAtIsNull());openReviews.set(reviews.countByStatus(ReviewStatus.OPEN));claimedReviews.set(reviews.countByStatus(ReviewStatus.CLAIMED));}
}

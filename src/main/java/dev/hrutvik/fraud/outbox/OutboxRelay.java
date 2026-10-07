package dev.hrutvik.fraud.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;

@Component
public class OutboxRelay {
    private final OutboxEventRepository events; private final KafkaTemplate<String,String> kafka; private final Clock clock=Clock.systemUTC();
    @Value("${fraud.outbox.batch-size:100}") int batchSize;
    public OutboxRelay(OutboxEventRepository events,KafkaTemplate<String,String> kafka){this.events=events;this.kafka=kafka;}
    @Scheduled(fixedDelayString="${fraud.outbox.poll-ms:1000}") @Transactional
    public void publish(){for(var event:events.lease(batchSize)){kafka.send("fraud.decisions.v1",event.getAggregateId().toString(),event.getPayload()).join();event.markPublished(clock.instant());}}
}

package com.sentinel.ingest;


import com.sentinel.ingest.IngestStats;
import com.sentinel.model.OrderCancelled;
import com.sentinel.model.json.TradeEventCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.time.Instant;

import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@DisabledOnOs(value = OS.MAC, disabledReason = "Kafka container exits with code 1 on my Mac — revisit")
class TradeEventListenerIT {

    @Container
    @ServiceConnection                       // Spring Boot points spring.kafka.bootstrap-servers at this container
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka-native:3.9.0");

    @Autowired
    KafkaTemplate<String, String> kafka;

    @Autowired
    TradeEventCodec codec;

    @Autowired
    IngestStats stats;

    @Test
    void consumesEveryPublishedEvent() {
        for (int i = 1; i <= 50; i++) {
            var event = new OrderCancelled("E-" + i, "O-" + i, "A" + (i % 5), Instant.parse("2026-10-27T14:00:00Z"));
            kafka.send("trade-events", event.accountId(), codec.encode(event));
        }

        await().atMost(Duration.ofSeconds(30)).until(() -> stats.total() == 50);
    }
}
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class TradeEventListenerIT {

    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:3.9.1")
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094")
            .withLogConsumer(frame -> System.out.print("[kafka] " + frame.getUtf8String()));

    @Autowired
    KafkaTemplate<String, String> kafka;

    @Autowired
    TradeEventCodec codec;

    @Autowired
    IngestStats stats;

    @Test
    void threeListenerThreadsShareTheWork() {
        for (int i = 1; i <= 600; i++) {
            var event = new OrderCancelled("E-" + i, "O-" + i, "A" + (i % 50), Instant.parse("2026-10-27T14:00:00Z"));
            kafka.send("trade-events", event.accountId(), codec.encode(event));
        }

        await().atMost(Duration.ofSeconds(30)).until(() -> stats.total() == 600);

        assertThat(stats.perThread()).hasSize(3);                                       // concurrency: 3
        assertThat(stats.perThread().values().stream().mapToLong(Long::longValue).sum()).isEqualTo(600);
    }
}
package com.sentinel.simulator.publish;

import com.sentinel.model.TradeEvent;
import com.sentinel.model.json.TradeEventCodec;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class KafkaEventPublisher implements EventPublisher {
    private static final String TOPIC = "trade-events";
    private final KafkaProducer<String, String> producer;
    private final TradeEventCodec codec;
    private final AtomicReference<Exception> firstFailure = new AtomicReference<>();   // written by Kafka's thread, read by ours


    public KafkaEventPublisher(final String bootStrapServers,
                               final TradeEventCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
        this.producer = new KafkaProducer<>(producerProperties(bootStrapServers));
    }


    @Override
    public CompletableFuture<Void> publish(TradeEvent event) {
        var confirmed = new CompletableFuture<Void>();
        var record = new ProducerRecord<>(TOPIC, event.accountId(), codec.encode(event));
        producer.send(record, (recordMetadata, e) -> {
            if (e != null) {
                confirmed.completeExceptionally(e);
            } else {
                confirmed.complete(null);
            }
        });

        return confirmed;
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
        var failure = firstFailure.get();
        if (failure != null) {
            throw new IllegalStateException("At least one event was not published", failure);
        }
    }

    private static Properties producerProperties(String bootstrapServers) {
        var properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "5");
        return properties;
    }
}


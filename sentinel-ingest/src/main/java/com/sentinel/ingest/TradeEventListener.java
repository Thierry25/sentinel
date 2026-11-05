package com.sentinel.ingest;

import com.sentinel.model.json.TradeEventCodec;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class TradeEventListener {
    private final TradeEventCodec codec;
    private final IngestStats stats;

    public TradeEventListener(final TradeEventCodec codec,
                              final IngestStats stats) {
        this.codec = codec;
        this.stats = stats;
    }

    @KafkaListener(topics = "trade-events")
    void onEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        codec.decode(record.value());
        stats.record(record.partition());
        ack.acknowledge();
    }
}

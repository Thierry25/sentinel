package com.sentinel.ingest;

import com.sentinel.model.json.TradeEventCodec;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class IngestConfig {

    @Bean
    public TradeEventCodec codec() {
        return new TradeEventCodec();
    }

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name("trade-events")
                .partitions(6)
                .replicas(1)
                .build();
    }
}

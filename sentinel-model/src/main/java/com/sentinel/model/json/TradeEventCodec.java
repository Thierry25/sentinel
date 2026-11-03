package com.sentinel.model.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sentinel.model.*;

public final class TradeEventCodec {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = OrderPlaced.class, name = "ORDER_PLACED"),
            @JsonSubTypes.Type(value = OrderAmended.class, name = "ORDER_AMENDED"),
            @JsonSubTypes.Type(value = OrderCancelled.class, name = "ORDER_CANCELLED"),
            @JsonSubTypes.Type(value = OrderFilled.class, name = "ORDER_FILLED"),
    })
    private interface TradeEventMixIn {}

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
            .addMixIn(TradeEvent.class, TradeEventMixIn.class)
            .build();

    public String encode(TradeEvent event) {
        try {
            return mapper.writerFor(TradeEvent.class).writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot encode " + event, e);
        }
    }

    public TradeEvent decode(String json) {
        try {
            return mapper.readValue(json, TradeEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot decode " + json, e);
        }
    }


}

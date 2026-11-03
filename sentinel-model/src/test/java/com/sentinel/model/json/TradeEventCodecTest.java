package com.sentinel.model.json;

import com.sentinel.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TradeEventCodecTest {

    private final TradeEventCodec codec = new TradeEventCodec();

    @Test
    void orderPlacedDecodedEncodeIsSame() {
        var orderPlaced = new OrderPlaced("E-001", "orderid", "A100", Instant.now(),
                "MSF", Side.SELL, 200, null);
        assertThat(orderPlaced).isEqualTo(codec.decode(codec.encode(orderPlaced)));
    }

    @Test
    void orderCancelledDecodedEncodeIsSame() {
        var orderCancelled = new OrderCancelled("E-001", "139", "A100", Instant.now());
        assertThat(orderCancelled).isEqualTo(codec.decode(codec.encode(orderCancelled)));
    }

    @Test
    void orderFilledDecodedEncodeIsSame() {
        var orderFilled = new OrderFilled("E-001", "orderid", "A100", Instant.now(),
                "MSF", Side.SELL, 200, Money.of("100", "USD"));
        assertThat(orderFilled).isEqualTo(codec.decode(codec.encode(orderFilled)));
    }

    @Test
    void orderAmendedDecodedEncodeIsSame() {
        var orderAmended = new OrderAmended("E-001", "130", "A100", Instant.now(), 15, null);
        assertThat(orderAmended).isEqualTo(codec.decode(codec.encode(orderAmended)));
    }

    @Test
    void verifyJsonContainsSomeValues() {
        var instant = Instant.parse("2026-10-27T10:15:30.00Z");
        var orderFilled = new OrderFilled("E-001", "orderid", "A100", instant,
                "MSF", Side.SELL, 200, Money.of("100", "USD"));
        var json = codec.encode(orderFilled);
        assertThat(json).contains("\"type\":\"ORDER_FILLED\"");
        assertThat(json).contains("\"2026-10-27T");
    }

    @Test
    void verifyErrorIsThrownByBadType() {
        assertThatThrownBy(() -> codec.decode("{\"type\":\"NOPE\"}")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verifyErrorIsThrownByUnexpectedString() {
        assertThatThrownBy(() -> codec.decode("not a string")).isInstanceOf(IllegalArgumentException.class);
    }
}

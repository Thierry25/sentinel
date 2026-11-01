package com.sentinel.simulator.patterns;

import com.sentinel.model.OrderCancelled;
import com.sentinel.model.OrderFilled;
import com.sentinel.model.OrderPlaced;
import com.sentinel.model.Side;
import com.sentinel.model.TradeEvent;
import com.sentinel.simulator.IdGenerator;
import com.sentinel.simulator.SimulationConfig;
import com.sentinel.simulator.flow.TradingDay;
import com.sentinel.simulator.universe.Universe;
import com.sentinel.simulator.universe.UniverseFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PlantersTest {

    private static final SimulationConfig CONFIG = SimulationConfig.defaults(42);
    private static final Universe UNIVERSE = new UniverseFactory().create(CONFIG, new Random(42));
    private static final TradingDay DAY = new TradingDay(CONFIG.day());

    private static Planted plant(PatternPlanter planter) {
        return planter.plant(UNIVERSE, DAY, new Random(7), new IdGenerator());
    }

    private static Map<String, TradeEvent> byId(Planted planted) {
        return planted.events().stream().collect(Collectors.toMap(TradeEvent::eventId, Function.identity()));
    }

    @Test
    void washTrades_twentyBuyThenSellOfTheSameQuantityWithinSeconds() {
        var planted = plant(new WashTradePlanter());
        var events = byId(planted);

        assertThat(planted.answers()).hasSize(20).allSatisfy(answer -> {
            assertThat(answer.type()).isEqualTo(PatternType.WASH_TRADE);
            var buy = (OrderFilled) events.get(answer.eventIds().get(0));
            var sell = (OrderFilled) events.get(answer.eventIds().get(1));
            assertThat(buy.side()).isEqualTo(Side.BUY);
            assertThat(sell.side()).isEqualTo(Side.SELL);
            assertThat(sell.accountId()).isEqualTo(buy.accountId()).isEqualTo(answer.accountId());
            assertThat(sell.symbol()).isEqualTo(buy.symbol()).isEqualTo(answer.symbol());
            assertThat(sell.quantity()).isEqualTo(buy.quantity());
            assertThat(Duration.between(buy.timestamp(), sell.timestamp()))
                    .isBetween(Duration.ofSeconds(5), Duration.ofSeconds(57));
            assertThat(UNIVERSE.homeSymbols().get(answer.accountId())).doesNotContain(answer.symbol());
        });
    }

    @Test
    void spoofing_tenBigOrdersCancelledWithinFourSeconds() {
        var planted = plant(new SpoofingPlanter());
        var events = byId(planted);

        assertThat(planted.answers()).hasSize(10).allSatisfy(answer -> {
            var placed = (OrderPlaced) events.get(answer.eventIds().get(0));
            var cancelled = (OrderCancelled) events.get(answer.eventIds().get(1));
            assertThat(placed.quantity()).isBetween(10_000L, 50_000L);
            assertThat(cancelled.orderId()).isEqualTo(placed.orderId());
            assertThat(Duration.between(placed.timestamp(), cancelled.timestamp()))
                    .isBetween(Duration.ofSeconds(1), Duration.ofSeconds(4));
        });
    }

    @Test
    void restricted_fifteenFillsOnRestrictedSymbols() {
        var planted = plant(new RestrictedTradePlanter());
        var events = byId(planted);

        assertThat(planted.answers()).hasSize(15).allSatisfy(answer -> {
            var fill = (OrderFilled) events.get(answer.eventIds().get(0));
            assertThat(UNIVERSE.restrictedSymbols()).contains(fill.symbol());
        });
    }
}

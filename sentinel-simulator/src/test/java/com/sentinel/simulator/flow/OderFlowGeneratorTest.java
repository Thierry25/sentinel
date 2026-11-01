package com.sentinel.simulator.flow;

import com.sentinel.model.*;
import com.sentinel.simulator.IdGenerator;
import com.sentinel.simulator.SimulationConfig;
import com.sentinel.simulator.universe.Universe;
import com.sentinel.simulator.universe.UniverseFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class OrderFlowGeneratorTest {

    private static final SimulationConfig CONFIG = SimulationConfig.defaults(42);
    private static final Run RUN = run(42);           // one generated day, shared by the read-only tests

    // ---------- determinism ----------

    @Test
    void sameSeedGivesTheSameEvents() {
        assertThat(run(42).events()).isEqualTo(RUN.events());
    }

    @Test
    void differentSeedGivesDifferentEvents() {
        assertThat(run(43).events()).isNotEqualTo(RUN.events());
    }

    // ---------- shape of the day ----------

    @Test
    void placesExactlyTheConfiguredNumberOfOrders() {
        assertThat(eventsOf(OrderPlaced.class)).hasSize(CONFIG.orders());
    }

    @Test
    void eventsAreSortedByTimeThenId() {
        assertThat(RUN.events()).isSortedAccordingTo(
                Comparator.comparing(TradeEvent::timestamp).thenComparing(TradeEvent::eventId));
    }

    @Test
    void everyEventIsDuringTradingHours() {
        var day = new TradingDay(CONFIG.day());

        assertThat(RUN.events()).allSatisfy(e ->
                assertThat(e.timestamp()).isBetween(day.open(), day.close()));
    }

    @Test
    void tenToTwentyPercentCancelledAndFiveToFifteenPercentAmended() {
        double placed = CONFIG.orders();

        assertThat(eventsOf(OrderCancelled.class).size() / placed).isBetween(0.10, 0.20);
        assertThat(eventsOf(OrderAmended.class).size() / placed).isBetween(0.05, 0.15);
    }

    @Test
    void everyOrderIsOnOneOfTheAccountsHomeSymbols() {
        assertThat(eventsOf(OrderPlaced.class)).allSatisfy(p ->
                assertThat(RUN.universe().homeSymbols().get(p.accountId())).contains(p.symbol()));
    }

    @Test
    void anAmendedOrderIsFilledAfterItsAmendmentWithTheNewQuantity() {
        Map<String, OrderFilled> fillsByOrder = eventsOf(OrderFilled.class).stream()
                .collect(Collectors.toMap(OrderFilled::orderId, Function.identity()));

        assertThat(eventsOf(OrderAmended.class)).allSatisfy(amend -> {
            var fill = fillsByOrder.get(amend.orderId());
            assertThat(fill).isNotNull();
            assertThat(fill.timestamp()).isAfter(amend.timestamp());
            assertThat(fill.quantity()).isEqualTo(amend.newQuantity());
        });
    }

    // ---------- no accidental patterns ----------

    @Test
    void noAccountFillsBothSidesOfTheSameSymbol() {          // no accidental wash trade
        var sidesPerAccountAndSymbol = eventsOf(OrderFilled.class).stream()
                .collect(Collectors.groupingBy(
                        f -> List.of(f.accountId(), f.symbol()),          // a safe composite key
                        Collectors.mapping(OrderFilled::side, Collectors.toSet())));

        assertThat(sidesPerAccountAndSymbol.values()).allSatisfy(sides -> assertThat(sides).hasSize(1));
    }

    @Test
    void noOrderIsCancelledWithinThirtySeconds() {           // no accidental spoofing
        Map<String, OrderPlaced> placedByOrder = eventsOf(OrderPlaced.class).stream()
                .collect(Collectors.toMap(OrderPlaced::orderId, Function.identity()));

        assertThat(eventsOf(OrderCancelled.class)).allSatisfy(cancel -> {
            var placed = placedByOrder.get(cancel.orderId());
            assertThat(Duration.between(placed.timestamp(), cancel.timestamp()))
                    .isGreaterThanOrEqualTo(Duration.ofSeconds(30));
        });
    }

    @Test
    void noRestrictedSymbolIsTraded() {
        var restricted = RUN.universe().restrictedSymbols();

        assertThat(eventsOf(OrderPlaced.class)).noneMatch(p -> restricted.contains(p.symbol()));
    }

    @Test
    void everyQuantityIsBetween100And2000() {
        assertThat(eventsOf(OrderPlaced.class)).allSatisfy(p ->
                assertThat(p.quantity()).isBetween(100L, 2_000L));
        assertThat(eventsOf(OrderAmended.class)).allSatisfy(a ->
                assertThat(a.newQuantity()).isBetween(100L, 2_000L));
    }

    // ---------- helpers ----------

    private record Run(Universe universe, List<TradeEvent> events) {}

    /** Builds a whole day exactly as Simulator will: one Random and one IdGenerator for the run. */
    private static Run run(long seed) {
        var config = SimulationConfig.defaults(seed);
        var random = new Random(seed);
        var universe = new UniverseFactory().create(config, random);
        var events = new OrderFlowGenerator().generate(universe, config, random, new IdGenerator());
        return new Run(universe, events);
    }

    private static <T extends TradeEvent> List<T> eventsOf(Class<T> type) {
        return RUN.events().stream().filter(type::isInstance).map(type::cast).toList();
    }
}

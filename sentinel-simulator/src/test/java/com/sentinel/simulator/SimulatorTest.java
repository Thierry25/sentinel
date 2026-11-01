package com.sentinel.simulator;

import com.sentinel.model.OrderFilled;
import com.sentinel.model.OrderPlaced;
import com.sentinel.model.Side;
import com.sentinel.model.TradeEvent;
import com.sentinel.simulator.flow.OrderFlowGenerator;
import com.sentinel.simulator.patterns.AnswerKeyEntry;
import com.sentinel.simulator.patterns.PatternPlanter;
import com.sentinel.simulator.patterns.PatternType;
import com.sentinel.simulator.patterns.Planted;
import com.sentinel.simulator.patterns.RestrictedTradePlanter;
import com.sentinel.simulator.patterns.SpoofingPlanter;
import com.sentinel.simulator.patterns.WashTradePlanter;
import com.sentinel.simulator.universe.UniverseFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatorTest {

    private static final SimulationResult RESULT = Simulator.standard().run(SimulationConfig.defaults(42));

    @Test
    void sameSeedGivesTheSameResult() {
        assertThat(Simulator.standard().run(SimulationConfig.defaults(42))).isEqualTo(RESULT);
    }

    @Test
    void answerKeyHasTwentyTenAndFifteen() {
        var counts = RESULT.answerKey().stream()
                .collect(Collectors.groupingBy(AnswerKeyEntry::type, Collectors.counting()));

        assertThat(counts).isEqualTo(Map.of(
                PatternType.WASH_TRADE, 20L, PatternType.SPOOFING, 10L, PatternType.RESTRICTED_LIST, 15L));
    }

    @Test
    void everyAnswerKeyEventExists() {
        var ids = RESULT.events().stream().map(TradeEvent::eventId).collect(Collectors.toSet());

        assertThat(RESULT.answerKey()).allSatisfy(entry -> assertThat(ids).containsAll(entry.eventIds()));
    }

    @Test
    void eventsAreSorted() {
        assertThat(RESULT.events()).isSortedAccordingTo(Simulator.CHRONOLOGICAL);
    }

    @Test
    void aNewPlanterNeedsNoChangeToSimulator() {                     // Open/Closed, proven
        PatternPlanter oneRestrictedTrade = (universe, day, random, ids) -> {
            var account = universe.accounts().getFirst().accountId();
            var symbol = universe.restrictedSymbolsInOrder().getFirst();
            var price = universe.referencePrice(symbol);
            var time = day.randomOrderTime(random);
            var order = ids.nextOrderId();
            var placed = new OrderPlaced(ids.nextEventId(), order, account, time, symbol, Side.BUY, 100, price);
            var fill = new OrderFilled(ids.nextEventId(), order, account, time.plusSeconds(1), symbol, Side.BUY, 100, price);
            return new Planted(List.of(placed, fill),
                    List.of(new AnswerKeyEntry(PatternType.RESTRICTED_LIST, account, symbol, List.of(fill.eventId()))));
        };

        var simulator = new Simulator(new UniverseFactory(), new OrderFlowGenerator(), List.of(
                new WashTradePlanter(), new SpoofingPlanter(), new RestrictedTradePlanter(), oneRestrictedTrade));

        assertThat(simulator.run(SimulationConfig.defaults(42)).answerKey()).hasSize(46);
    }
}
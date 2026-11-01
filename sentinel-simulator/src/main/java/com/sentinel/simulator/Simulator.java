package com.sentinel.simulator;

import com.sentinel.model.TradeEvent;
import com.sentinel.simulator.flow.OrderFlowGenerator;
import com.sentinel.simulator.flow.TradingDay;
import com.sentinel.simulator.patterns.AnswerKeyEntry;
import com.sentinel.simulator.patterns.PatternPlanter;
import com.sentinel.simulator.patterns.RestrictedTradePlanter;
import com.sentinel.simulator.patterns.SpoofingPlanter;
import com.sentinel.simulator.patterns.WashTradePlanter;
import com.sentinel.simulator.universe.UniverseFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class Simulator {

    /** Same timestamp: the event id decides, so the order is always the same. */
    public static final Comparator<TradeEvent> CHRONOLOGICAL =
            Comparator.comparing(TradeEvent::timestamp).thenComparing(TradeEvent::eventId);

    private final UniverseFactory universes;
    private final OrderFlowGenerator flow;
    private final List<PatternPlanter> planters;

    public Simulator(UniverseFactory universes, OrderFlowGenerator flow, List<PatternPlanter> planters) {
        this.universes = Objects.requireNonNull(universes, "universes");
        this.flow = Objects.requireNonNull(flow, "flow");
        this.planters = List.copyOf(planters);
    }

    /** The real configuration: the 3 planters with their default counts (20 + 10 + 15). */
    public static Simulator standard() {
        return new Simulator(new UniverseFactory(), new OrderFlowGenerator(),
                List.of(new WashTradePlanter(), new SpoofingPlanter(), new RestrictedTradePlanter()));
    }

    public SimulationResult run(SimulationConfig config) {
        var random = new Random(config.seed());     // ONE source of randomness for the whole run
        var ids = new IdGenerator();                // ONE id counter for the whole run
        var universe = universes.create(config, random);
        var day = new TradingDay(config.day());

        List<TradeEvent> events = new ArrayList<>(flow.generate(universe, config, random, ids));
        List<AnswerKeyEntry> answerKey = new ArrayList<>();

        for (var planter : planters) {              // Simulator never knows WHICH planters these are
            var planted = planter.plant(universe, day, random, ids);
            events.addAll(planted.events());
            answerKey.addAll(planted.answers());
        }

        events.sort(CHRONOLOGICAL);
        return new SimulationResult(universe, events, answerKey);
    }
}
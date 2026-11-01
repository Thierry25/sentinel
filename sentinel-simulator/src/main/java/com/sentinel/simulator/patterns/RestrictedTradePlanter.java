package com.sentinel.simulator.patterns;

import com.sentinel.model.OrderFilled;
import com.sentinel.model.OrderPlaced;
import com.sentinel.model.Side;
import com.sentinel.model.TradeEvent;
import com.sentinel.simulator.IdGenerator;
import com.sentinel.simulator.flow.TradingDay;
import com.sentinel.simulator.universe.Universe;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class RestrictedTradePlanter implements PatternPlanter {

    public static final int DEFAULT_COUNT = 15;

    private final int count;

    public RestrictedTradePlanter() {
        this(DEFAULT_COUNT);
    }

    public RestrictedTradePlanter(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        this.count = count;
    }

    @Override
    public Planted plant(Universe universe, TradingDay day, RandomGenerator random, IdGenerator ids) {
        var restricted = universe.restrictedSymbolsInOrder();      // sorted, so the same seed picks the same symbol
        List<TradeEvent> events = new ArrayList<>();
        List<AnswerKeyEntry> answers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            var account = universe.randomAccount(random).accountId();
            var symbol = restricted.get(random.nextInt(restricted.size()));
            var price = universe.referencePrice(symbol);
            long quantity = random.nextLong(100, 2_001);
            var side = random.nextBoolean() ? Side.BUY : Side.SELL;

            var placedAt = day.randomOrderTime(random);
            var filledAt = placedAt.plusSeconds(random.nextInt(1, 21));

            var orderId = ids.nextOrderId();
            var placed = new OrderPlaced(ids.nextEventId(), orderId, account, placedAt, symbol, side, quantity, price);
            var fill = new OrderFilled(ids.nextEventId(), orderId, account, filledAt, symbol, side, quantity, price);

            events.addAll(List.of(placed, fill));
            answers.add(new AnswerKeyEntry(PatternType.RESTRICTED_LIST, account, symbol, List.of(fill.eventId())));
        }
        return new Planted(events, answers);
    }
}
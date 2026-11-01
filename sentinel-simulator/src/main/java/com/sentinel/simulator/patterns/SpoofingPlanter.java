package com.sentinel.simulator.patterns;

import com.sentinel.model.OrderCancelled;
import com.sentinel.model.OrderPlaced;
import com.sentinel.model.Side;
import com.sentinel.model.TradeEvent;
import com.sentinel.simulator.IdGenerator;
import com.sentinel.simulator.flow.TradingDay;
import com.sentinel.simulator.universe.Universe;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public class SpoofingPlanter implements PatternPlanter {
    public static final int DEFAULT_COUNT = 10;
    public static final long MIN_QUANTITY = 10_000;
    public static final long MAX_QUANTITY = 50_000;

    private final int count;

    public SpoofingPlanter() {
        this(DEFAULT_COUNT);
    }

    public SpoofingPlanter(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        this.count = count;
    }

    @Override
    public Planted plant(Universe universe, TradingDay day, RandomGenerator random, IdGenerator ids) {
        List<TradeEvent> events = new ArrayList<>();
        List<AnswerKeyEntry> answers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            var account = universe.randomAccount(random).accountId();
            var symbol = universe.foreignSymbolFor(account, random);
            var price = universe.referencePrice(symbol);
            long quantity = random.nextLong(MIN_QUANTITY, MAX_QUANTITY + 1);
            var side = random.nextBoolean() ? Side.BUY : Side.SELL;

            var placedAt = day.randomOrderTime(random);
            var cancelledAt = placedAt.plusSeconds(random.nextInt(1, 5));       // 1 to 4 s later

            var orderId = ids.nextOrderId();
            var placed = new OrderPlaced(ids.nextEventId(), orderId, account, placedAt, symbol, side, quantity, price);
            var cancelled = new OrderCancelled(ids.nextEventId(), orderId, account, cancelledAt);

            events.addAll(List.of(placed, cancelled));
            answers.add(new AnswerKeyEntry(PatternType.SPOOFING, account, symbol,
                    List.of(placed.eventId(), cancelled.eventId())));
        }
        return new Planted(events, answers);
    }

}

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

public class WashTradePlanter implements PatternPlanter {
    public static final int DEFAULT_COUNT = 20;

    private final int count;

    public WashTradePlanter() {
        this(DEFAULT_COUNT);
    }

    public WashTradePlanter(int count) {
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
            var symbol = universe.foreignSymbolFor(account, random);     // outside its normal trading
            var price = universe.referencePrice(symbol);
            long quantity = random.nextLong(100, 2_001);

            var buyPlacedAt = day.randomOrderTime(random);
            var buyFilledAt = buyPlacedAt.plusSeconds(1);
            var sellPlacedAt = buyFilledAt.plusSeconds(random.nextInt(5, 56));   // the gap: 5 to 55 s
            var sellFilledAt = sellPlacedAt.plusSeconds(1);

            var buyOrder = ids.nextOrderId();
            var buyPlaced = new OrderPlaced(ids.nextEventId(), buyOrder, account, buyPlacedAt, symbol, Side.BUY, quantity, price);
            var buyFill = new OrderFilled(ids.nextEventId(), buyOrder, account, buyFilledAt, symbol, Side.BUY, quantity, price);

            var sellOrder = ids.nextOrderId();
            var sellPlaced = new OrderPlaced(ids.nextEventId(), sellOrder, account, sellPlacedAt, symbol, Side.SELL, quantity, price);
            var sellFill = new OrderFilled(ids.nextEventId(), sellOrder, account, sellFilledAt, symbol, Side.SELL, quantity, price);

            events.addAll(List.of(buyPlaced, buyFill, sellPlaced, sellFill));
            answers.add(new AnswerKeyEntry(PatternType.WASH_TRADE, account, symbol,
                    List.of(buyFill.eventId(), sellFill.eventId())));       // the proof: the two fills
        }
        return new Planted(events, answers);
    }
}

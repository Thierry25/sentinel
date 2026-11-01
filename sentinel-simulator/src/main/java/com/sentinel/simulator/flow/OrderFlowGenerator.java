package com.sentinel.simulator.flow;

import com.sentinel.model.*;
import com.sentinel.simulator.IdGenerator;
import com.sentinel.simulator.SimulationConfig;
import com.sentinel.simulator.universe.Universe;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public class OrderFlowGenerator {
    public List<TradeEvent> generate(Universe universe, SimulationConfig config, RandomGenerator random, IdGenerator ids) {
        var tradingDay = new TradingDay(config.day());
        Map<String, Instrument> instrumentsBySymbol = universe.instruments().stream()
                .collect(Collectors.toMap(Instrument::symbol, Function.identity()));

        Map<AccountSymbol, Side> sides = new HashMap<>();
        List<TradeEvent> events = new ArrayList<>();

        for(int i = 0; i < config.orders(); i++) {
            var account = universe.accounts().get(random.nextInt(universe.accounts().size()));
            var home = universe.homeSymbols().get(account.accountId());
            var symbol = home.get(random.nextInt(home.size()));

            var side = sides.computeIfAbsent(new AccountSymbol(account.accountId(), symbol),
                    key -> random.nextBoolean() ? Side.BUY : Side.SELL);

            long quantity = random.nextLong(100, 2_001);
            Money price = instrumentsBySymbol.get(symbol).referencePrice();

            Instant placedAt = tradingDay.randomOrderTime(random);
            String orderId = ids.nextOrderId();
            events.add(new OrderPlaced(ids.nextEventId(), orderId, account.accountId(), placedAt,
                    symbol, side, quantity, price));

            int outcome = random.nextInt(100);

            if (outcome < 80) {
                var filledAt = placedAt.plusSeconds(random.nextInt(1, 21));
                events.add(new OrderFilled(ids.nextEventId(), orderId, account.accountId(), filledAt,
                        symbol, side, quantity, price));
            } else if (outcome < 90) {
                var cancelledAt = placedAt.plusSeconds(random.nextInt(30, 601));
                events.add(new OrderCancelled(ids.nextEventId(), orderId, account.accountId(), cancelledAt));
            } else {
                var amendedAt = placedAt.plusSeconds(random.nextInt(10, 61));
                var qty = random.nextInt(100, 2_001);
                events.add(new OrderAmended(ids.nextEventId(), orderId, account.accountId(), amendedAt,
                        qty, price));
                var filledAt = amendedAt.plusSeconds(random.nextInt(1, 21));
                events.add(new OrderFilled(ids.nextEventId(), orderId, account.accountId(), filledAt,
                        symbol, side, qty, price));
            }
        }
        events.sort(Comparator.comparing(TradeEvent::timestamp)
                .thenComparing(TradeEvent::eventId));
        return events;
    }

    private record AccountSymbol(String accountId, String symbol) {}
}

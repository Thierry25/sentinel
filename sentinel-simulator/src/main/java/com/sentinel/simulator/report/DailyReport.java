package com.sentinel.simulator.report;

import com.sentinel.model.Account;
import com.sentinel.model.Desk;
import com.sentinel.model.Money;
import com.sentinel.model.OrderCancelled;
import com.sentinel.model.OrderFilled;
import com.sentinel.model.OrderPlaced;
import com.sentinel.model.TradeEvent;
import com.sentinel.simulator.universe.Universe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toMap;

/** The analyst's morning numbers. Built with streams and collectors only: no loops. */
public record DailyReport(Map<Desk, Money> filledNotionalByDesk,
                          List<SymbolNotional> top10SymbolsByNotional,
                          Map<String, BigDecimal> cancelRatioByAccount,
                          List<String> accountsWithCancelRatioAbove50Percent) {

    private static final Currency USD = Currency.getInstance("USD");
    private static final BigDecimal FIFTY_PERCENT = new BigDecimal("0.50");
    private static final int TOP = 10;

    public DailyReport {
        filledNotionalByDesk = Map.copyOf(filledNotionalByDesk);
        top10SymbolsByNotional = List.copyOf(top10SymbolsByNotional);
        cancelRatioByAccount = Map.copyOf(cancelRatioByAccount);
        accountsWithCancelRatioAbove50Percent = List.copyOf(accountsWithCancelRatioAbove50Percent);
    }

    public static DailyReport from(List<TradeEvent> events, Universe universe) {
        Map<String, Desk> deskByAccount = universe.accounts().stream()
                .collect(toMap(Account::accountId, Account::desk));

        List<OrderFilled> fills = only(events, OrderFilled.class);

        // 1. sum of notionals per desk: start at 0.00 USD, turn each fill into price × quantity, add them up
        Map<Desk, Money> byDesk = fills.stream()
                .collect(groupingBy(fill -> deskByAccount.get(fill.accountId()),
                        reducing(Money.zero(USD), DailyReport::notional, Money::plus)));

        // 2. same per symbol, then the 10 biggest (ties broken by symbol so the order never changes)
        List<SymbolNotional> top10 = fills.stream()
                .collect(groupingBy(OrderFilled::symbol,
                        reducing(Money.zero(USD), DailyReport::notional, Money::plus)))
                .entrySet().stream()
                .map(entry -> new SymbolNotional(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing((SymbolNotional s) -> s.notional().amount()).reversed()
                        .thenComparing(SymbolNotional::symbol))
                .limit(TOP)
                .toList();

        // 3. cancelled orders ÷ placed orders, per account that placed at least one order
        Map<String, Long> placedByAccount = only(events, OrderPlaced.class).stream()
                .collect(groupingBy(OrderPlaced::accountId, counting()));
        Map<String, Long> cancelledByAccount = only(events, OrderCancelled.class).stream()
                .collect(groupingBy(OrderCancelled::accountId, counting()));

        Map<String, BigDecimal> ratios = placedByAccount.entrySet().stream()
                .collect(toMap(Map.Entry::getKey,
                        entry -> ratio(cancelledByAccount.getOrDefault(entry.getKey(), 0L), entry.getValue())));

        // 4. the accounts above 50%, sorted
        List<String> above50 = ratios.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(FIFTY_PERCENT) > 0)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        return new DailyReport(byDesk, top10, ratios, above50);
    }

    private static Money notional(OrderFilled fill) {
        return fill.price().times(fill.quantity());
    }

    private static BigDecimal ratio(long cancelled, long placed) {
        return BigDecimal.valueOf(cancelled).divide(BigDecimal.valueOf(placed), 2, RoundingMode.HALF_EVEN);
    }

    /** Keeps only the events of one type, already cast: only(events, OrderFilled.class). */
    private static <T extends TradeEvent> List<T> only(List<TradeEvent> events, Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).toList();
    }
}

package com.sentinel.simulator.report;

import com.sentinel.model.Account;
import com.sentinel.model.Desk;
import com.sentinel.model.Money;
import com.sentinel.model.TradeEvent;
import com.sentinel.simulator.Simulator;
import com.sentinel.simulator.SimulationConfig;
import com.sentinel.simulator.universe.Universe;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sentinel.model.Side.BUY;
import static com.sentinel.model.Side.SELL;
import static com.sentinel.simulator.TestEvents.cancelled;
import static com.sentinel.simulator.TestEvents.filled;
import static com.sentinel.simulator.TestEvents.placed;
import static org.assertj.core.api.Assertions.assertThat;

class DailyReportTest {

    private static final Universe UNIVERSE = new Universe(
            List.of(new Account("A1", Desk.EQUITIES), new Account("A2", Desk.FX)),
            List.of(), Set.of(), Map.of());

    private static final List<TradeEvent> EVENTS = List.of(
            placed().order("O1").account("A1").symbol("AAPL").side(BUY).quantity(100).price("230.00").build(),
            filled().order("O1").account("A1").symbol("AAPL").side(BUY).quantity(100).price("230.00").build(),
            placed().order("O2").account("A1").symbol("MSFT").side(SELL).quantity(50).price("400.00").build(),
            filled().order("O2").account("A1").symbol("MSFT").side(SELL).quantity(50).price("400.00").build(),
            placed().order("O3").account("A2").symbol("EURUSD").side(BUY).quantity(1_000).price("1.08").build(),
            cancelled().order("O3").account("A2").build(),
            placed().order("O4").account("A2").symbol("EURUSD").side(BUY).quantity(2_000).price("1.09").build(),
            cancelled().order("O4").account("A2").build(),
            placed().order("O5").account("A2").symbol("EURUSD").side(SELL).quantity(500).price("1.10").build(),
            filled().order("O5").account("A2").symbol("EURUSD").side(SELL).quantity(500).price("1.10").build());

    private final DailyReport report = DailyReport.from(EVENTS, UNIVERSE);

    @Test
    void sumsFilledNotionalPerDesk() {
        assertThat(report.filledNotionalByDesk()).isEqualTo(Map.of(
                Desk.EQUITIES, Money.of("43000.00", "USD"),
                Desk.FX, Money.of("550.00", "USD")));                // no fills on FIXED_INCOME: absent
    }

    @Test
    void ranksSymbolsByNotionalBiggestFirst() {
        assertThat(report.top10SymbolsByNotional()).containsExactly(
                new SymbolNotional("AAPL", Money.of("23000.00", "USD")),
                new SymbolNotional("MSFT", Money.of("20000.00", "USD")),
                new SymbolNotional("EURUSD", Money.of("550.00", "USD")));
    }

    @Test
    void computesCancelRatioPerAccount() {
        assertThat(report.cancelRatioByAccount()).isEqualTo(Map.of(
                "A1", new BigDecimal("0.00"),                         // 0 of 2
                "A2", new BigDecimal("0.67")));                       // 2 of 3
    }

    @Test
    void listsAccountsAboveFiftyPercent() {
        assertThat(report.accountsWithCancelRatioAbove50Percent()).containsExactly("A2");
    }

    @Test
    void emptyEventListGivesAnEmptyReport() {
        var empty = DailyReport.from(List.of(), UNIVERSE);

        assertThat(empty.filledNotionalByDesk()).isEmpty();
        assertThat(empty.top10SymbolsByNotional()).isEmpty();
        assertThat(empty.cancelRatioByAccount()).isEmpty();
        assertThat(empty.accountsWithCancelRatioAbove50Percent()).isEmpty();
    }

    @Test
    void equalNotionalsAreOrderedBySymbol() {
        var tie = List.of(
                filled().order("O1").symbol("BBB").quantity(100).price("10.00").build(),
                filled().order("O2").symbol("AAA").quantity(100).price("10.00").build());

        assertThat(DailyReport.from(tie, UNIVERSE).top10SymbolsByNotional())
                .extracting(SymbolNotional::symbol)
                .containsExactly("AAA", "BBB");
    }

    @Test
    void worksOnARealSimulatedDay() {
        var result = Simulator.standard().run(SimulationConfig.defaults(42));

        var real = DailyReport.from(result.events(), result.universe());

        assertThat(real.filledNotionalByDesk()).hasSize(3);
        assertThat(real.top10SymbolsByNotional()).hasSize(10);
    }
}
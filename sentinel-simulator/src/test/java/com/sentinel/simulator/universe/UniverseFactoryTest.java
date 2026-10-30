package com.sentinel.simulator.universe;

import com.sentinel.model.Account;
import com.sentinel.model.Desk;
import com.sentinel.model.Instrument;
import com.sentinel.simulator.SimulationConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UniverseFactoryTest {

    private final SimulationConfig config = SimulationConfig.defaults(42);
    private final Universe universe = new UniverseFactory().create(config, new Random(42));

    @Test
    void createsFiveHundredAccountsNumberedFromA0001() {
        assertThat(universe.accounts()).hasSize(500);
        assertThat(universe.accounts().getFirst().accountId()).isEqualTo("A0001");
        assertThat(universe.accounts().getLast().accountId()).isEqualTo("A0500");
    }

    @Test
    void spreadsAccountsOverTheThreeDesksInRotation() {
        var perDesk = universe.accounts().stream()
                .collect(Collectors.groupingBy(Account::desk, Collectors.counting()));

        assertThat(perDesk).containsEntry(Desk.EQUITIES, 167L)
                .containsEntry(Desk.FIXED_INCOME, 167L)
                .containsEntry(Desk.FX, 166L);
        assertThat(universe.accounts().get(0).desk()).isEqualTo(Desk.EQUITIES);
        assertThat(universe.accounts().get(1).desk()).isEqualTo(Desk.FIXED_INCOME);
        assertThat(universe.accounts().get(2).desk()).isEqualTo(Desk.FX);
    }

    @Test
    void createsFortyInstrumentsWithPricesBetween10And500() {
        assertThat(universe.instruments()).hasSize(40);
        assertThat(universe.instruments().getFirst().symbol()).isEqualTo("S01");
        assertThat(universe.instruments()).allSatisfy(instrument -> {
            var price = instrument.referencePrice().amount();
            assertThat(price).isBetween(new BigDecimal("10.00"), new BigDecimal("500.00"));
            assertThat(instrument.referencePrice().currency().getCurrencyCode()).isEqualTo("USD");
        });
    }

    @Test
    void putsThreeExistingSymbolsOnTheRestrictedList() {
        var allSymbols = universe.instruments().stream().map(Instrument::symbol).toList();

        assertThat(universe.restrictedSymbols()).hasSize(3);
        assertThat(allSymbols).containsAll(universe.restrictedSymbols());
    }

    @Test
    void givesEveryAccountFiveDistinctHomeSymbolsNeverRestricted() {
        assertThat(universe.homeSymbols()).hasSize(500);
        assertThat(universe.homeSymbols().values()).allSatisfy(home -> {
            assertThat(home).hasSize(5);
            assertThat(new HashSet<>(home)).hasSize(5);                       // distinct
            assertThat(home).doesNotContainAnyElementsOf(universe.restrictedSymbols());
        });
    }

    @Test
    void sameSeedGivesTheSameUniverse() {
        var again = new UniverseFactory().create(config, new Random(42));

        assertThat(again).isEqualTo(universe);
    }

    @Test
    void differentSeedGivesADifferentUniverse() {
        var other = new UniverseFactory().create(SimulationConfig.defaults(43), new Random(43));

        assertThat(other.restrictedSymbols().equals(universe.restrictedSymbols())
                   && other.homeSymbols().equals(universe.homeSymbols())).isFalse();
    }
}
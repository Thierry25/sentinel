package com.sentinel.simulator.universe;

import com.sentinel.model.Account;
import com.sentinel.model.Desk;
import com.sentinel.model.Instrument;
import com.sentinel.model.Money;
import com.sentinel.simulator.SimulationConfig;

import java.math.BigDecimal;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public class UniverseFactory {
    private static final List<String> SECTORS = List.of("Tech", "Banks", "Energy", "Health", "Retail");
    private static final Currency USD = Currency.getInstance("USD");
    private static final int RESTRICTED_COUNT = 3;
    private static final int HOME_SYMBOLS_PER_ACCOUNT = 5;

    public Universe create(SimulationConfig config, RandomGenerator random) {
        final List<Account> accounts = new ArrayList<>();
        final List<Instrument> instruments = new ArrayList<>();
        final Map<String, List<String>> homeSymbols = new HashMap<>();

        for (int i = 0; i < config.accounts(); i++) {
            accounts.add(new Account("A%04d".formatted(i+1), Desk.values()[i % Desk.values().length]));
        }

        for (int i = 0; i < config.symbols(); i++) {
            int cents = random.nextInt(1_000, 50_001);
            instruments.add(new Instrument("S%02d".formatted(i + 1), SECTORS.get(i % SECTORS.size()),
                    new Money(BigDecimal.valueOf(cents, 2), USD)));
        }

        var symbols = instruments.stream()
                .map(Instrument::symbol)
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(symbols, random);

        Set<String> restrictedSymbols = symbols.stream()
                .limit(RESTRICTED_COUNT)
                .collect(Collectors.toSet());

        symbols.removeAll(restrictedSymbols);
        var allowed = symbols.stream()
                .filter(s -> !restrictedSymbols.contains(s))
                .toList();

        for (var account : accounts) {
            var copySymbols = new ArrayList<>(allowed);;
            Collections.shuffle(copySymbols, random);
            var homeSymbolsListPerAccount = copySymbols.stream()
                    .limit(HOME_SYMBOLS_PER_ACCOUNT)
                    .toList();
            homeSymbols.put(account.accountId(), homeSymbolsListPerAccount);
        }

        return new Universe(accounts, instruments, restrictedSymbols, homeSymbols);
    }
}

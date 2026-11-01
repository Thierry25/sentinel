package com.sentinel.simulator.universe;

import com.sentinel.model.Account;
import com.sentinel.model.Instrument;
import com.sentinel.model.Money;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

public record Universe(List<Account> accounts, List<Instrument> instruments,
                       Set<String> restrictedSymbols, Map<String, List<String>> homeSymbols) {

    public Universe {
        accounts = List.copyOf(accounts);
        instruments = List.copyOf(instruments);
        restrictedSymbols = Set.copyOf(restrictedSymbols);
        homeSymbols = Map.copyOf(homeSymbols);
    }

    public Account randomAccount(RandomGenerator random) {
        return accounts.get(random.nextInt(accounts.size()));
    }

    public Money referencePrice(String symbol) {
        return instruments.stream()
                .filter(instrument -> instrument.symbol().equals(symbol))
                .findFirst()
                .map(Instrument::referencePrice)
                .orElseThrow(() -> new IllegalArgumentException("Unknown symbol: " + symbol));
    }

    /**
     * The restricted symbols in alphabetical order.
     * Set.copyOf has no fixed iteration order (it can change between two JVM runs),
     * so anything random that picks from the set must pick from a sorted list instead.
     */
    public List<String> restrictedSymbolsInOrder() {
        return restrictedSymbols.stream().sorted().toList();
    }

    /** A random symbol that is neither one of the account's home symbols nor restricted. */
    public String foreignSymbolFor(String accountId, RandomGenerator random) {
        var home = homeSymbols.get(accountId);
        if (home == null) {
            throw new IllegalArgumentException("Unknown account: " + accountId);
        }
        var candidates = instruments.stream()                     // instruments is a List: fixed order, so deterministic
                .map(Instrument::symbol)
                .filter(symbol -> !home.contains(symbol) && !restrictedSymbols.contains(symbol))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No foreign symbol available for " + accountId);
        }
        return candidates.get(random.nextInt(candidates.size()));
    }
}


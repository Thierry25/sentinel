package com.sentinel.simulator.universe;

import com.sentinel.model.Account;
import com.sentinel.model.Instrument;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record Universe(List<Account> accounts, List<Instrument> instruments,
                       Set<String> restrictedSymbols, Map<String, List<String>> homeSymbols) {

    public Universe {
        accounts = List.copyOf(accounts);
        instruments = List.copyOf(instruments);
        restrictedSymbols = Set.copyOf(restrictedSymbols);
        homeSymbols = Map.copyOf(homeSymbols);
    }
}


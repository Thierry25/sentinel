package com.sentinel.simulator.patterns;

import com.sentinel.simulator.IdGenerator;
import com.sentinel.simulator.flow.TradingDay;
import com.sentinel.simulator.universe.Universe;

import java.util.random.RandomGenerator;

@FunctionalInterface
public interface PatternPlanter {
    Planted plant(Universe universe, TradingDay day, RandomGenerator random, IdGenerator ids);
}


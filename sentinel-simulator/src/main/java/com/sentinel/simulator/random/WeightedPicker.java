package com.sentinel.simulator.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

public class WeightedPicker<T> {
    private final List<T> items;
    private final double[] prefixSum;
    private final double total;

    public WeightedPicker(List<Weighted<T>> items) {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Cannot have null or empty list");

        var itemsInOrder = new ArrayList<T>(items.size());
        prefixSum = new double[items.size()];
        double runningTotal = 0.0;

        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (item == null) throw new IllegalArgumentException("Cannot have null");
            var weight = item.weight();
            if (weight < 0) throw new IllegalArgumentException("Cannot be less than zero");
            runningTotal += weight;
            prefixSum[i] = runningTotal;
            itemsInOrder.add(item.item());
        }

        if (runningTotal == 0.0) throw new IllegalArgumentException("Cannot have weight of only zero");
        this.items = List.copyOf(itemsInOrder);
        this.total = runningTotal;
    }

    public T pick(RandomGenerator random) {
        var value = random.nextDouble() * total;
        int index = Arrays.binarySearch(prefixSum, value);
        if (index < 0) {
            var insertionPoint = -(index) - 1;
            return items.get(insertionPoint);
        }

        var curVal = prefixSum[index];
        while (curVal <= value && index < prefixSum.length - 1) {
            index++;
            curVal = prefixSum[index];
        }
        return items.get(index);

    }
}


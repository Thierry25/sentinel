package com.sentinel.simulator.random;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.*;

public class WeightedPickerTest {

    private static final int PICKS = 100_000;

    @Test
    void dartExactlyOnAnEdgeBelongsToTheNextSegment() {
        var picker = picker("AAPL", 5, "MSFT", 3, "XYZ", 2);       // edges [5, 8, 10]

        assertThat(picker.pick(alwaysReturning(0.5))).isEqualTo("MSFT");   // dart = 5.0
        assertThat(picker.pick(alwaysReturning(0.8))).isEqualTo("XYZ");    // dart = 8.0
    }

    @Test
    void dartAtZeroPicksTheFirstItem() {
        var picker = picker("AAPL", 5, "MSFT", 3, "XYZ", 2);

        assertThat(picker.pick(alwaysReturning(0.0))).isEqualTo("AAPL");
    }

    @Test
    void largestPossibleDartPicksTheLastItemWithoutCrashing() {
        var picker = picker("AAPL", 5, "MSFT", 3, "XYZ", 2);

        assertThat(picker.pick(alwaysReturning(Math.nextDown(1.0)))).isEqualTo("XYZ");
    }

    @Test
    void dartOnARepeatedEdgeSkipsTheZeroWeightItem() {
        var picker = picker("A", 2, "B", 0, "C", 5, "D", 3);       // edges [2, 2, 7, 10]

        assertThat(picker.pick(alwaysReturning(0.2))).isEqualTo("C");      // dart = 2.0, never B
    }

    // behaviour over many picks

    @Test
    void singleItemIsAlwaysPicked() {
        var picker = picker("ONLY", 1);
        var random = new Random(42);

        for (int i = 0; i < 1_000; i++) {
            assertThat(picker.pick(random)).isEqualTo("ONLY");
        }
    }

    @Test
    void zeroWeightItemIsNeverPicked() {
        var counts = countPicks(picker("A", 2, "B", 0, "C", 5, "D", 3), new Random(42));

        assertThat(counts).doesNotContainKey("B");
    }

    @Test
    void picksFollowTheWeights() {
        var counts = countPicks(picker("A", 1, "B", 3, "C", 6), new Random(42));

        // within 1 percentage point of 10% / 30% / 60%
        assertThat(counts.get("A")).isCloseTo(10_000, within(1_000));
        assertThat(counts.get("B")).isCloseTo(30_000, within(1_000));
        assertThat(counts.get("C")).isCloseTo(60_000, within(1_000));
    }

    // ---------- invalid input ----------

    @Test
    void refusesAnEmptyList() {
        assertThatThrownBy(() -> new WeightedPicker<String>(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesANullList() {
        assertThatThrownBy(() -> new WeightedPicker<String>(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesANegativeWeight() {
        assertThatThrownBy(() -> picker("A", 1, "B", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesAllZeroWeights() {
        assertThatThrownBy(() -> picker("A", 0, "B", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // -- helpers
    private static WeightedPicker<String> picker(Object... itemsAndWeights) {
        var list = new java.util.ArrayList<Weighted<String>>();
        for (int i = 0; i < itemsAndWeights.length; i += 2) {
            var item = (String) itemsAndWeights[i];
            var weight = ((Number) itemsAndWeights[i + 1]).doubleValue();
            list.add(new Weighted<>(item, weight));
        }
        return new WeightedPicker<>(list);
    }

    private static Map<String, Integer> countPicks(WeightedPicker<String> picker, RandomGenerator random) {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < PICKS; i++) {
            counts.merge(picker.pick(random), 1, Integer::sum);
        }
        return counts;
    }

    private static RandomGenerator alwaysReturning(double value) {
        return new RandomGenerator() {
            @Override public long nextLong() { throw new UnsupportedOperationException(); }
            @Override public double nextDouble() { return value; }
        };
    }
}

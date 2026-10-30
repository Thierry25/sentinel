package com.sentinel.simulator.flow;

import com.sentinel.simulator.random.Weighted;
import com.sentinel.simulator.random.WeightedPicker;

import java.time.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class TradingDay {
    private final LocalDate localDate;
    private final WeightedPicker<Integer> minutePicker;
    private static final LocalTime OPEN = LocalTime.of(9, 30);
    private static final LocalTime CLOSE = LocalTime.of(16, 0);

    private static final int ORDER_WINDOW_MINUTES = 375;
    private static final int RUSH_MINUTES = 30;
    private static final long MINUTES_TO_SECONDS = 60L;
    private static final ZoneId zoneId = ZoneId.of("America/New_York");

    public TradingDay(LocalDate day) {
        this.localDate = Objects.requireNonNull(day, "day");

        var minutes = new ArrayList<Weighted<Integer>>();
        for (int i = 0; i < ORDER_WINDOW_MINUTES; i++) {
            // weight given
            double weight = (i < RUSH_MINUTES || i >= ORDER_WINDOW_MINUTES - RUSH_MINUTES) ? 3 : 1;
            minutes.add(new Weighted<>(i, weight));
        }
        this.minutePicker = new WeightedPicker<>(minutes);
    }

    public Instant randomOrderTime(RandomGenerator random) {
        int minute = minutePicker.pick(random);
        int second = random.nextInt(60);
        return open().plusSeconds(minute * MINUTES_TO_SECONDS + second);
    }

    public Instant open() {
        return generateMarketTime(OPEN);
    }

    public Instant close() {
        return generateMarketTime(CLOSE);
    }

    private Instant generateMarketTime(LocalTime localTime) {
        ZonedDateTime time = ZonedDateTime.of(
                this.localDate,
                localTime,
                zoneId
        );
        return time.toInstant();
    }
}

package com.sentinel.simulator;
import com.sentinel.model.Desk;
import com.sentinel.simulator.patterns.AnswerKeyCsv;
import com.sentinel.simulator.patterns.AnswerKeyEntry;
import com.sentinel.simulator.patterns.PatternType;
import com.sentinel.simulator.report.DailyReport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/** Run with: ./gradlew :sentinel-simulator:run --args="--seed 42 --orders 20000" */
public final class SimulatorApp {

    private SimulatorApp() {
    }

    public static void main(String[] args) throws IOException {
        long seed = argument(args, "--seed", 42);
        int orders = (int) argument(args, "--orders", 20_000);

        var defaults = SimulationConfig.defaults(seed);
        var config = new SimulationConfig(seed, defaults.day(), defaults.accounts(), defaults.symbols(), orders);
        var result = Simulator.standard().run(config);

        var answerKeyFile = Path.of("build", "sim", "answer-key.csv");
        AnswerKeyCsv.write(result.answerKey(), answerKeyFile);

        var counts = result.answerKey().stream()
                .collect(groupingBy(AnswerKeyEntry::type, () -> new EnumMap<>(PatternType.class), counting()));
        System.out.printf("Seed %d: %,d events, answer key %s%n", seed, result.events().size(), counts);
        System.out.println("Answer key written to " + answerKeyFile.toAbsolutePath());

        var report = DailyReport.from(result.events(), result.universe());
        System.out.println("Filled notional per desk:");
        Arrays.stream(Desk.values()).forEach(desk ->
                System.out.printf("  %-13s %s%n", desk, report.filledNotionalByDesk().get(desk)));
        System.out.println("Top 3 symbols: " + report.top10SymbolsByNotional().stream().limit(3).toList());
        System.out.println("Accounts above 50% cancels: " + report.accountsWithCancelRatioAbove50Percent().size());
    }

    /** Reads "--name value" from the arguments, or returns the default. */
    private static long argument(String[] args, String name, long defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(name)) {
                return Long.parseLong(args[i + 1]);
            }
        }
        return defaultValue;
    }
}

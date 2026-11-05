package com.sentinel.simulator;

import com.sentinel.model.Desk;
import com.sentinel.model.json.TradeEventCodec;
import com.sentinel.simulator.patterns.AnswerKeyCsv;
import com.sentinel.simulator.patterns.AnswerKeyEntry;
import com.sentinel.simulator.patterns.PatternType;
import com.sentinel.simulator.publish.KafkaEventPublisher;
import com.sentinel.simulator.report.DailyReport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * Run with: ./gradlew :sentinel-simulator:run --args="--seed 42 --orders 20000"
 */
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

        String kafka = textArgument(args, "--publish");            // null when the argument isn't there
        if (kafka != null) {
            long start = System.nanoTime();
            try (var publisher = new KafkaEventPublisher(kafka, new TradeEventCodec())) {
                boolean sync = hasFlag(args, "--sync");
                if (sync) {
                    for (var event : result.events()) {
                        publisher.publish(event).join();
                    }
                } else {
                    var confirmations = result.events().stream()
                            .map(publisher::publish)
                            .toList();

                    CompletableFuture.allOf(confirmations.toArray(CompletableFuture[]::new))
                            .orTimeout(60, TimeUnit.SECONDS)
                            .join();
                }
            }                                                       // close(): flush everything, throw if any send failed
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("Published %,d events to %s in %,d ms%n", result.events().size(), kafka, elapsedMs);
        }
    }

    /**
     * Reads "--name value" from the arguments, or returns the default.
     */
    private static long argument(String[] args, String name, long defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(name)) {
                return Long.parseLong(args[i + 1]);
            }
        }
        return defaultValue;
    }

    private static String textArgument(String[] args, String name) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(name)) {
                return args[i + 1];
            }
        }
        return null;
    }

    private static boolean hasFlag(String[] args, String flag) {
        return Arrays.asList(args).contains(flag);
    }
}

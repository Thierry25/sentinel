package com.sentinel.simulator.patterns;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class AnswerKeyCsv {

    public static final String HEADER = "type,accountId,symbol,eventIds";

    private AnswerKeyCsv() {
    }

    public static void write(List<AnswerKeyEntry> entries, Path file) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        var lines = Stream.concat(
                Stream.of(HEADER),
                entries.stream().map(AnswerKeyCsv::toLine)
        ).toList();
        Files.write(file, lines);
    }

    static String toLine(AnswerKeyEntry entry) {
        return String.join(",", entry.type().name(), entry.accountId(), entry.symbol(),
                String.join("|", entry.eventIds()));
    }
}

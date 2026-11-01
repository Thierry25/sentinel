package com.sentinel.simulator.patterns;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerKeyCsvTest {

    @Test
    void writesAHeaderThenOneLinePerEntry(@TempDir Path folder) throws Exception {
        var file = folder.resolve("sim/answer-key.csv");             // the sim/ folder doesn't exist yet
        var entries = List.of(
                new AnswerKeyEntry(PatternType.WASH_TRADE, "A0123", "S17", List.of("E-0041021", "E-0041023")),
                new AnswerKeyEntry(PatternType.RESTRICTED_LIST, "A0007", "S03", List.of("E-0042001")));

        AnswerKeyCsv.write(entries, file);

        assertThat(Files.readAllLines(file)).containsExactly(
                "type,accountId,symbol,eventIds",
                "WASH_TRADE,A0123,S17,E-0041021|E-0041023",
                "RESTRICTED_LIST,A0007,S03,E-0042001");
    }
}
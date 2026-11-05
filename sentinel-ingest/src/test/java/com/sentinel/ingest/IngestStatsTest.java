package com.sentinel.ingest;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class IngestStatsTest {

    @Test
    void startsAtZero() {
        var stats = new IngestStats();

        assertThat(stats.total()).isZero();
        assertThat(stats.perPartition()).isEmpty();
        assertThat(stats.perThread()).isEmpty();
    }

    @Test
    void countsTheTotalAndEachPartition() {
        var stats = new IngestStats();

        stats.record(0);
        stats.record(0);
        stats.record(3);

        assertThat(stats.total()).isEqualTo(3);
        assertThat(stats.perPartition()).containsOnly(entry(0, 2L), entry(3, 1L));
    }

    @Test
    void aSnapshotDoesNotChangeAfterwards() {
        var stats = new IngestStats();
        stats.record(1);

        var snapshot = stats.perPartition();
        stats.record(1);

        assertThat(snapshot).containsOnly(entry(1, 1L));               // the copy taken earlier
        assertThat(stats.perPartition()).containsOnly(entry(1, 2L));   // the live value
    }

    @Test
    void countsWhichThreadHandledEachEvent() throws InterruptedException {
        var stats = new IngestStats();
        var a = new Thread(() -> {
            for (int i = 0; i < 300; i++) stats.record(0);
        }, "worker-a");
        var b = new Thread(() -> {
            for (int i = 0; i < 200; i++) stats.record(1);
        }, "worker-b");

        a.start();
        b.start();
        a.join();
        b.join();                                           // wait for both to finish

        assertThat(stats.perThread()).containsOnly(entry("worker-a", 300L), entry("worker-b", 200L));
    }

    @Test
    void losesNothingWhen16ThreadsRecordAtTheSameTime() {
        var stats = new IngestStats();
        int threads = 16, eventsEach = 10_000;
        var go = new CountDownLatch(1);                                // holds every thread at the start line

        try (var pool = Executors.newFixedThreadPool(threads)) {       // try-with-resources: close() waits for all tasks
            for (int t = 0; t < threads; t++) {
                int partition = t % 6;
                pool.submit(() -> {
                    go.await();                                        // all 16 start together → maximum contention
                    for (int i = 0; i < eventsEach; i++) stats.record(partition);
                    return null;
                });
            }
            go.countDown();                                            // GO
        }

        assertThat(stats.total()).isEqualTo(160_000);
        assertThat(stats.perPartition()).containsOnly(                 // threads 0–15 → partitions t % 6
                entry(0, 30_000L), entry(1, 30_000L), entry(2, 30_000L),
                entry(3, 30_000L), entry(4, 20_000L), entry(5, 20_000L));
        assertThat(stats.perThread()).hasSize(16);
        assertThat(stats.perThread().values().stream().mapToLong(Long::longValue).sum()).isEqualTo(160_000);
    }
}
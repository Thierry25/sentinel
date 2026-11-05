package com.sentinel.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Component
public class IngestStats {
    private static final Logger LOGGER = LoggerFactory.getLogger(IngestStats.class);
    private final ConcurrentMap<Integer, LongAdder> map = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> perThread = new ConcurrentHashMap<>();
    private final LongAdder total = new LongAdder();

    public long total() {
        return total.longValue();
    }

    public void record(int partition) {
        total.increment();
        long now = total.sum();
        map.computeIfAbsent(partition, key -> new LongAdder()).increment();

        var threadName = Thread.currentThread().getName();
        perThread.computeIfAbsent(threadName, key -> new LongAdder()).increment();

        if (now % 1000 == 0) {
            LOGGER.info("Ingested {} events so far", total);
        }
    }

    public Map<Integer, Long> perPartition() {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().sum()));
    }
}

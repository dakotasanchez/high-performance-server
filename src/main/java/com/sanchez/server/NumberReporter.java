package com.sanchez.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Concrete implementation of Reporter that reports current unique/duplicate number data from all clients.
 */
class NumberReporter extends Reporter {

    private final ConcurrentHashMap.KeySetView<Integer, Boolean> uniqueNumbers;
    private final AtomicLong duplicates;

    private long lastNumberCount;
    private long lastDuplicateCount;

    /**
     *
     * @param uniqueNumbers Thread-safe hash set (created from ConcurrentHashMap) to track duplicate data from all connections.
     * @param duplicates Thread-safe counter for counting the number of duplicates encountered.
     */
    NumberReporter(ConcurrentHashMap.KeySetView<Integer, Boolean> uniqueNumbers, AtomicLong duplicates) {
        this.uniqueNumbers = uniqueNumbers;
        this.duplicates = duplicates;
    }

    @Override
    void report() {
        long totalUniqueCount = uniqueNumbers.size();
        long totalDuplicateCount = duplicates.get();
        long newUniqueCount = totalUniqueCount - lastNumberCount;
        long newDuplicateCount = totalDuplicateCount - lastDuplicateCount;

        System.out.println("Received " + newUniqueCount + " unique numbers, " + newDuplicateCount +
                " duplicates. Unique total: " + totalUniqueCount);

        lastNumberCount = totalUniqueCount;
        lastDuplicateCount = totalDuplicateCount;
    }
}

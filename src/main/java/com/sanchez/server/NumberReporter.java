package com.sanchez.server;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Concrete implementation of Reporter that reports current unique/duplicate number data from all clients.
 */
class NumberReporter extends Reporter {

    private final NumberTracker processedNumbers;
    private final AtomicLong duplicates;

    private long lastNumberCount;
    private long lastDuplicateCount;

    /**
     *
     * @param processedNumbers Thread-safe tracker to track "seen" data from all connections.
     * @param duplicates Thread-safe counter for counting the number of duplicates encountered.
     */
    NumberReporter(final NumberTracker processedNumbers, final AtomicLong duplicates) {
        this.processedNumbers = processedNumbers;
        this.duplicates = duplicates;
    }

    @Override
    void report() {
        long totalUniqueCount = processedNumbers.getNumbersProcessed();
        long totalDuplicateCount = duplicates.get();
        long newUniqueCount = totalUniqueCount - lastNumberCount;
        long newDuplicateCount = totalDuplicateCount - lastDuplicateCount;

        System.out.println("Received " + newUniqueCount + " unique numbers, " + newDuplicateCount +
                " duplicates. Unique total: " + totalUniqueCount);

        lastNumberCount = totalUniqueCount;
        lastDuplicateCount = totalDuplicateCount;
    }
}

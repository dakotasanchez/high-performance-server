package com.sanchez.server;

import java.util.concurrent.atomic.AtomicLong;

class NumberReporter extends Reporter {

    private final NumberTracker processedNumbers;
    private final AtomicLong duplicates;

    private long lastNumberCount;
    private long lastDuplicateCount;

    /**
     * Concrete implementation of {@link Reporter} that reports current unique/duplicate number data from all clients.
     *
     * @param processedNumbers Thread-safe tracker to track "seen" data from all connections.
     * @param duplicates Thread-safe counter for counting the number of duplicates encountered.
     */
    public NumberReporter(final NumberTracker processedNumbers, final AtomicLong duplicates) {
        this.processedNumbers = processedNumbers;
        this.duplicates = duplicates;
    }

    @Override
    public void report() {
        final long totalUniqueCount = processedNumbers.getNumbersProcessed();
        final long totalDuplicateCount = duplicates.get();
        final long newUniqueCount = totalUniqueCount - lastNumberCount;
        final long newDuplicateCount = totalDuplicateCount - lastDuplicateCount;

        logger.info("Received " + newUniqueCount + " unique numbers, " + newDuplicateCount +
                " duplicates. Unique total: " + totalUniqueCount);

        lastNumberCount = totalUniqueCount;
        lastDuplicateCount = totalDuplicateCount;
    }
}

package com.sanchez.server;

import java.util.BitSet;

/**
 * Keeps track of seen/processed numbers.
 */
public class NumberTracker {

    private final BitSet processedNumbers;

    public NumberTracker(final BitSet processedNumbers) {
        this.processedNumbers = processedNumbers;
    }

    /**
     * Check if number has been processed.
     *
     * @param number Number to check
     */
    public synchronized boolean isNumberProcessed(final int number) {
        return processedNumbers.get(number);
    }

    /**
     * Thread-safe writing of processed numbers.
     *
     * @param number Number to set as "processed"
     */
    public synchronized void setNumberProcessed(final int number) {
        processedNumbers.set(number);
    }

    /**
     * Get number of numbers processed.
     *
     * @return number of numbers processed
     */
    public synchronized int getNumbersProcessed() {
        return processedNumbers.cardinality();
    }
}

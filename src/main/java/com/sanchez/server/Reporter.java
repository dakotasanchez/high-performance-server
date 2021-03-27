package com.sanchez.server;

import java.util.concurrent.TimeUnit;

/**
 * Abstract class for printing data at a 10-second interval
 */
abstract class Reporter implements Runnable {

    private volatile boolean stopped;

    Reporter() {}

    @Override
    public void run() {
        try {
            while (!stopped) {
                TimeUnit.SECONDS.sleep(10);
                report();
            }
        } catch (Exception e) {
            if (!(e instanceof InterruptedException)) {
                e.printStackTrace();
            }
        }
    }

    void stop() {
        stopped = true;
    }

    /**
     * Override to report data
     */
    abstract void report();
}

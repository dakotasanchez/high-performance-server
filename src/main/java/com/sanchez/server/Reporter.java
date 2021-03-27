package com.sanchez.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Abstract class for printing data at a 10-second interval
 */
abstract class Reporter implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(Reporter.class);

    private volatile boolean stopped;

    @Override
    public void run() {
        try {
            while (!stopped) {
                TimeUnit.SECONDS.sleep(10);
                report();
            }
        } catch (Exception e) {
            if (!(e instanceof InterruptedException)) {
                logger.error("Unexpected exception: ", e);
            }
        }
    }

    public void stop() {
        stopped = true;
    }

    /**
     * Override to report data.
     */
    public abstract void report();
}

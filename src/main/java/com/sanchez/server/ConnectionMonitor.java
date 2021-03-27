package com.sanchez.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class ConnectionMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionMonitor.class);

    private volatile boolean stopped;

    private final Server server;
    private final List<Future<Boolean>> results;

    /**
     * Class to monitor a list of Future<Boolean> results returned from client connections.
     *
     * @param server Reference to server to issue a shutdown.
     * @param results List of results from connection.
     *      TRUE: connection encountered terminate request from client
     *      FALSE: connection did not encounter terminate request
     */
    public ConnectionMonitor(final Server server, final List<Future<Boolean>> results) {
        this.server = server;
        this.results = results;
    }

    @Override
    public void run() {
        boolean encounteredClientTerminate = false;

        try {
            while (!stopped && !encounteredClientTerminate) {
                TimeUnit.SECONDS.sleep(1);

                final List<Future<Boolean>> toRemove = new ArrayList<>();

                synchronized (results) {
                    for (Future<Boolean> result : results) {
                        if (result.isDone()) {
                            if (result.get() == Boolean.TRUE) {
                                encounteredClientTerminate = true;
                            } else {
                                toRemove.add(result);
                            }
                        }
                    }
                    results.removeAll(toRemove);
                }
            }
        } catch (Exception e) {
            if (!(e instanceof InterruptedException)) {
                logger.error("Encountered unexpected exception: ", e);
            }
        }
        if (encounteredClientTerminate) {
            logger.info("ConnectionMonitor encountered client terminate request.");
        }
        server.shutdown();
    }

    public void stop() {
        stopped = true;
    }
}

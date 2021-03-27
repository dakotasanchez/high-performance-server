package com.sanchez.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Class to monitor a list of Future<Boolean> results returned from client connections.
 */
class ConnectionMonitor implements Runnable {

    private volatile boolean stopped;

    private final Server server;
    private final List<Future<Boolean>> results;

    /**
     *
     * @param server Reference to server to issue a shutdown.
     * @param results List of results from connection.
     *      TRUE: connection encountered terminate request from client
     *      FALSE: connection did not encounter terminate request
     */
    ConnectionMonitor(Server server, List<Future<Boolean>> results) {
        this.server = server;
        this.results = results;
    }

    @Override
    public void run() {
        boolean encounteredClientTerminate = false;

        try {
            while (!stopped && !encounteredClientTerminate) {
                TimeUnit.SECONDS.sleep(1);

                List<Future<Boolean>> toRemove = new ArrayList<>();

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
                e.printStackTrace();
            }
        }
        if (encounteredClientTerminate) {
            System.out.println("ConnectionMonitor encountered client terminate request.");
        }
        server.shutdown();
    }

    void stop() {
        stopped = true;
    }
}

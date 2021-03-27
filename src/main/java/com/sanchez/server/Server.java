package com.sanchez.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

class Server implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    // Accommodate LogWriter, ConnectionMonitor, and Reporter threads
    private static final int EXTRA_THREADS = 3;
    // Default producer-consumer data queue capacity
    private static final int QUEUE_CAPACITY = 100_000;
    // Maximum allowed number
    private static final int MAX_NUMBER = 1_000_000_000;
    // Length of incoming data lines (9 digit numbers)
    private static final int DATA_LINE_LENGTH = 9;

    private final List<Future<Boolean>> results = new ArrayList<>();
    private final NumberTracker processedNumbers = new NumberTracker(new BitSet(MAX_NUMBER));
    private final AtomicLong duplicates = new AtomicLong();
    private final BlockingQueue<Integer> dataQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private final String logFilePath;
    private final ExecutorService executorService;
    private final int port;

    private ServerSocket serverSocket;
    private ConnectionMonitor connectionMonitor;
    private LogWriter<Integer> logWriter;
    private Reporter reporter;

    /**
     * Server class to handle 5 concurrent connections (plus ConnectionMonitor, LogWriter, and Reporter threads),
     * and spawn worker threads for each concurrent client.
     *
     * @param port Port to listen on.
     * @param connections Number of concurrent connections to allow.
     * @param logFilePath Log file path to be written to.
     */
    public Server(int port, int connections, String logFilePath) {
        this.port = port;
        this.logFilePath = logFilePath;
        this.executorService = Executors.newFixedThreadPool(connections + EXTRA_THREADS);

    }

    /**
     * In case the Server needs to be ran on a separate thread (testing purposes).
     */
    @Override
    public void run() {
        startServer();
    }

    /**
     * Spin up threads (ConnectionMonitor, LogWriter, Reporter), then block on the server socket waiting for incoming
     * connections. Submit connections to thread pool and pass connection results to ConnectionMonitor.
     */
    public void startServer() {
        logger.info("Starting server ...");
        try {
            serverSocket = new ServerSocket(port);
            connectionMonitor = new ConnectionMonitor(this, results);
            reporter = new NumberReporter(processedNumbers, duplicates);
            logWriter = new LogWriter<>(logFilePath, dataQueue);

            executorService.execute(connectionMonitor);
            executorService.execute(logWriter);
            executorService.execute(reporter);

            while (true) {
                final Connection workerThread = new NumberConnection(serverSocket.accept(),
                        processedNumbers,
                        duplicates,
                        dataQueue,
                        DATA_LINE_LENGTH);

                final Future<Boolean> result = executorService.submit(workerThread);

                synchronized (results) {
                    results.add(result);
                }
            }
        } catch (Exception e) {
            logger.info("Shutting down server...");
            shutdownExecutorService();
            logger.info("Final unique count: " + processedNumbers.getNumbersProcessed());
        }
    }

    /**
     * Send stop flag to threads, and then interrupt server socket to begin ExecutorService shutdown.
     */
    void shutdown() {
        try {
            connectionMonitor.stop();
            logWriter.stop();
            reporter.stop();
            serverSocket.close();
        } catch (IOException e) {
            logger.error("Unexpected exception: ", e);
        }
    }

    /**
     * Shutdown ExecutorService gracefully.
     */
    private void shutdownExecutorService() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Unexpected exception: ", e);
        } finally {
            if (!executorService.isTerminated()) {
                logger.info("Forcing shutdown of Server executor service.");
                executorService.shutdownNow();
            }
            logger.info("Server has finished.");
        }
    }
}

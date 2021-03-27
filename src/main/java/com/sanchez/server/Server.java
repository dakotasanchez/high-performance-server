package com.sanchez.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server class to handle 5 concurrent connections (plus ConnectionMonitor, LogWriter, and Reporter threads),
 * and spawn worker threads for each concurrent client.
 */
class Server implements Runnable {

    // Accommodate LogWriter, ConnectionMonitor, and Reporter threads
    private static final int EXTRA_THREADS = 3;
    // Default producer-consumer data queue capacity
    private static final int QUEUE_CAPACITY = 100_000;
    // Length of incoming data lines (9 digit numbers)
    private static final int DATA_LINE_LENGTH = 9;

    private final List<Future<Boolean>> results = new ArrayList<>();
    private final ConcurrentHashMap.KeySetView<Integer, Boolean> writtenNumbers = ConcurrentHashMap.newKeySet();
    private final AtomicLong duplicates = new AtomicLong();
    private final BlockingQueue<Integer> dataQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private final String logFilePath;
    private final ExecutorService executorService;
    private final int port;

    private ServerSocket serverSocket;
    private ConnectionMonitor connectionMonitor;
    private LogWriter logWriter;
    private Reporter reporter;

    /**
     *
     * @param port Port to listen on.
     * @param connections Number of concurrent connections to allow.
     * @param logFilePath Log file path to be written to.
     */
    Server(int port, int connections, String logFilePath) {
        this.port = port;
        this.logFilePath = logFilePath;
        this.executorService = Executors.newFixedThreadPool(connections + EXTRA_THREADS);
    }

    /**
     * In case the Server needs to be ran on a separate thread (like the tests do)
     */
    @Override
    public void run() {
        startServer();
    }

    /**
     * Spin up threads (ConnectionMonitor, LogWriter, Reporter), then block on the server socket waiting for incoming
     * connections. Submit connections to thread pool and pass connection results to ConnectionMonitor.
     */
    void startServer() {
        System.out.println("Starting up server ....");
        try {
            serverSocket = new ServerSocket(port);

            connectionMonitor = new ConnectionMonitor(this, results);
            reporter = new NumberReporter(writtenNumbers, duplicates);
            logWriter = new LogWriter<>(logFilePath, dataQueue);

            executorService.execute(connectionMonitor);
            executorService.execute(logWriter);
            executorService.execute(reporter);

            while (true) {
                Connection workerThread =
                        new NumberConnection(serverSocket.accept(),
                                writtenNumbers,
                                duplicates,
                                dataQueue,
                                DATA_LINE_LENGTH);

                Future<Boolean> result = executorService.submit(workerThread);
                synchronized (results) {
                    results.add(result);
                }
            }
        } catch (Exception e) {
            System.out.println("Shutting down server...");
            shutdownExecutorService();
            System.out.println("Final unique count: " + writtenNumbers.size());
        }
    }

    /**
     * Send stop flag to threads, and then interrupt server socket to begin ExecutorService shutdown
     */
    void shutdown() {
        try {
            connectionMonitor.stop();
            logWriter.stop();
            reporter.stop();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shutdown ExecutorService gracefully
     */
    private void shutdownExecutorService() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (!executorService.isTerminated()) {
                System.out.println("Forcing shutdown of Server executor service.");
            }
            executorService.shutdownNow();
            System.out.println("Server is finished.");
        }
    }
}

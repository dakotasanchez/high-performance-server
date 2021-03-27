package com.sanchez.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.fail;

public class MainTest {

    private static final String HOST = "localhost";
    private static final int HOST_PORT = 3000;
    private static final String logFilePath = "numbers.log";

    private ExecutorService serverExecutor;
    private Server server;

    @Before
    public void setUp() {
        serverExecutor = Executors.newSingleThreadExecutor();
        try {
            final int connections = 5;
            final String logFile = "numbers.log";

            server = new Server(HOST_PORT, connections, logFile);
            serverExecutor.submit(server);
            TimeUnit.SECONDS.sleep(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        stopExecutorService(serverExecutor);
    }

    @Test
    public void testDuplicates() throws InterruptedException, IOException {
        final ExecutorService duplicateExecutor = Executors.newSingleThreadExecutor();
        duplicateExecutor.submit(new DuplicateNumberClient(HOST, HOST_PORT));
        TimeUnit.SECONDS.sleep(5);
        stopExecutorService(duplicateExecutor);

        final File file = new File(logFilePath);
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (!line.equals("999999999")) {
                fail("Incorrect file content");
            }
            line = reader.readLine();
            if (!line.equals("888888888")) {
                fail("Incorrect file content");
            }
            if (reader.readLine() != null) {
                fail("Incorrect file content");
            }
        }
    }

    @Test
    public void testMoreThan5Clients() throws InterruptedException {
        final int threads = 6;
        final ExecutorService randomExecutor = Executors.newFixedThreadPool(threads);
        final List<RandomNumberClient> clients = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            clients.add(new RandomNumberClient(HOST, HOST_PORT, 500_000));
        }
        randomExecutor.invokeAll(clients);
        stopExecutorService(randomExecutor);

        final File file = new File(logFilePath);
        if (!file.exists()) {
            fail("Log file was not created");
        }
    }

    @Test
    public void testThroughput() throws InterruptedException {
        final int threads = 5;
        final ExecutorService randomExecutor = Executors.newFixedThreadPool(threads);
        final List<RandomNumberClient> clients = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            clients.add(new RandomNumberClient(HOST, HOST_PORT, 5_000_000));
        }
        randomExecutor.invokeAll(clients);
        TimeUnit.SECONDS.sleep(5);
        stopExecutorService(randomExecutor);

        final File file = new File(logFilePath);
        if (!file.exists()) {
            fail("Log file was not created");
        }
    }

    @Test
    public void testTerminate() throws InterruptedException {
        final ExecutorService terminateExecutor = Executors.newSingleThreadExecutor();
        terminateExecutor.submit(new TerminateClient(HOST, HOST_PORT));
        TimeUnit.SECONDS.sleep(5);
        stopExecutorService(terminateExecutor);
    }

    private void stopExecutorService(ExecutorService executorService) {
        try {
            executorService.shutdown();
            server.shutdown();
            executorService.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdownNow();
        }
    }
}
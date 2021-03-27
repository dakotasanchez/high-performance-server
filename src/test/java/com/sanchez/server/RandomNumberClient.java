package com.sanchez.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

class RandomNumberClient implements Callable<Boolean> {

    private static final int MAX_RANDOM = 999_999_999;

    private final String host;
    private final int port;
    private final long numbersToWrite;
    private long numbersWritten;

    public RandomNumberClient(final String host, final int port, final long numbersToWrite) {
        this.host = host;
        this.port = port;
        this.numbersToWrite = numbersToWrite;
    }

    @Override
    public Boolean call() {
        try (
                final Socket socket = new Socket(host, port);
                final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            while (numbersWritten < numbersToWrite) {
                final int random = ThreadLocalRandom.current().nextInt(MAX_RANDOM + 1);
                writer.println(String.format("%09d", random));
                numbersWritten++;
            }

            System.out.println("Client " + Thread.currentThread().getName() + " finished.");
        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}

package com.sanchez.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

class RandomNumberClient implements Callable<Boolean> {

    private static final int MAX_RANDOM = 999_999_999;

    private String host;
    private int port;
    private long numbersWritten;
    private long numbersToWrite;

    RandomNumberClient(String host, int port, long numbersToWrite) {
        this.host = host;
        this.port = port;
        this.numbersToWrite = numbersToWrite;
    }

    @Override
    public Boolean call() {
        try (
                Socket socket = new Socket(host, port);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            while (numbersWritten < numbersToWrite) {
                int random = ThreadLocalRandom.current().nextInt(MAX_RANDOM + 1);
                String toWrite = String.format("%09d", random);
                writer.println(toWrite);
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

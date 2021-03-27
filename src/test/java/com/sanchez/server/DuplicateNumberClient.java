package com.sanchez.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

public class DuplicateNumberClient implements Callable<Boolean> {

    private final String host;
    private final int port;

    public DuplicateNumberClient(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Boolean call() {
        try (
                final Socket socket = new Socket(host, port);
                final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            writer.println("999999999");
            writer.println("888888888");
            writer.println("999999999");

            System.out.println("Client " + Thread.currentThread().getName() + " finished.");
        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}

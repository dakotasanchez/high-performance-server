package com.sanchez.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Abstract class for basic connection handling.
 */
abstract class Connection implements Callable<Boolean> {

    private final Socket socket;

    /**
     *
     * @param socket Connection to client returned from serverSocket.accept() method.
     */
    Connection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public Boolean call() throws Exception {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("terminate")) {
                    terminateConnection();
                    return Boolean.TRUE;
                }
                if (validInput(line)) {
                    processInput(line);
                } else {
                    System.err.println("Invalid input: " + line);
                    terminateConnection();
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.FALSE;
    }

    private void terminateConnection() throws IOException {
        System.out.println("Terminating connection: " + Thread.currentThread().getName());
        socket.close();
    }

    /**
     * Override to validate client data.
     *
     * @param input
     *      String input received from client
     * @return
     *      Whether or not this input from the client is for you
     */
    abstract boolean validInput(String input);

    /**
     * Override to process client data.
     *
     * @param input
     *      String input received from client
     * @throws InterruptedException
     *      In case processing is interrupted
     */
    abstract void processInput(String input) throws InterruptedException;
}

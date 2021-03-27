package com.sanchez.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Abstract class for basic connection handling.
 */
abstract class Connection implements Callable<Boolean> {

    protected static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private final Socket socket;

    public Connection(final Socket socket) {
        this.socket = socket;
    }

    @Override
    public Boolean call() throws Exception {

        try (final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("terminate")) {
                    terminateConnection();
                    return Boolean.TRUE;
                }
                if (validInput(line)) {
                    processInput(line);
                } else {
                    logger.error("Invalid input: " + line);
                    terminateConnection();
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.FALSE;
    }

    private void terminateConnection() throws IOException {
        logger.info("Terminating connection: " + Thread.currentThread().getName());
        socket.close();
    }

    /**
     * Override to validate client data.
     *
     * @param input String input received from client
     * @return whether or not this input from the client is for you
     */
    public abstract boolean validInput(final String input);

    /**
     * Override to process client data.
     *
     * @param input String input received from client
     * @throws InterruptedException if processing is interrupted
     */
    public abstract void processInput(final String input) throws InterruptedException;
}

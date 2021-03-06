package com.sanchez.server;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

class NumberConnection extends Connection {

    private final NumberTracker processedNumbers;
    private final AtomicLong duplicates;
    private final BlockingQueue<Integer> dataQueue;
    private final int dataLineLength;

    /**
     * Concrete implementation of {@link Connection} that validates/parses Numbers from a client.
     *
     * @param socket Connection to client.
     * @param processedNumbers Thread-safe tracker to track "seen" data from all connections.
     * @param duplicates Thread-safe counter for counting the number of duplicates encountered.
     * @param dataQueue Thread-safe queue to hold data to be written.
     * @param dataLineLength Length of lines to be expected from clients. Invalid lengths will terminate this connection.
     */
    public NumberConnection(final Socket socket,
                     final NumberTracker processedNumbers,
                     final AtomicLong duplicates,
                     final BlockingQueue<Integer> dataQueue,
                     final int dataLineLength) {
        super(socket);
        this.processedNumbers = processedNumbers;
        this.duplicates = duplicates;
        this.dataQueue = dataQueue;
        this.dataLineLength = dataLineLength;
    }

    @Override
    public boolean validInput(final String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        }
        return input.length() == dataLineLength;
    }

    @Override
    public void processInput(final String input) throws InterruptedException {
        final int parsedInt = Integer.parseInt(input);

        if (processedNumbers.isNumberProcessed(parsedInt)) {
            duplicates.incrementAndGet();
        } else {
            dataQueue.put(parsedInt);
            processedNumbers.setNumberProcessed(parsedInt);
        }
    }
}

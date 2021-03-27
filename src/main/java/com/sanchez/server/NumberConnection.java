package com.sanchez.server;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Concrete implementation of Connection that validates/parses Numbers from a client.
 */
class NumberConnection extends Connection {

    private final ConcurrentHashMap.KeySetView<Integer, Boolean> writtenData;
    private final AtomicLong duplicates;
    private final BlockingQueue<Integer> dataQueue;
    private final int dataLineLength;

    /**
     *
     * @param socket Connection to client.
     * @param writtenData Thread-safe hash set (created from ConcurrentHashMap) to track duplicate data from all connections.
     * @param duplicates Thread-safe counter for counting the number of duplicates encountered.
     * @param dataQueue Thread-safe queue to hold data to be written.
     * @param dataLineLength Length of lines to be expected from clients. Invalid lengths will terminate this connection.
     */
    NumberConnection(Socket socket,
                      ConcurrentHashMap.KeySetView<Integer, Boolean> writtenData,
                      AtomicLong duplicates,
                      BlockingQueue<Integer> dataQueue,
                      int dataLineLength) {
        super(socket);
        this.writtenData = writtenData;
        this.duplicates = duplicates;
        this.dataQueue = dataQueue;
        this.dataLineLength = dataLineLength;
    }

    @Override
    boolean validInput(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        }
        return input.length() == dataLineLength;
    }

    @Override
    void processInput(String input) throws InterruptedException {
        Integer parsedInt = Integer.parseInt(input);

        if (writtenData.contains(parsedInt)) {
            duplicates.incrementAndGet();
        } else {
            dataQueue.put(parsedInt);
            writtenData.add(parsedInt);
        }
    }
}
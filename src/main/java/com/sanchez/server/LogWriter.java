package com.sanchez.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @param <T> Type of data inside thread-safe queue
 */
class LogWriter<T> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LogWriter.class);

    private volatile boolean stopped;

    private final String logFilePath;
    private final BlockingQueue<T> dataQueue;

    /**
     * Generic class to write data from a Queue separated by newlines to a file.
     *
     * @param logFilePath File path to be written to.
     * @param dataQueue Thread-safe queue to drain data from and write to file.
     */
    public LogWriter(final String logFilePath, final BlockingQueue<T> dataQueue) {
        this.logFilePath = logFilePath;
        this.dataQueue = dataQueue;
    }

    @Override
    public void run() {
        try (final BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFilePath), StandardCharsets.UTF_8))) {
            final List<T> drain = new ArrayList<>();
            while (!stopped) {
                dataQueue.drainTo(drain);
                for (final T data : drain) {
                    writer.write(data.toString());
                    writer.newLine();
                }
                drain.clear();
                writer.flush();
            }
        } catch (Exception e) {
            logger.error("Error while draining data to disk: ", e);
        }
    }

    public void stop() {
        stopped = true;
    }
}

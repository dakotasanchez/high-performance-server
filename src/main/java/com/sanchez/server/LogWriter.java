package com.sanchez.server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Generic class to write data from a Queue separated by newlines to a file.
 *
 * @param <T> Type of data inside thread-safe queue
 */
class LogWriter<T> implements Runnable {

    private volatile boolean stopped;

    private final String logFilePath;
    private final BlockingQueue<T> dataQueue;

    /**
     *
     * @param logFilePath File path to be written to.
     * @param dataQueue Thread-safe queue to drain data from and write to file.
     */
    LogWriter(String logFilePath, BlockingQueue<T> dataQueue) {
        this.logFilePath = logFilePath;
        this.dataQueue = dataQueue;
    }

    @Override
    public void run() {
        try (BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFilePath), "UTF-8"))) {
            List<T> drain = new ArrayList<>();
            while (!stopped) {
                dataQueue.drainTo(drain);
                for (T data : drain) {
                    writer.write(data.toString());
                    writer.newLine();
                }
                drain.clear();
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stop() {
        stopped = true;
    }
}

package com.sanchez.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int PORT = 3000;
    private static final int CONNECTIONS = 5;
    private static final String LOG_FILE = "numbers.log";

    public static void main(String[] args) {
        try {
            Server server = new Server(PORT, CONNECTIONS, LOG_FILE);
            server.startServer();
        } catch (Exception e) {
            logger.error("Failure in Main: ", e);
        }
    }
}
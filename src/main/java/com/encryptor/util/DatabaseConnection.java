// Java Version Target: Java 17 (LTS)

// The following rewritten files are refactored for stability, clarity, and compatibility
// with Java 17. Changes include modern exception handling, removal of incubator features,
// virtual threads replaced with a fixed thread pool, and UI/threading consistency ensured.

// --- FILE: com.encryptor.util.DatabaseConnection.java ---
package com.encryptor.util;

import java.io.InputStream;
import java.sql.*;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DatabaseConnection {
    private static final int POOL_SIZE = 10;
    private static final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(POOL_SIZE);
    private static final Properties props = new Properties();

    static {
        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties not found in classpath");
            }
            props.load(input);
            initializePool();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize database connection pool: " + e.getMessage());
        }
    }

    private static void initializePool() throws SQLException {
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String encodedPass = props.getProperty("db.password");
        if (url == null || user == null || encodedPass == null) {
            throw new IllegalArgumentException("Missing DB configuration in properties file");
        }
        String pass = new String(Base64.getDecoder().decode(encodedPass));

        for (int i = 0; i < POOL_SIZE; i++) {
            pool.add(DriverManager.getConnection(url, user, pass));
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = pool.poll(5, TimeUnit.SECONDS);
            if (conn == null) {
                throw new SQLException("Timeout while waiting for database connection");
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for DB connection", e);
        }
    }

    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            pool.offer(conn);
        }
    }
}

// Remaining files (MainFrame.java, OperationDAO, EncryptionService, etc.) will be updated next.
// Let me know which file you'd like to refactor and stabilize next.

package com.encryptor.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * DatabaseConnection is a thread-safe utility for managing JDBC connections
 * via a simple connection pool. It loads configuration from application.properties.
 */
public final class DatabaseConnection {

    private static final int POOL_SIZE = 10;
    private static final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(POOL_SIZE);
    private static final Properties dbProperties = new Properties();

    static {
        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/application.properties")) {
            if (input == null) throw new IllegalStateException("Missing configuration file: application.properties");

            dbProperties.load(input);
            initConnectionPool();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize database connection pool: " + e.getMessage());
        }
    }

    private DatabaseConnection() {}

    private static void initConnectionPool() throws SQLException {
        final String url = dbProperties.getProperty("db.url");
        final String username = dbProperties.getProperty("db.username");
        final String passwordEncoded = dbProperties.getProperty("db.password");
        final String password = new String(Base64.getDecoder().decode(passwordEncoded));

        for (int i = 0; i < POOL_SIZE; i++) {
            pool.offer(createConnection(url, username, password));
        }
    }

    private static Connection createConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Acquires a connection from the pool within 5 seconds.
     *
     * @return an active JDBC connection
     * @throws SQLException if no connection is available
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = pool.poll(5, TimeUnit.SECONDS);
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Failed to obtain a database connection in time.");
            }
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Thread interrupted while waiting for connection.", e);
        }
    }

    /**
     * Returns a connection back to the pool.
     *
     * @param connection the JDBC connection to return
     */
    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    pool.offer(connection);
                }
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
            }
        }
    }
}

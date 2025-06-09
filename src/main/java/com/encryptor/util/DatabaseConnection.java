package com.encryptor.util;

import java.io.InputStream;
import java.sql.*;
import java.util.Base64;
import java.util.Properties;

/**
 * Simplified database connection utility.
 * Handles JDBC connections with proper error handling and resource management.
 */
public final class DatabaseConnection {

    private static final Properties properties = new Properties();
    private static boolean initialized = false;

    static {
        try {
            loadProperties();
            initialized = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize database configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DatabaseConnection() {
        // Utility class - prevent instantiation
    }

    private static void loadProperties() throws Exception {
        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Missing application.properties in resources folder.");
            }
            properties.load(input);
        }
    }

    /**
     * Gets a database connection using properties configuration.
     * @return Connection to the database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Database configuration not initialized properly.");
        }

        try {
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.username");
            String encodedPassword = properties.getProperty("db.password");

            if (url == null || user == null || encodedPassword == null) {
                throw new SQLException("Database configuration incomplete. Check application.properties file.");
            }

            String password = new String(Base64.getDecoder().decode(encodedPassword));

            // Ensure MySQL driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connection = DriverManager.getConnection(url, user, password);

            // Test the connection
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Failed to establish database connection.");
            }

            return connection;

        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. Check your dependencies.", e);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Invalid base64 encoded password in configuration.", e);
        } catch (Exception e) {
            throw new SQLException("Database connection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Safely closes a database connection.
     * @param connection the connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Tests if database connection is available.
     * @return true if connection can be established
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}

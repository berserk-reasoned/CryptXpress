package com.encryptor.util;

import java.sql.*;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.*;

public class DatabaseConnection {
    private static final int POOL_SIZE = 10;
    private static final BlockingQueue<Connection> pool = new LinkedBlockingQueue<>(POOL_SIZE);
    private static final Properties props = new Properties();

    static {
        try (var input = DatabaseConnection.class.getResourceAsStream("/application.properties")) {
            props.load(input);
            initializePool();
        } catch (Exception e) {
            throw new RuntimeException("Initialization failed", e);
        }
    }

    private static void initializePool() throws SQLException {
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String pass = new String(Base64.getDecoder().decode(props.getProperty("db.password")));

        for (int i = 0; i < POOL_SIZE; i++) {
            pool.add(DriverManager.getConnection(url, user, pass));
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = pool.poll(5, TimeUnit.SECONDS);
            if (conn == null) throw new SQLException("Connection timeout");
            return conn;
        } catch (InterruptedException e) {
            throw new SQLException("Connection interrupted", e);
        }
    }

    public static void releaseConnection(Connection conn) {
        if (conn != null) pool.offer(conn);
    }
}

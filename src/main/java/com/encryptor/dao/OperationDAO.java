package com.encryptor.dao;

import com.encryptor.model.OperationRecord;
import com.encryptor.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for logging and retrieving operation history.
 * Handles database operations with proper error handling and resource management.
 */
public class OperationDAO {

    /**
     * Logs an operation to the database.
     * @param record the operation record to log
     * @return true if logging was successful, false otherwise
     */
    public boolean logOperation(OperationRecord record) {
        if (record == null || !record.isValid()) {
            System.err.println("Invalid operation record provided for logging.");
            return false;
        }

        String query = """
            INSERT INTO operation_history
            (file_name, original_path, operation_type, encryption_method, timestamp, file_size, status, error_message)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, record.fileName());
                stmt.setString(2, record.originalPath());
                stmt.setString(3, record.operationType());
                stmt.setString(4, record.encryptionMethod());
                stmt.setTimestamp(5, Timestamp.valueOf(record.timestamp()));
                stmt.setLong(6, record.fileSize());
                stmt.setString(7, record.status());
                stmt.setString(8, record.errorMessage());

                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            System.err.println("Failed to log operation to database: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Retrieves recent operations from the database.
     * @param limit maximum number of operations to retrieve
     * @return list of operation records
     */
    public List<OperationRecord> getRecentOperations(int limit) {
        List<OperationRecord> operations = new ArrayList<>();

        if (limit <= 0) {
            limit = 10; // Default limit
        }

        String query = """
            SELECT file_name, original_path, operation_type, encryption_method,
                   timestamp, file_size, status, error_message
            FROM operation_history
            ORDER BY timestamp DESC
            LIMIT ?
        """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, limit);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        try {
                            OperationRecord record = new OperationRecord(
                                    rs.getString("file_name"),
                                    rs.getString("original_path"),
                                    rs.getString("operation_type"),
                                    rs.getString("encryption_method"),
                                    rs.getLong("file_size"),
                                    rs.getString("status"),
                                    rs.getString("error_message"),
                                    rs.getTimestamp("timestamp").toLocalDateTime()
                            );
                            operations.add(record);
                        } catch (Exception e) {
                            System.err.println("Error parsing operation record: " + e.getMessage());
                            // Continue processing other records
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to fetch operation history from database: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return operations;
    }

    /**
     * Gets the total count of operations in the database.
     * @return total number of operations
     */
    public int getTotalOperationsCount() {
        String query = "SELECT COUNT(*) FROM operation_history";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to get operations count: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return 0;
    }

    /**
     * Clears all operation history (for testing purposes).
     * @return true if successful
     */
    public boolean clearHistory() {
        String query = "DELETE FROM operation_history";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Failed to clear operation history: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Tests if the database connection and table are accessible.
     * @return true if database is accessible
     */
    public boolean testDatabaseAccess() {
        String query = "SELECT 1 FROM operation_history LIMIT 1";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.executeQuery();
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Database access test failed: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }
}

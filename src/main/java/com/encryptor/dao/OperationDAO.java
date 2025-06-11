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
     * Saves an operation to the database.
     * @param record the operation record to save
     * @throws SQLException if database operation fails
     */
    public void saveOperation(OperationRecord record) throws SQLException {
        if (record == null || !record.isValid()) {
            throw new IllegalArgumentException("Invalid operation record provided for saving.");
        }

        String query = """
            INSERT INTO operation_history
            (operation, method, input_file, output_file, timestamp, success, error_message)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, record.operationType());
                stmt.setString(2, record.encryptionMethod());
                stmt.setString(3, record.originalPath());
                stmt.setString(4, record.outputFile()); // Need to add this field to OperationRecord
                stmt.setTimestamp(5, Timestamp.valueOf(record.timestamp()));
                stmt.setBoolean(6, "SUCCESS".equals(record.status()));
                stmt.setString(7, record.errorMessage());

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Failed to save operation to database: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Gets all operations from the database.
     * @return list of all operation records
     * @throws SQLException if database operation fails
     */
    public List<OperationRecord> getAllOperations() throws SQLException {
        List<OperationRecord> operations = new ArrayList<>();

        String query = """
            SELECT operation, method, input_file, output_file, timestamp, success, error_message
            FROM operation_history
            ORDER BY timestamp DESC
        """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    try {
                        // Create record using the constructor that matches the database schema
                        OperationRecord record = new OperationRecord(
                                extractFileName(rs.getString("input_file")),
                                rs.getString("input_file"),
                                rs.getString("operation"),
                                rs.getString("method"),
                                0L, // file size - may need to be calculated or stored separately
                                rs.getBoolean("success") ? "SUCCESS" : "FAILED",
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

        } catch (SQLException e) {
            System.err.println("Failed to fetch operation history from database: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return operations;
    }

    /**
     * Logs an operation to the database.
     * @param record the operation record to log
     * @return true if logging was successful, false otherwise
     */
    public boolean logOperation(OperationRecord record) {
        try {
            saveOperation(record);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to log operation: " + e.getMessage());
            return false;
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
            SELECT operation, method, input_file, output_file, timestamp, success, error_message
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
                                    extractFileName(rs.getString("input_file")),
                                    rs.getString("input_file"),
                                    rs.getString("operation"),
                                    rs.getString("method"),
                                    0L, // file size
                                    rs.getBoolean("success") ? "SUCCESS" : "FAILED",
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
     * Extracts filename from full path
     */
    private String extractFileName(String fullPath) {
        if (fullPath == null) return null;
        int lastSeparator = Math.max(fullPath.lastIndexOf('/'), fullPath.lastIndexOf('\\'));
        return lastSeparator >= 0 ? fullPath.substring(lastSeparator + 1) : fullPath;
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

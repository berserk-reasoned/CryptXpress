package com.encryptor.dao;

import com.encryptor.model.OperationRecord;
import com.encryptor.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for logging and retrieving operation history.
 * Securely interacts with the MySQL database using prepared statements.
 */
public class OperationDAO {

    public void logOperation(OperationRecord record) {
        String query = """
            INSERT INTO operation_history
            (file_name, original_path, operation_type, encryption_method, timestamp, file_size, status, error_message)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, record.fileName());
            stmt.setString(2, record.originalPath());
            stmt.setString(3, record.operationType());
            stmt.setString(4, record.encryptionMethod());
            stmt.setTimestamp(5, Timestamp.valueOf(record.timestamp()));
            stmt.setLong(6, record.fileSize());
            stmt.setString(7, record.status());
            stmt.setString(8, record.errorMessage());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log operation: " + e.getMessage());
        }
    }

    public List<OperationRecord> getRecentOperations(int limit) {
        List<OperationRecord> operations = new ArrayList<>();

        String query = """
            SELECT file_name, original_path, operation_type, encryption_method,
                   timestamp, file_size, status, error_message
            FROM operation_history
            ORDER BY timestamp DESC
            LIMIT ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch operation history: " + e.getMessage());
        }

        return operations;
    }
}

package com.encryptor.dao;

import com.encryptor.model.OperationRecord;
import com.encryptor.util.DatabaseConnection;
import java.sql.*;

public class OperationDAO {
    private static final String INSERT_SQL = """
        INSERT INTO operation_history 
        (file_name, original_path, operation_type, encryption_method, 
         file_size, status, error_message)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    public void logOperation(OperationRecord record) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
                stmt.setString(1, record.fileName());
                stmt.setString(2, record.originalPath());
                stmt.setString(3, record.operationType());
                stmt.setString(4, record.encryptionMethod());
                stmt.setLong(5, record.fileSize());
                stmt.setString(6, record.status());
                stmt.setString(7, record.errorMessage());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } finally {
            DatabaseConnection.releaseConnection(conn);
        }
    }
}

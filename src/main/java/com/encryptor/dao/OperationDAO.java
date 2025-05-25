package com.encryptor.dao;

import com.encryptor.model.OperationRecord;
import com.encryptor.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OperationDAO {
    
    public boolean insertOperation(OperationRecord record) {
        String sql = "INSERT INTO operation_history (file_name, original_path, operation_type, " +
                    "encryption_method, file_size, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getFileName());
            pstmt.setString(2, record.getOriginalPath());
            pstmt.setString(3, record.getOperationType());
            pstmt.setString(4, record.getEncryptionMethod());
            pstmt.setLong(5, record.getFileSize());
            pstmt.setString(6, record.getStatus());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<OperationRecord> getAllOperations() {
        List<OperationRecord> operations = new ArrayList<>();
        String sql = "SELECT * FROM operation_history ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                OperationRecord record = new OperationRecord();
                record.setId(rs.getInt("id"));
                record.setFileName(rs.getString("file_name"));
                record.setOriginalPath(rs.getString("original_path"));
                record.setOperationType(rs.getString("operation_type"));
                record.setEncryptionMethod(rs.getString("encryption_method"));
                record.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                record.setFileSize(rs.getLong("file_size"));
                record.setStatus(rs.getString("status"));
                operations.add(record);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return operations;
    }
}

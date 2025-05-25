package com.encryptor.dao;

import com.encryptor.model.OperationRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CryptXpress Operation Data Access Object
 * Handles database operations for operation history
 */
public class OperationDAO {
    private static final String DB_URL = "jdbc:sqlite:cryptxpress_history.db";
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS operations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            file_name TEXT NOT NULL,
            operation_type TEXT NOT NULL,
            method TEXT NOT NULL,
            status TEXT NOT NULL,
            timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
        )
        """;

    public OperationDAO() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. History tracking will be disabled.");
            System.err.println("To enable history tracking, add sqlite-jdbc dependency to your project.");
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    /**
     * Initialize the database and create tables if they don't exist
     */
    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(CREATE_TABLE_SQL);
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    /**
     * Get database connection
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Insert a new operation record
     */
    public boolean insertOperation(OperationRecord record) {
        if (record == null) {
            return false;
        }

        String sql = """
            INSERT INTO operations (file_name, operation_type, method, status, timestamp)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, record.getFileName());
            pstmt.setString(2, record.getOperationType());
            pstmt.setString(3, record.getMethod());
            pstmt.setString(4, record.getStatus());
            pstmt.setTimestamp(5, Timestamp.valueOf(record.getTimestamp()));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Failed to insert operation record: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all operation records ordered by timestamp (newest first)
     */
    public List<OperationRecord> getAllOperations() {
        List<OperationRecord> operations = new ArrayList<>();
        String sql = "SELECT * FROM operations ORDER BY timestamp DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                operations.add(createOperationFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to retrieve operations: " + e.getMessage());
        }

        return operations;
    }

    /**
     * Get operation records by status
     */
    public List<OperationRecord> getOperationsByStatus(String status) {
        List<OperationRecord> operations = new ArrayList<>();
        String sql = "SELECT * FROM operations WHERE status = ? ORDER BY timestamp DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    operations.add(createOperationFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to retrieve operations by status: " + e.getMessage());
        }

        return operations;
    }

    /**
     * Get operation records by type (ENCRYPT/DECRYPT)
     */
    public List<OperationRecord> getOperationsByType(String operationType) {
        List<OperationRecord> operations = new ArrayList<>();
        String sql = "SELECT * FROM operations WHERE operation_type = ? ORDER BY timestamp DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, operationType);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    operations.add(createOperationFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to retrieve operations by type: " + e.getMessage());
        }

        return operations;
    }

    /**
     * Get recent operations (last N records)
     */
    public List<OperationRecord> getRecentOperations(int limit) {
        List<OperationRecord> operations = new ArrayList<>();
        String sql = "SELECT * FROM operations ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    operations.add(createOperationFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to retrieve recent operations: " + e.getMessage());
        }

        return operations;
    }

    /**
     * Delete operation record by ID
     */
    public boolean deleteOperation(Long id) {
        if (id == null) {
            return false;
        }

        String sql = "DELETE FROM operations WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Failed to delete operation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clear all operation history
     */
    public boolean clearAllOperations() {
        String sql = "DELETE FROM operations";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            int rowsAffected = stmt.executeUpdate(sql);
            return rowsAffected >= 0; // Return true even if no rows were deleted
            
        } catch (SQLException e) {
            System.err.println("Failed to clear operations: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get operation statistics
     */
    public OperationStats getOperationStats() {
        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as successful,
                SUM(CASE WHEN operation_type = 'ENCRYPT' THEN 1 ELSE 0 END) as encryptions,
                SUM(CASE WHEN operation_type = 'DECRYPT' THEN 1 ELSE 0 END) as decryptions
            FROM operations
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new OperationStats(
                    rs.getInt("total"),
                    rs.getInt("successful"),
                    rs.getInt("encryptions"),
                    rs.getInt("decryptions")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get operation stats: " + e.getMessage());
        }

        return new OperationStats(0, 0, 0, 0);
    }

    /**
     * Create OperationRecord from ResultSet
     */
    private OperationRecord createOperationFromResultSet(ResultSet rs) throws SQLException {
        return new OperationRecord(
            rs.getLong("id"),
            rs.getString("file_name"),
            rs.getString("operation_type"),
            rs.getString("method"),
            rs.getString("status"),
            rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }

    /**
     * Check if database is available and working
     */
    public boolean isDatabaseAvailable() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Inner class for operation statistics
     */
    public static class OperationStats {
        private final int total;
        private final int successful;
        private final int encryptions;
        private final int decryptions;

        public OperationStats(int total, int successful, int encryptions, int decryptions) {
            this.total = total;
            this.successful = successful;
            this.encryptions = encryptions;
            this.decryptions = decryptions;
        }

        public int getTotal() { return total; }
        public int getSuccessful() { return successful; }
        public int getFailed() { return total - successful; }
        public int getEncryptions() { return encryptions; }
        public int getDecryptions() { return decryptions; }
        
        public double getSuccessRate() {
            return total > 0 ? (double) successful / total * 100 : 0;
        }

        @Override
        public String toString() {
            return String.format("Stats{total=%d, successful=%d, failed=%d, encryptions=%d, decryptions=%d, success_rate=%.1f%%}",
                    total, successful, getFailed(), encryptions, decryptions, getSuccessRate());
        }
    }
}

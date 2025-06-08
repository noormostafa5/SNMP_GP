package com.mycompany.snmpmonitoringserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String URL = "jdbc:postgresql://end-point:5432/snmp";
    private static final String USER = "postgres";
    private static final String PASS = "password";
    
    private Connection connection;
    
    public DatabaseManager() {
        try {
            initializeDatabase();
            testConnection();
            testDatabaseConnection();
        } catch (SQLException e) {
            logger.error("Failed to initialize database: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private void testConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SELECT 1");
                    logger.info("Database connection test successful");
                }
            } else {
                logger.error("Database connection is not available");
            }
        } catch (SQLException e) {
            logger.error("Database connection test failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database connection test failed", e);
        }
    }
    
    private void initializeDatabase() throws SQLException {
        // Create connection with timeout
        DriverManager.setLoginTimeout(10);
        connection = DriverManager.getConnection(URL, USER, PASS);
        connection.setAutoCommit(true); // Ensure auto-commit is enabled
        logger.info("Connected to database successfully at {}", URL);
        
        // Create tables if they don't exist
        try (Statement stmt = connection.createStatement()) {
            // Create server_status table with indexes
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS server_status (" +
                "id SERIAL PRIMARY KEY, " +
                "server_name VARCHAR(100) NOT NULL, " +
                "server_ip VARCHAR(50) NOT NULL, " +
                "server_port INTEGER NOT NULL, " +
                "cpu_usage DOUBLE PRECISION NOT NULL, " +
                "memory_usage DOUBLE PRECISION NOT NULL, " +
                "disk_status TEXT, " +
                "alarm_status BOOLEAN NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT unique_server_status UNIQUE (server_name, timestamp)" +
                ")"
            );
            
            // Create indexes for better query performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_server_name ON server_status(server_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_timestamp ON server_status(timestamp)");
            
            logger.info("Database tables and indexes initialized successfully");
        }
    }
    
    public void saveServerStatus(ServerStatus status) {
        try {
            if (connection == null || connection.isClosed()) {
                logger.error("Database connection is not available. Attempting to reconnect...");
                initializeDatabase();
            }

            String sql = "INSERT INTO server_status " +
                        "(server_name, server_ip, server_port, cpu_usage, memory_usage, disk_status, alarm_status, timestamp) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT (server_name, timestamp) DO UPDATE SET " +
                        "cpu_usage = EXCLUDED.cpu_usage, " +
                        "memory_usage = EXCLUDED.memory_usage, " +
                        "disk_status = EXCLUDED.disk_status, " +
                        "alarm_status = EXCLUDED.alarm_status";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, status.getServerName());
                pstmt.setString(2, status.getServerIP());
                pstmt.setInt(3, status.getServerPort());
                pstmt.setDouble(4, status.getCpuUsage());
                pstmt.setDouble(5, status.getMemoryUsage());
                pstmt.setString(6, status.getDiskStatus());
                pstmt.setBoolean(7, status.isAlarmed());
                pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                
                int rowsAffected = pstmt.executeUpdate();
                logger.info("Server status saved to database for server: {} ({} rows affected)", 
                           status.getServerName(), rowsAffected);
                
                // Log the saved data for verification
                logger.debug("Saved data - Server: {}, CPU: {}%, Memory: {}%, Alarm: {}", 
                            status.getServerName(), 
                            String.format("%.2f", status.getCpuUsage()),
                            String.format("%.2f", status.getMemoryUsage()),
                            status.isAlarmed() ? "ALARMED" : "NORMAL");
            }
        } catch (SQLException e) {
            logger.error("Failed to save server status to database for server {}: {}", 
                        status.getServerName(), e.getMessage(), e);
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection: {}", e.getMessage(), e);
        }
    }

    public void testDatabaseConnection() {
        try {
            // Test inserting sample data
            String sql = "INSERT INTO server_status " +
                        "(server_name, server_ip, server_port, cpu_usage, memory_usage, disk_status, alarm_status, timestamp) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, "TEST-SERVER");
                pstmt.setString(2, "127.0.0.1");
                pstmt.setInt(3, 162);
                pstmt.setDouble(4, 25.5);
                pstmt.setDouble(5, 45.7);
                pstmt.setString(6, "Test disk status");
                pstmt.setBoolean(7, false);
                pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));


                int rowsAffected = pstmt.executeUpdate();
                logger.info("Test data inserted successfully. Rows affected: {}", rowsAffected);


                // Verify the data was inserted
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM server_status");
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        logger.info("Current number of records in database: {}", count);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Database test failed: {}", e.getMessage(), e);
            throw new RuntimeException("Database test failed", e);
        }
    }
} 
package com.mycompany.snmpmonitoringserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:postgresql://my-snmp-public.ca5cwqo86nt5.us-east-1.rds.amazonaws.com:5432/snmp";
    private static final String USER = "postgres";
    private static final String PASS = "Mayar123m";
    
    private Connection connection;
    
    public DatabaseManager() {
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            
            // Create nodes table if not exists
            String createNodesTableSQL = "CREATE TABLE IF NOT EXISTS nodes (" +
                "node_id SERIAL PRIMARY KEY," +
                "node_name VARCHAR(100)," +
                "node_ip INET," +
                "node_port INTEGER," +
                "CONSTRAINT unique_node_ip UNIQUE (node_ip)" +
                ")";
            
            // Create server reports table if not exists
            String createReportsTableSQL = "CREATE TABLE IF NOT EXISTS server_reports (" +
                "id SERIAL PRIMARY KEY," +
                "node_id INTEGER," +
                "server_name VARCHAR(100)," +
                "server_ip INET," +
                "report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "cpu_usage DOUBLE," +
                "memory_usage DOUBLE," +
                "disk_status TEXT," +
                "is_alarmed BOOLEAN" +
                ")";
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createNodesTableSQL);
                stmt.execute(createReportsTableSQL);
                logger.info("Database initialized successfully");
            }
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
        }
    }
    
    private Integer getNodeId(String serverIp) {
        String sql = "SELECT node_id FROM nodes WHERE node_ip = ?::INET";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, serverIp);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("node_id");
            }
        } catch (SQLException e) {
            logger.error("Error getting node_id for IP {}: {}", serverIp, e.getMessage());
        }
        return null;
    }
    
    public void saveServerReport(ServerStatus status) {
        Integer nodeId = getNodeId(status.getServerIP());
        if (nodeId == null) {
            logger.error("Cannot save report: No node found with IP {}", status.getServerIP());
            return;
        }

        String insertSQL = "INSERT INTO server_reports " +
            "(node_id, server_name, server_ip, cpu_usage, memory_usage, disk_status, is_alarmed) " +
            "VALUES (?, ?, ?::INET, ?, ?, ?, ?)";
            
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setInt(1, nodeId);
            pstmt.setString(2, status.getServerName());
            pstmt.setString(3, status.getServerIP());
            pstmt.setDouble(4, status.getCpuUsage());
            pstmt.setDouble(5, status.getMemoryUsage());
            pstmt.setString(6, status.getDiskStatus());
            pstmt.setBoolean(7, status.isAlarmed());
            
            pstmt.executeUpdate();
            logger.info("Server report saved to database for server: {}", status.getServerName());
        } catch (SQLException e) {
            logger.error("Error saving server report to database", e);
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }
} 
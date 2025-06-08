package com.mycompany.snmpmonitoringserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:postgresql://end-point:5432/snmp";
    private static final String USER = "postgres";
    private static final String PASS = "aws-rds-password";

    private Connection connection;

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS server_reports (" +
                    "id SERIAL PRIMARY KEY," +
                    "server_name VARCHAR(255) NOT NULL," +
                    "server_ip VARCHAR(45) NOT NULL," +
                    "server_port INT NOT NULL," +
                    "cpu_usage DOUBLE PRECISION NOT NULL," +
                    "memory_usage DOUBLE PRECISION NOT NULL," +
                    "disk_status TEXT NOT NULL," +
                    "is_alarmed BOOLEAN NOT NULL," +
                    "report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
                logger.info("Database initialized successfully");
            }
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
        }
    }

    public void saveServerReport(ServerStatus status) {
        String insertSQL = "INSERT INTO server_reports " +
                "(server_name, server_ip, server_port, cpu_usage, memory_usage, disk_status, is_alarmed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, status.getServerName());
            pstmt.setString(2, status.getServerIP());
            pstmt.setInt(3, status.getServerPort());
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
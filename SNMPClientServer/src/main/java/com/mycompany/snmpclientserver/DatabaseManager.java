package com.mycompany.snmpclientserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String URL = "jdbc:postgresql://end-point:5432/snmp";
    private static final String USER = "postgres";
    private static final String PASS = "aws-rds-password";
    private static Connection connection = null;

    public static void initialize() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USER, PASS);
            createHealthReportTableIfNotExists();
            createErrorReportTableIfNotExists();
            logger.info("Database connection established successfully");
        } catch (Exception e) {
            logger.error("Database initialization error: {}", e.getMessage());
        }
    }

    private static void createHealthReportTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS server_reports (" +
                "id SERIAL PRIMARY KEY, " +
                "server_name VARCHAR(100), " +
                "server_ip VARCHAR(50), " +
                "report_time TIMESTAMP, " +
                "cpu_usage FLOAT, " +
                "memory_usage FLOAT, " +
                "disk_usage FLOAT, " +
                "network_usage FLOAT, " +
                "status VARCHAR(20))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Table server_reports created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating health report table: {}", e.getMessage());
        }
    }

    private static void createErrorReportTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS error_reports (" +
                "id SERIAL PRIMARY KEY, " +
                "server_name VARCHAR(100), " +
                "server_ip VARCHAR(50), " +
                "description TEXT, " +
                "report_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Table error_reports created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating error report table: {}", e.getMessage());
        }
    }

    public static void saveReport(String serverName, String serverIp, double cpuUsage,
                                  double memoryUsage, double diskUsage, double networkUsage, boolean isAlarmed) {
        String statusText = isAlarmed ? "ALARMED" : "OK";
        String sql = "INSERT INTO server_reports " +
                "(server_name, server_ip, report_time, cpu_usage, memory_usage, disk_usage, network_usage, status) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, serverName);
            pstmt.setString(2, serverIp);
            pstmt.setDouble(3, cpuUsage);
            pstmt.setDouble(4, memoryUsage);
            pstmt.setDouble(5, diskUsage);
            pstmt.setDouble(6, networkUsage);
            pstmt.setString(7, statusText);
            pstmt.executeUpdate();
            logger.info("Health report saved successfully for server: {}", serverName);
        } catch (SQLException e) {
            logger.error("Error saving health report: {}", e.getMessage());
        }
    }

    public static void saveErrorReport(String serverName, String serverIp, String description, long timestamp) {
        String sql = "INSERT INTO error_reports " +
                "(server_name, server_ip, description, report_time) " +
                "VALUES (?, ?, ?, TO_TIMESTAMP(? / 1000.0))";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, serverName);
            pstmt.setString(2, serverIp);
            pstmt.setString(3, description);
            pstmt.setLong(4, timestamp);
            pstmt.executeUpdate();
            logger.warn("Error report saved successfully for server: {}", serverName);
        } catch (SQLException e) {
            logger.error("Error saving error report: {}", e.getMessage());
        }
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection: {}", e.getMessage());
            }
        }
    }
}
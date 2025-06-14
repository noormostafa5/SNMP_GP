package com.mycompany.snmpclientserver;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
            createActionRulesTableIfNotExists();
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

    private static void createActionRulesTableIfNotExists() {
        String createSql = "CREATE TABLE IF NOT EXISTS action_rules (" +
                "id SERIAL PRIMARY KEY, " +
                "server_id INTEGER, " +
                "action_type TEXT DEFAULT 'sending an email to the root user of the clientserver', " +
                "root_email TEXT DEFAULT 'mohamedmeselhy999@gmail.com')";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSql);
            logger.info("Table action_rules created or already exists with new schema");
        } catch (SQLException e) {
            logger.error("Error creating action rules table: {}", e.getMessage());
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

    public static void saveActionRule(int serverId) {
        String sql = "INSERT INTO action_rules " +
                "(server_id) " +
                "VALUES (?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, serverId);
            pstmt.executeUpdate();
            logger.info("Action rule saved successfully for server ID: {}", serverId);
        } catch (SQLException e) {
            logger.error("Error saving action rule: {}", e.getMessage());
        }
    }

    public static void handleServerError(String serverIp, String errorType, String description) {
        try {
            String serverName = "Server-" + serverIp; // Derive serverName here
            // 1. حفظ تقرير الخطأ
            saveErrorReport(serverName, serverIp, description, System.currentTimeMillis());

            // 2. الحصول على قواعد الإجراءات للخادم
            String sql = "SELECT * FROM action_rules WHERE server_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, 1); // استخدام server_id = 1
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String actionType = rs.getString("action_type");
                    String rootEmail = rs.getString("root_email");
                    logger.info("Retrieved actionType: '{}', rootEmail: '{}' from action_rules.", actionType, rootEmail);

                    // 3. إرسال البريد الإلكتروني
                    if ("sending an email to the root user of the clientserver".equals(actionType.trim())) {
                        logger.info("Action type matches for email sending.");
                        sendErrorEmail(rootEmail, serverName, serverIp, errorType, description);
                    } else {
                        logger.warn("Action type '{}' does not match 'sending an email to the root user of the clientserver'. Email not sent.", actionType);
                    }
                }
                if (!rs.isBeforeFirst() && !rs.isAfterLast()) { // Check if ResultSet was empty
                    logger.warn("No action rules found for server_id = 1. Email not sent.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error handling server error: {}", e.getMessage(), e);
        }
    }

    private static void sendErrorEmail(String toEmail, String serverName, String serverIp, String errorType,
                                       String description) {
        logger.info("Attempting to send error email to: {}", toEmail);
        final String username = "mohamedmeselhy999@gmail.com";
        final String password = "nrab dpzf koio xtnf";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        logger.info("Getting mail session...");
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            logger.info("Creating MimeMessage...");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Server Error Alert - " + serverName + " (" + serverIp + ")");

            String emailContent = String.format(
                    "Server Error Alert\n\n" +
                            "Server Name: %s\n" +
                            "Server IP: %s\n" +
                            "Error Type: %s\n" +
                            "Description: %s\n" +
                            "Time of Error: %s\n\n" +
                            "This is an automated message from the monitoring system.",
                    serverName,
                    serverIp,
                    errorType,
                    description,
                    new java.util.Date()
            );

            message.setText(emailContent);
            logger.info("Sending email via Transport...");
            Transport.send(message);
            logger.info("Error notification email sent to: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Error sending email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public static void addDefaultActionRule() {
        String checkSql = "SELECT COUNT(*) FROM action_rules WHERE server_id = ?";
        String insertSql = "INSERT INTO action_rules " +
                "(server_id) " +
                "VALUES (?)";

        try (PreparedStatement checkPstmt = connection.prepareStatement(checkSql)) {
            checkPstmt.setInt(1, 1);
            ResultSet rs = checkPstmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) { // If no default rule exists for server_id = 1
                try (PreparedStatement insertPstmt = connection.prepareStatement(insertSql)) {
                    insertPstmt.setInt(1, 1);  // server_id = 1
                    insertPstmt.executeUpdate();
                    logger.info("Default action rule added successfully");
                }
            } else {
                logger.info("Default action rule already exists for server ID: 1");
            }
        } catch (SQLException e) {
            logger.error("Error adding default action rule: {}", e.getMessage());
        }
    }

    public static void restartClientServer(String serverIp) {
        try {
            // التحقق من أن الخادم متصل
            if (InetAddress.getByName(serverIp).isReachable(5000)) {
                // هنا يمكنك إضافة الكود الخاص بإعادة تشغيل الخادم
                // مثال: استخدام SSH أو أي طريقة أخرى لإعادة التشغيل
                logger.info("Attempting to restart server: {}", serverIp);

                // يمكنك استخدام Runtime.exec() لتنفيذ أمر إعادة التشغيل
                // Runtime.getRuntime().exec("ssh user@" + serverIp + " sudo reboot");

                logger.info("Restart command sent to server: {}", serverIp);
            } else {
                logger.error("Server is not reachable: {}", serverIp);
            }
        } catch (IOException e) {
            logger.error("Error restarting server: {}", e.getMessage());
        }
    }

    public static void getServerReports(String serverIp) {
        String sql = "SELECT * FROM server_reports WHERE server_ip = ? ORDER BY report_time DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, serverIp);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n=== Server Reports for " + serverIp + " ===");
            System.out.println("ID | Server Name | CPU Usage | Memory Usage | Disk Usage | Network Usage | Status | Report Time");
            System.out.println("----------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%d | %s | %.2f%% | %.2f%% | %.2f%% | %.2f%% | %s | %s%n",
                        rs.getInt("id"),
                        rs.getString("server_name"),
                        rs.getDouble("cpu_usage"),
                        rs.getDouble("memory_usage"),
                        rs.getDouble("disk_usage"),
                        rs.getDouble("network_usage"),
                        rs.getString("status"),
                        rs.getTimestamp("report_time")
                );
            }
        } catch (SQLException e) {
            logger.error("Error retrieving server reports: {}", e.getMessage());
        }
    }

    public static void getAllServerReports() {
        String sql = "SELECT * FROM server_reports ORDER BY report_time DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== All Server Reports ===");
            System.out.println("ID | Server Name | Server IP | CPU Usage | Memory Usage | Disk Usage | Network Usage | Status | Report Time");
            System.out.println("--------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%d | %s | %s | %.2f%% | %.2f%% | %.2f%% | %.2f%% | %s | %s%n",
                        rs.getInt("id"),
                        rs.getString("server_name"),
                        rs.getString("server_ip"),
                        rs.getDouble("cpu_usage"),
                        rs.getDouble("memory_usage"),
                        rs.getDouble("disk_usage"),
                        rs.getDouble("network_usage"),
                        rs.getString("status"),
                        rs.getTimestamp("report_time")
                );
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all server reports: {}", e.getMessage());
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

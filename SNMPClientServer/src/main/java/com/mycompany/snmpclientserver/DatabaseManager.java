package com.mycompany.snmpclientserver;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
    private static final String URL = "jdbc:postgresql://my-snmp-public.ca5cwqo86nt5.us-east-1.rds.amazonaws.com:5432/snmp";
    private static final String USER = "postgres";
    private static final String PASS = "Mayar123m";
    private static Connection connection = null;

    public static void initialize() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USER, PASS);
            logger.info("Database connection established successfully");
            
            // Create tables in the correct order
            createNodesTableIfNotExists();
            createHealthReportTableIfNotExists();
            createErrorReportTableIfNotExists();
            createActionRulesTableIfNotExists();
            
            // Verify tables exist
            verifyTablesExist();
            
            // Add a test node if none exists
            addTestNodeIfNotExists();
            
            logger.info("Database initialization completed successfully");
        } catch (Exception e) {
            logger.error("Database initialization error: {}", e.getMessage(), e);
        }
    }

    private static void createNodesTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS nodes (" +
                    "node_id SERIAL PRIMARY KEY, " +
                    "node_name VARCHAR(100), " +
                    "node_ip INET, " +
                    "node_port INTEGER, " +
                    "CONSTRAINT unique_node_ip UNIQUE (node_ip))";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Table nodes created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating nodes table: {}", e.getMessage());
        }
    }

    private static void createHealthReportTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS server_reports (" +
                    "id SERIAL PRIMARY KEY, " +
                    "node_id INTEGER, " +
                    "server_name VARCHAR(100), " +
                    "server_ip INET, " +
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
                    "node_id INTEGER, " +
                    "server_name VARCHAR(100), " +
                    "server_ip INET, " +
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
        // Drop the table if it exists to ensure the schema is always up-to-date
        String dropSql = "DROP TABLE IF EXISTS action_rules CASCADE"; // CASCADE to drop dependent objects
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(dropSql);
            logger.info("Table action_rules dropped if it existed");
        } catch (SQLException e) {
            logger.error("Error dropping action rules table: {}", e.getMessage());
        }

        String sql = "CREATE TABLE action_rules (" +
                    "id SERIAL PRIMARY KEY, " +
                    "node_id INTEGER, " +
                    "action_type TEXT DEFAULT 'sending an email to the root user of the clientserver', " +
                    "root_email TEXT DEFAULT 'mohamedmeselhy999@gmail.com'," +
                    "CONSTRAINT fk_node_id FOREIGN KEY (node_id) REFERENCES nodes (node_id) ON DELETE CASCADE)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Table action_rules created successfully with node_id and foreign key constraint");
        } catch (SQLException e) {
            logger.error("Error creating action rules table: {}", e.getMessage());
        }
    }

    private static Integer getNodeId(String serverIp) {
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

    private static List<Integer> getAllNodeIds() {
        List<Integer> nodeIds = new ArrayList<>();
        String sql = "SELECT node_id FROM nodes";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                nodeIds.add(rs.getInt("node_id"));
            }
        } catch (SQLException e) {
            logger.error("Error getting all node IDs: {}", e.getMessage());
        }
        return nodeIds;
    }

    public static void saveReport(String serverName, String serverIp, double cpuUsage, 
                                double memoryUsage, double diskUsage, double networkUsage, boolean isAlarmed) {
        Integer nodeId = getNodeId(serverIp);
        if (nodeId == null) {
            logger.error("Cannot save report: No node found with IP {}", serverIp);
            // Try to add the node if it doesn't exist
            nodeId = addNode(serverName, serverIp, 161);
            if (nodeId == null) {
                logger.error("Failed to add node and still cannot find node_id for IP {}", serverIp);
                return;
            } else {
                // If a new node was added, add a default action rule for it
                saveActionRule(nodeId);
            }
        }

        String statusText = isAlarmed ? "ALARMED" : "OK";
        String sql = "INSERT INTO server_reports " +
                    "(node_id, server_name, server_ip, report_time, cpu_usage, memory_usage, disk_usage, network_usage, status) " +
                    "VALUES (?, ?, ?::INET, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, nodeId);
            pstmt.setString(2, serverName);
            pstmt.setString(3, serverIp);
            pstmt.setDouble(4, cpuUsage);
            pstmt.setDouble(5, memoryUsage);
            pstmt.setDouble(6, diskUsage);
            pstmt.setDouble(7, networkUsage);
            pstmt.setString(8, statusText);
            pstmt.executeUpdate();
            logger.info("Health report saved successfully for server: {} (node_id: {})", serverName, nodeId);
        } catch (SQLException e) {
            logger.error("Error saving health report: {}", e.getMessage(), e);
        }
    }

    public static void saveErrorReport(String serverName, String serverIp, String description, long timestamp) {
        Integer nodeId = getNodeId(serverIp);
        if (nodeId == null) {
            logger.error("Cannot save error report: No node found with IP {}", serverIp);
            // Try to add the node if it doesn't exist
            nodeId = addNode(serverName, serverIp, 161);
            if (nodeId == null) {
                logger.error("Failed to add node and still cannot find node_id for IP {}", serverIp);
                return;
            } else {
                // If a new node was added, add a default action rule for it
                saveActionRule(nodeId);
            }
        }

        String sql = "INSERT INTO error_reports " +
                    "(node_id, server_name, server_ip, description, report_time) " +
                    "VALUES (?, ?, ?::INET, ?, TO_TIMESTAMP(? / 1000.0))";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, nodeId);
            pstmt.setString(2, serverName);
            pstmt.setString(3, serverIp);
            pstmt.setString(4, description);
            pstmt.setLong(5, timestamp);
            pstmt.executeUpdate();
            logger.warn("Error report saved successfully for server: {} (node_id: {})", serverName, nodeId);
        } catch (SQLException e) {
            logger.error("Error saving error report: {}", e.getMessage(), e);
        }
    }

    public static void saveActionRule(int nodeId) {
        String sql = "INSERT INTO action_rules " +
                    "(node_id) " +
                    "VALUES (?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, nodeId);
            pstmt.executeUpdate();
            logger.info("Action rule saved successfully for node ID: {}", nodeId);

            // Debug: Verify immediate visibility of the inserted rule
            String verifySql = "SELECT COUNT(*) FROM action_rules WHERE node_id = ?";
            try (PreparedStatement verifyPstmt = connection.prepareStatement(verifySql)) {
                verifyPstmt.setInt(1, nodeId);
                ResultSet rs = verifyPstmt.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    logger.info("Debug: After insert, found {} action rules for node ID: {}", count, nodeId);
                }
            } catch (SQLException ve) {
                logger.error("Debug: Error verifying action rule after insert: {}", ve.getMessage());
            }

        } catch (SQLException e) {
            logger.error("Error saving action rule: {}", e.getMessage());
        }
    }

    public static void handleServerError(String serverName, String serverIp, String errorType, String description) {
        try {
            // Get the node_id for the given serverIp
            Integer nodeId = getNodeId(serverIp);
            if (nodeId == null) {
                logger.error("Cannot handle server error: No node found with IP {}", serverIp);
                return;
            }

            // 2. الحصول على قواعد الإجراءات للخادم
            String sql = "SELECT * FROM action_rules WHERE node_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, nodeId);
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
                    logger.warn("No action rules found for node_id = {}. Email not sent.", nodeId);
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
        List<Integer> nodeIds = getAllNodeIds();
        if (nodeIds.isEmpty()) {
            logger.warn("No nodes found in the database. Cannot add default action rules.");
            return;
        }

        for (Integer nodeId : nodeIds) {
            String checkSql = "SELECT COUNT(*) FROM action_rules WHERE node_id = ?";
            String insertSql = "INSERT INTO action_rules " +
                               "(node_id) " +
                               "VALUES (?)";
            
            try (PreparedStatement checkPstmt = connection.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, nodeId);
                ResultSet rs = checkPstmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) { // If no default rule exists for this node_id
                    try (PreparedStatement insertPstmt = connection.prepareStatement(insertSql)) {
                        insertPstmt.setInt(1, nodeId);  // Use the retrieved node_id
                        insertPstmt.executeUpdate();
                        logger.info("Default action rule added successfully for node ID: {}", nodeId);
                    }
                } else {
                    logger.info("Default action rule already exists for node ID: {}", nodeId);
                }
            } catch (SQLException e) {
                logger.error("Error adding default action rule for node {}: {}", nodeId, e.getMessage());
            }
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

    private static void verifyTablesExist() {
        String[] tables = {"nodes", "server_reports", "error_reports", "action_rules"};
        for (String table : tables) {
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = '" + table + "')");
                if (rs.next() && rs.getBoolean(1)) {
                    logger.info("Table '{}' exists", table);
                } else {
                    logger.error("Table '{}' does not exist!", table);
                }
            } catch (SQLException e) {
                logger.error("Error verifying table '{}': {}", table, e.getMessage());
            }
        }
    }

    private static void addTestNodeIfNotExists() {
        String checkSql = "SELECT COUNT(*) FROM nodes";
        String insertSql = "INSERT INTO nodes (node_name, node_ip, node_port) VALUES (?, ?::INET, ?)";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(checkSql);
            if (rs.next() && rs.getInt(1) == 0) {
                // No nodes exist, add a test node
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    pstmt.setString(1, "Test-Server");
                    pstmt.setString(2, "192.168.1.100");
                    pstmt.setInt(3, 161);
                    pstmt.executeUpdate();
                    logger.info("Test node added successfully");
                }
            } else {
                logger.info("Nodes already exist in the database");
            }
        } catch (SQLException e) {
            logger.error("Error adding test node: {}", e.getMessage());
        }
    }

    private static Integer addNode(String nodeName, String nodeIp, int nodePort) {
        String sql = "INSERT INTO nodes (node_name, node_ip, node_port) VALUES (?, ?::INET, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nodeName);
            pstmt.setString(2, nodeIp);
            pstmt.setInt(3, nodePort);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer nodeId = generatedKeys.getInt(1);
                        logger.info("Added new node: {} ({}) with ID {}", nodeName, nodeIp, nodeId);
                        return nodeId;
                    }
                }
            }
            logger.error("Failed to retrieve generated node ID after adding node: {} ({})", nodeName, nodeIp);
            return null;
        } catch (SQLException e) {
            logger.error("Error adding node: {}", e.getMessage(), e);
            return null;
        }
    }
} 
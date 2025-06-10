package com.mycompany.snmpproject;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPClientServer {

    // SNMP Configuration
    private static final String MONITORING_SERVER_IP = "localhost";
    private static final int MONITORING_SERVER_PORT = 162;
    private static final String COMMUNITY = "public";
    
    // AWS Database Configuration
    private static final String URL = "jdbc:postgresql://end-point:5432/snmp";
    private static final String USER = "postgres";
    private static final String PASS = "password";
    
    private Snmp snmp;
    private String clientIP;
    private Timer monitoringTimer;
    private Connection dbConnection;
    
    public SNMPClientServer() {
        try {
            // Initialize database connection
            initializeDatabase();
            
            // Initialize SNMP
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();
            
            // Get server IP address
            clientIP = InetAddress.getLocalHost().getHostAddress();
            
            // Start monitoring
            startMonitoring();
            
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeDatabase() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            dbConnection = DriverManager.getConnection(URL, USER, PASS);
            createTablesIfNotExist();
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
    }
    
    private void createTablesIfNotExist() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS system_metrics (
                id SERIAL PRIMARY KEY,
                timestamp TIMESTAMP NOT NULL,
                client_ip VARCHAR(50) NOT NULL,
                cpu_usage DOUBLE PRECISION,
                memory_usage DOUBLE PRECISION,
                disk_usage DOUBLE PRECISION,
                error_type VARCHAR(50),
                description TEXT
            )
        """;
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(createTableSQL)) {
            stmt.execute();
        }
    }
    
    private void startMonitoring() {
        monitoringTimer = new Timer();
        monitoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkSystemResources();
            }
        }, 0, 60000); // فحص كل دقيقة
    }
    
    private void checkSystemResources() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        // Get system metrics
        double cpuLoad = osBean.getSystemLoadAverage();
        double memoryUsage = (double) memoryBean.getHeapMemoryUsage().getUsed() / 
                           memoryBean.getHeapMemoryUsage().getMax();
        java.io.File root = new java.io.File("/");
        double diskUsage = 1 - ((double) root.getFreeSpace() / root.getTotalSpace());
        
        // Store metrics in database
        storeMetrics(cpuLoad, memoryUsage, diskUsage, null, null);
        
        // Check thresholds and send traps
        if (cpuLoad > 0.8) {
            String description = "CPU usage is above 80%: " + (cpuLoad * 100) + "%";
            sendTrap("CPU_USAGE_HIGH", description);
            storeMetrics(cpuLoad, memoryUsage, diskUsage, "CPU_USAGE_HIGH", description);
        }
        
        if (memoryUsage > 0.8) {
            String description = "Memory usage is above 80%: " + (memoryUsage * 100) + "%";
            sendTrap("MEMORY_USAGE_HIGH", description);
            storeMetrics(cpuLoad, memoryUsage, diskUsage, "MEMORY_USAGE_HIGH", description);
        }
        
        if (diskUsage > 0.8) {
            String description = "Disk usage is above 80%: " + (diskUsage * 100) + "%";
            sendTrap("DISK_USAGE_HIGH", description);
            storeMetrics(cpuLoad, memoryUsage, diskUsage, "DISK_USAGE_HIGH", description);
        }
    }
    
    private void storeMetrics(double cpuUsage, double memoryUsage, double diskUsage, 
                            String errorType, String description) {
        String insertSQL = """
            INSERT INTO system_metrics 
            (timestamp, client_ip, cpu_usage, memory_usage, disk_usage, error_type, description)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(insertSQL)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, clientIP);
            stmt.setDouble(3, cpuUsage);
            stmt.setDouble(4, memoryUsage);
            stmt.setDouble(5, diskUsage);
            stmt.setString(6, errorType);
            stmt.setString(7, description);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void sendTrap(String errorType, String description) {
        try {
            // إنشاء PDU للإشعار
            PDU pdu = new PDU();
            pdu.setType(PDU.TRAP);
            
            // إضافة معلومات الإشعار
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, 
                new OID("1.3.6.1.4.1.9999.1.1.1"))); // OID مخصص للإشعارات
            
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.1.2"), 
                new OctetString(clientIP)));
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.1.3"), 
                new OctetString(errorType)));
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.1.4"), 
                new OctetString(description)));
            
            // إرسال الإشعار
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(COMMUNITY));
            target.setVersion(SnmpConstants.version2c);
            target.setAddress(GenericAddress.parse("udp:" + MONITORING_SERVER_IP + "/" + MONITORING_SERVER_PORT));
            
            snmp.send(pdu, target);
            System.out.println("Trap sent: " + errorType + " - " + description);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
        if (monitoringTimer != null) {
            monitoringTimer.cancel();
        }
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        SNMPClientServer client = new SNMPClientServer();
        // Add shutdown hook to properly close resources
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));
    }
} 
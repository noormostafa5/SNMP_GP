package com.mycompany.snmpclientserver;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// SNMP4J Imports
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.sun.management.OperatingSystemMXBean;

import oshi.SystemInfo;
import oshi.software.os.OSFileStore;

public class SNMPClientServer {
    private static final Logger logger = LoggerFactory.getLogger(SNMPClientServer.class);
    private static final int DEFAULT_PORT = 162; // This might become the trap listening port or general SNMP agent port
    private static final int REPORT_INTERVAL = 60; // seconds (every minute)
    public static final int DEFAULT_TRAP_PORT = 162; // Default SNMP trap port

    private final String serverName;
    private final String serverIp;
    private final int port; // This might be used for general SNMP agent requests, not UDP socket
    private final String monitoringIp;
    private final int monitoringPort; // This is the target port for sending traps

    private final OperatingSystemMXBean osBean;
    private final SystemInfo systemInfo;
    
    private ScheduledExecutorService scheduler;
    
    // SNMP4J fields
    private Snmp snmp;
    private CommunityTarget target; // Target for sending traps

    // OIDs for the health report and error report (Custom OIDs under .1.3.6.1.4.1.999)
    public static final OID OID_HEALTH_REPORT = new OID(".1.3.6.1.4.1.999.1.1"); // Custom OID for health report
    public static final OID OID_ERROR_REPORT = new OID(".1.3.6.1.4.1.999.1.2"); // Custom OID for error report
    public static final OID OID_CPU_USAGE = new OID(".1.3.6.1.4.1.999.2.1");
    public static final OID OID_MEMORY_USAGE = new OID(".1.3.6.1.4.1.999.2.2");
    public static final OID OID_DISK_USAGE = new OID(".1.3.6.1.4.1.999.2.3");
    public static final OID OID_NETWORK_USAGE = new OID(".1.3.6.1.4.1.999.2.4");
    public static final OID OID_IS_ALARMED = new OID(".1.3.6.1.4.1.999.2.5");
    public static final OID OID_ERROR_DESCRIPTION = new OID(".1.3.6.1.4.1.999.2.6");

    public SNMPClientServer(String serverName, String serverIp, int port, String monitoringIp, int monitoringPort, int trapPort) {
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.port = port; // This port can be repurposed for SNMP agent if needed later
        this.monitoringIp = monitoringIp;
        this.monitoringPort = monitoringPort; // This is the port where the monitoring server listens for traps
        
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.systemInfo = new SystemInfo();
        
        // Initialize SNMP for sending traps
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen(); // Start listening for responses if needed, though for traps it's not strictly necessary.
                                // It will be needed for agent functionality later.
        } catch (IOException e) {
            logger.error("Error initializing SNMP sender: {}", e.getMessage());
        }

        // Configure the target for sending traps
        target = new CommunityTarget();
        target.setCommunity(new OctetString("public")); // Use a default community string
        target.setAddress(new UdpAddress(monitoringIp + "/" + monitoringPort));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c); // Use SNMPv2c for traps
    }

    public void start() {
        try {
            logger.info("SNMP Client Server started. Sending health reports to {}:{}.", monitoringIp, monitoringPort);
            
            // Start sending health reports as SNMP Traps
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::sendHealthReport, 0, REPORT_INTERVAL, TimeUnit.SECONDS);
            
            // In the next phase, we will add SNMP agent functionality here to listen for requests

        } catch (Exception e) {
            logger.error("Error starting server: {}", e.getMessage());
        }
    }

    private void sendHealthReport() {
        try {
            double cpuUsage = getCpuUsage();
            double memoryUsage = getMemoryUsage();
            double diskUsage = getDiskUsage();
            double networkUsage = getNetworkUsage();

            boolean isAlarmed = false;

            // Check CPU usage and send separate error report if alarmed
            if (cpuUsage > 70.0) {
                isAlarmed = true; // Set alarmed status for the main health report
                sendSingleErrorReport(String.format("CPU usage is high (%.2f%%). ", cpuUsage));
            }
            // Check Memory usage and send separate error report if alarmed
            if (memoryUsage > 70.0) {
                isAlarmed = true; // Set alarmed status for the main health report
                sendSingleErrorReport(String.format("Memory usage is high (%.2f%%). ", memoryUsage));
            }
            // Check Disk usage and send separate error report if alarmed
            if (diskUsage > 70.0) {
                isAlarmed = true; // Set alarmed status for the main health report
                sendSingleErrorReport(String.format("Disk usage is high (%.2f%%). ", diskUsage));
            }

            // Create and send Health Report as SNMP Trap
            PDU trap = new PDU();
            trap.setType(PDU.TRAP);
            // Add sysUpTime to the trap
            trap.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(String.valueOf(System.currentTimeMillis()))));
            // Add snmpTrapOID to identify the type of trap (Health Report)
            trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, OID_HEALTH_REPORT));
            // Add custom VariableBindings for health metrics
            trap.add(new VariableBinding(new OID(OID_HEALTH_REPORT + ".1"), new OctetString(serverName))); // Server Name
            trap.add(new VariableBinding(new OID(OID_HEALTH_REPORT + ".2"), new OctetString(serverIp)));   // Server IP
            trap.add(new VariableBinding(OID_CPU_USAGE, new OctetString(String.format("%.2f", cpuUsage))));
            trap.add(new VariableBinding(OID_MEMORY_USAGE, new OctetString(String.format("%.2f", memoryUsage))));
            trap.add(new VariableBinding(OID_DISK_USAGE, new OctetString(String.format("%.2f", diskUsage))));
            trap.add(new VariableBinding(OID_NETWORK_USAGE, new OctetString(String.format("%.2f", networkUsage))));
            trap.add(new VariableBinding(OID_IS_ALARMED, new OctetString(String.valueOf(isAlarmed))));

            sendSNMPTrap(trap);
            logger.info("Health report SNMP trap sent to monitoring server");
            
            // Removed: Save health report to database
            // DatabaseManager.saveReport(
            //     serverName,
            //     serverIp,
            //     cpuUsage,
            //     memoryUsage,
            //     diskUsage,
            //     networkUsage,
            //     isAlarmed 
            // );

        } catch (Exception e) {
            logger.error("Error sending health report: {}", e.getMessage());
        }
    }

    private void sendSingleErrorReport(String description) {
        try {
            // Create and send Error Report as SNMP Trap
            PDU trap = new PDU();
            trap.setType(PDU.TRAP);
            // Add sysUpTime to the trap
            trap.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(String.valueOf(System.currentTimeMillis()))));
            // Add snmpTrapOID to identify the type of trap (Error Report)
            trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, OID_ERROR_REPORT));
            // Add custom VariableBindings for error description
            trap.add(new VariableBinding(new OID(OID_ERROR_REPORT + ".1"), new OctetString(serverName))); // Server Name
            trap.add(new VariableBinding(new OID(OID_ERROR_REPORT + ".2"), new OctetString(serverIp)));   // Server IP
            trap.add(new VariableBinding(OID_ERROR_DESCRIPTION, new OctetString(description)));

            sendSNMPTrap(trap);
            logger.warn("Error report SNMP trap sent to monitoring server: {}", description);
        } catch (Exception e) {
            logger.error("Error sending error report: {}", e.getMessage());
        }
    }

    private void sendSNMPTrap(PDU trap) throws IOException {
        try {
            snmp.send(trap, target);
        } catch (IOException e) {
            logger.error("Error sending SNMP trap: {}", e.getMessage());
            throw e; // Re-throw to be caught by calling method
        }
    }

    private double getCpuUsage() {
        return osBean.getSystemCpuLoad() * 100.0;
    }

    private double getMemoryUsage() {
        long totalMemory = osBean.getTotalPhysicalMemorySize();
        long freeMemory = osBean.getFreePhysicalMemorySize();
        return ((double)(totalMemory - freeMemory) / totalMemory) * 100.0;
    }

    private double getDiskUsage() {
        long totalDiskSpace = 0;
        long usableDiskSpace = 0;
        for (OSFileStore fs : systemInfo.getOperatingSystem().getFileSystem().getFileStores()) {
            totalDiskSpace += fs.getTotalSpace();
            usableDiskSpace += fs.getUsableSpace();
        }
        if (totalDiskSpace == 0) return 0.0;
        return ((double)(totalDiskSpace - usableDiskSpace) / totalDiskSpace) * 100.0;
    }

    private double getNetworkUsage() {
        // This is a placeholder for real-time network usage. 
        // Oshi provides network interface information, but calculating real-time usage
        // requires tracking bytes sent/received over time and is more complex.
        // For simplicity, we return 0.0.
        return 0.0; 
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        
        if (snmp != null) {
            try {
                snmp.close();
            } catch (IOException e) {
                logger.error("Error closing SNMP sender: {}", e.getMessage());
            }
        }

        DatabaseManager.close();
    }

    public static void main(String[] args) {
        final String serverName;
        final String serverIp;
        final int port;
        final String monitoringIp;
        final int monitoringPort;
        final int trapPort; // This is now used for the SNMP agent listener if implemented

        if (args.length == 6) {
            serverName = args[0];
            serverIp = args[1];
            port = Integer.parseInt(args[2]); // This might be for agent port later
            monitoringIp = args[3];
            monitoringPort = Integer.parseInt(args[4]);
            trapPort = Integer.parseInt(args[5]);
        } else {
            logger.error("Usage: java -jar <your-jar-file>.jar <serverName> <serverIp> <port> <monitoringIp> <monitoringPort> <trapPort>");
            logger.error("Using default values.");
            serverName = "DefaultServer";
            serverIp = "127.0.0.1";
            port = 161; // Default for agent
            monitoringIp = "127.0.0.1";
            monitoringPort = DEFAULT_TRAP_PORT; // Default for traps
            trapPort = DEFAULT_TRAP_PORT; // Default for traps
        }

        SNMPClientServer server = new SNMPClientServer(serverName, serverIp, port, monitoringIp, monitoringPort, trapPort);
        server.start();

        // Add shutdown hook to ensure resources are properly released
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down SNMP Client Server...");
            server.stop();
        }));
    }
} 
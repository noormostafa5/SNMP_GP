package com.mycompany.snmpclientserver;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.management.OperatingSystemMXBean;

import oshi.SystemInfo;
import oshi.software.os.OSFileStore;

public class SNMPClientServer {
    private static final Logger logger = LoggerFactory.getLogger(SNMPClientServer.class);
    private static final int DEFAULT_PORT = 162;
    private static final int DEFAULT_MONITORING_PORT = 161;
    private static final int BUFFER_SIZE = 1024;
    private static final int REPORT_INTERVAL = 60; // seconds (every minute)

    private final String serverName;
    private final String serverIp;
    private final int port;
    private final String monitoringIp;
    private final int monitoringPort;
    private final ObjectMapper objectMapper;
    private final OperatingSystemMXBean osBean;
    private final SystemInfo systemInfo;
    private DatagramSocket socket;
    private ScheduledExecutorService scheduler;

    public SNMPClientServer(String serverName, String serverIp, int port, String monitoringIp, int monitoringPort) {
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.port = port;
        this.monitoringIp = monitoringIp;
        this.monitoringPort = monitoringPort;
        this.objectMapper = new ObjectMapper();
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.systemInfo = new SystemInfo();
    }

    public void start() {
        try {
            socket = new DatagramSocket(port);
            logger.info("SNMP Client Server started on port {}", port);

            // Start sending health reports
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::sendHealthReport, 0, REPORT_INTERVAL, TimeUnit.SECONDS);

            // Start listening for SNMP requests
            new Thread(this::listenForRequests).start();

        } catch (IOException e) {
            logger.error("Error starting server: {}", e.getMessage());
        }
    }

    private void listenForRequests() {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (!socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength());
                logger.info("Received request: {}", request);

                // Process the request and send response
                String response = processRequest(request);
                byte[] responseData = response.getBytes();

                DatagramPacket responsePacket = new DatagramPacket(
                        responseData,
                        responseData.length,
                        packet.getAddress(),
                        packet.getPort()
                );

                socket.send(responsePacket);

            } catch (IOException e) {
                if (!socket.isClosed()) {
                    logger.error("Error processing request: {}", e.getMessage());
                }
            }
        }
    }

    private String processRequest(String request) {
        try {
            // Here you would implement actual SNMP request processing
            // For now, we'll just return a simple response
            return "Response to: " + request;
        } catch (Exception e) {
            logger.error("Error processing request: {}", e.getMessage());
            return "Error processing request";
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

            // Send Health Report
            ObjectNode healthReportNode = objectMapper.createObjectNode();
            healthReportNode.put("reportType", "health_report");
            healthReportNode.put("serverName", serverName);
            healthReportNode.put("serverIp", serverIp);
            healthReportNode.put("cpuUsage", cpuUsage);
            healthReportNode.put("memoryUsage", memoryUsage);
            healthReportNode.put("diskUsage", diskUsage);
            healthReportNode.put("networkUsage", networkUsage);
            healthReportNode.put("isAlarmed", isAlarmed);

            String healthReportJson = objectMapper.writeValueAsString(healthReportNode);
            sendReportPacket(healthReportJson);
            logger.info("Health report sent to monitoring server");

            // Save health report to database
            DatabaseManager.saveReport(
                    serverName,
                    serverIp,
                    cpuUsage,
                    memoryUsage,
                    diskUsage,
                    networkUsage,
                    isAlarmed
            );

        } catch (Exception e) {
            logger.error("Error sending health report: {}", e.getMessage());
        }
    }

    private void sendSingleErrorReport(String description) throws IOException {
        ObjectNode errorReportNode = objectMapper.createObjectNode();
        errorReportNode.put("reportType", "error_report");
        errorReportNode.put("serverName", serverName);
        errorReportNode.put("serverIp", serverIp);
        errorReportNode.put("description", description);
        errorReportNode.put("timestamp", System.currentTimeMillis());

        String errorReportJson = objectMapper.writeValueAsString(errorReportNode);
        sendReportPacket(errorReportJson);
        logger.warn("Error report sent to monitoring server: {}", description);
    }

    private void sendReportPacket(String reportJson) throws IOException {
        byte[] reportData = reportJson.getBytes();
        DatagramPacket packet = new DatagramPacket(
                reportData,
                reportData.length,
                InetAddress.getByName(monitoringIp),
                monitoringPort
        );
        socket.send(packet);
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
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        DatabaseManager.close();
    }

    public static void main(String[] args) {
        final String serverName;
        final String serverIp;
        final int port;
        final String monitoringIp;
        final int monitoringPort;

        if (args.length == 5) {
            serverName = args[0];
            serverIp = args[1];
            port = Integer.parseInt(args[2]);
            monitoringIp = args[3];
            monitoringPort = Integer.parseInt(args[4]);
        } else if (args.length == 4) {
            serverName = "BTS-1";
            serverIp = args[0];
            port = Integer.parseInt(args[1]);
            monitoringIp = args[2];
            monitoringPort = Integer.parseInt(args[3]);
            logger.info("Using default server name 'BTS-1' with provided arguments.");
        } else {
            logger.error("Usage: java -jar SNMPClientServer.jar <serverName> <serverIp> <port> <monitoringIp> <monitoringPort>");
            logger.error("Or:    java -jar SNMPClientServer.jar <serverIp> <port> <monitoringIp> <monitoringPort> (for default serverName 'BTS-1')");
            System.exit(1);
            return; // Add return to explicitly state no further execution
        }

        DatabaseManager.initialize();
        SNMPClientServer server = new SNMPClientServer(serverName, serverIp, port, monitoringIp, monitoringPort);
        server.start();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
} 

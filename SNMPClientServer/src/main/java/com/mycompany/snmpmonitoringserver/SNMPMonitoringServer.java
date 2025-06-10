package com.mycompany.snmpmonitoringserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import DatabaseManager from the client package (as per current structure)
import com.mycompany.snmpclientserver.DatabaseManager;


public class SNMPMonitoringServer {
    private static final Logger logger = LoggerFactory.getLogger(SNMPMonitoringServer.class);
    private final Map<String, ServerStatus> serverStatuses;
    private final int port;
    private DatagramSocket socket;
    private final ObjectMapper objectMapper;

    public SNMPMonitoringServer(int port) throws IOException {
        this.port = port;
        this.serverStatuses = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
    }

    public void start() {
        try {
            socket = new DatagramSocket(port);
            logger.info("SNMP Monitoring Server started on port {}", port);

            // Start listening for incoming reports
            new Thread(this::listenForReports).start();

        } catch (SocketException e) {
            logger.error("Error starting server socket: {}", e.getMessage());
        } catch (IOException e) {
            logger.error("Error starting server: {}", e.getMessage());
        }
    }

    private void listenForReports() {
        byte[] buffer = new byte[1024];
        while (!socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String receivedJson = new String(packet.getData(), 0, packet.getLength());
                processReport(receivedJson);

            } catch (IOException e) {
                if (!socket.isClosed()) {
                    logger.error("Error receiving report: {}", e.getMessage());
                }
            }
        }
    }

    private void processReport(String jsonString) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            String reportType = rootNode.get("reportType").asText();

            if ("health_report".equals(reportType)) {
                String serverName = rootNode.get("serverName").asText();
                String serverIp = rootNode.get("serverIp").asText();
                double cpuUsage = rootNode.get("cpuUsage").asDouble();
                double memoryUsage = rootNode.get("memoryUsage").asDouble();
                double diskUsage = rootNode.get("diskUsage").asDouble();
                double networkUsage = rootNode.get("networkUsage").asDouble();
                boolean isAlarmed = rootNode.get("isAlarmed").asBoolean();

                DatabaseManager.saveReport(
                        serverName, serverIp, cpuUsage, memoryUsage, diskUsage, networkUsage, isAlarmed
                );
                logger.info("Received health report from {}: CPU={:.2f}%%, Mem={:.2f}%%, Disk={:.2f}%%, Alarmed={}",
                        serverName, cpuUsage, memoryUsage, diskUsage, isAlarmed);

            } else if ("error_report".equals(reportType)) {
                String serverName = rootNode.get("serverName").asText();
                String serverIp = rootNode.get("serverIp").asText();
                String description = rootNode.get("description").asText();
                long timestamp = rootNode.get("timestamp").asLong();

                DatabaseManager.saveErrorReport(
                        serverName, serverIp, description, timestamp
                );
                logger.warn("Received error report from {}: {}", serverName, description);

            } else {
                logger.warn("Unknown report type received: {}", reportType);
            }

        } catch (IOException e) {
            logger.error("Error parsing JSON report: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing report: {}", e.getMessage());
        }
    }

    public void stop() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        DatabaseManager.close();
        logger.info("SNMP Monitoring Server stopped");
    }

    public Map<String, ServerStatus> getServerStatuses() {
        return new ConcurrentHashMap<>(serverStatuses);
    }

    public static void main(String[] args) {
        int port;
        if (args.length == 0) {
            port = 161;
            logger.info("No port specified, using default port: {}", port);
        } else if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid port number. Usage: java SNMPMonitoringServer [port]");
                System.exit(1);
                return;
            }
        } else {
            System.out.println("Usage: java SNMPMonitoringServer [port]");
            System.exit(1);
            return;
        }

        try {
            DatabaseManager.initialize();

            SNMPMonitoringServer server = new SNMPMonitoringServer(port);

            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            server.start();

            Thread.currentThread().join();

        } catch (Exception e) {
            logger.error("Failed to start SNMP Monitoring Server", e);
            System.exit(1);
        }
    }
}
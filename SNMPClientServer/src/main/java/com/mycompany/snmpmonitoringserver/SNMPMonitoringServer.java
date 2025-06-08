package com.mycompany.snmpmonitoringserver;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPMonitoringServer {
    private static final Logger logger = LoggerFactory.getLogger(SNMPMonitoringServer.class);
    private final Snmp snmp;
    private final Map<String, ServerStatus> serverStatuses;
    private final int port;
    private final DatabaseManager dbManager;

    public SNMPMonitoringServer() throws IOException {
        this(1161); // Default SNMP port changed to 1161
    }

    public SNMPMonitoringServer(int port) throws IOException {
        this.port = port;
        this.serverStatuses = new ConcurrentHashMap<>();
        this.dbManager = new DatabaseManager();

        logger.info("Initializing SNMP transport on port {}", port);
        TransportMapping transport;
        try {
            transport = new DefaultUdpTransportMapping(new UdpAddress(port));
            this.snmp = new Snmp(transport);
        } catch (IOException e) {
            logger.error("Failed to create UDP transport on port {}", port, e);
            throw e;
        }

        // إضافة معالج للتنبيهات
        snmp.addCommandResponder(new CommandResponder() {
            @Override
            public void processPdu(CommandResponderEvent event) {
                PDU pdu = event.getPDU();
                if (pdu != null) {
                    logger.info("Received SNMP trap from address: {}", event.getPeerAddress());
                    logger.info("PDU Type: {}, Request ID: {}", pdu.getType(), pdu.getRequestID());
                    processTrap(pdu);
                } else {
                    logger.warn("Received null PDU in trap");
                }
            }
        });

        try {
            logger.info("Attempting to listen on UDP port {}", port);
            transport.listen();
            logger.info("Successfully listening on UDP port {}", port);
        } catch (IOException e) {
            logger.error("FAILED to bind to UDP port {}. Another process may be using it, or there's a network issue.", port, e);
            throw e;
        }
    }

    private void processTrap(PDU pdu) {
        try {
            logger.info("Received SNMP trap, processing...");
            
            // استخراج معلومات الخادم
            String serverName = pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.2")).toString();
            String serverIP = pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.3")).toString();
            int serverPort = pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.4")).toInt();
            String alarmStatus = pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.5")).toString();
            double cpuUsage = Double.parseDouble(pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.6")).toString());
            double memoryUsage = Double.parseDouble(pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.7")).toString());
            
            logger.info("Extracted server data - Name: {}, IP: {}, Port: {}, CPU: {}%, Memory: {}%, Alarm: {}", 
                       serverName, serverIP, serverPort, 
                       String.format("%.2f", cpuUsage), 
                       String.format("%.2f", memoryUsage),
                       alarmStatus);

            // استخراج معلومات القرص
            StringBuilder diskStatus = new StringBuilder();
            int diskIndex = 1;
            while (true) {
                try {
                    String diskInfo = pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.8." + diskIndex)).toString();
                    diskStatus.append("  ").append(diskInfo).append("\n");
                    diskIndex++;
                } catch (Exception e) {
                    break;
                }
            }
            
            logger.info("Disk status extracted: {}", diskStatus.toString().trim());

            // تحديث حالة الخادم
            ServerStatus status = new ServerStatus(
                serverName, serverIP, serverPort,
                cpuUsage, memoryUsage, diskStatus.toString(),
                alarmStatus.equals("ALARMED")
            );

            // Save to database first
            logger.info("Attempting to save server status to database...");
            dbManager.saveServerStatus(status);
            logger.info("Database save operation completed");

            // Then update in-memory status
            serverStatuses.put(serverName, status);
            logger.info("In-memory status updated");

            // Display detailed health report
            logger.info("\n=== Health Report from {} ===\n" +
                       "Server Details:\n" +
                       "  Name: {}\n" +
                       "  IP: {}\n" +
                       "  Port: {}\n" +
                       "System Status:\n" +
                       "  CPU Usage: {:.2f}%\n" +
                       "  Memory Usage: {:.2f}%\n" +
                       "  Alarm Status: {}\n" +
                       "Disk Status:\n{}" +
                       "===========================",
                       serverName, serverName, serverIP, serverPort,
                       cpuUsage, memoryUsage, alarmStatus, diskStatus.toString());

        } catch (Exception e) {
            logger.error("Error processing trap: {}", e.getMessage(), e);
            logger.error("Stack trace:", e);
        }
    }

    public void start() {
        logger.info("Starting SNMP Monitoring Server on port {}", port);
        logger.info("Database connection status: {}", dbManager != null ? "Initialized" : "Not initialized");
    }

    public void stop() {
        try {
            snmp.close();
            dbManager.close();
            logger.info("SNMP Monitoring Server stopped");
        } catch (IOException e) {
            logger.error("Error stopping SNMP Monitoring Server", e);
        }
    }

    public Map<String, ServerStatus> getServerStatuses() {
        return new ConcurrentHashMap<>(serverStatuses);
    }

    public static void main(String[] args) {
        try {
            SNMPMonitoringServer server;
            if (args.length > 0) {
                try {
                    int port = Integer.parseInt(args[0]);
                    server = new SNMPMonitoringServer(port);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid port number. Using default port 1161.");
                    server = new SNMPMonitoringServer();
                }
            } else {
                server = new SNMPMonitoringServer(); // Will use default port 1161
            }

            // إضافة معالج لإغلاق الخادم بشكل نظيف
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            server.start();

            logger.info("=====================================================");
            logger.info("SNMP Monitoring Server is running. Press CTRL+C to stop.");
            logger.info("=====================================================");

            // انتظار الإغلاق
            Thread.currentThread().join();

        } catch (Exception e) {
            logger.error("FATAL: Failed to start SNMP Monitoring Server. Shutting down.", e);
            System.exit(1);
        }
    }
}
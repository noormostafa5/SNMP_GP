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
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.mycompany.snmpclientserver.DatabaseManager;
import com.mycompany.snmpclientserver.SNMPClientServer;

public class SNMPMonitoringServer implements CommandResponder {
    private static final Logger logger = LoggerFactory.getLogger(SNMPMonitoringServer.class);
    private final Map<String, ServerStatus> serverStatuses; // Keep this for potential future UI/logic
    private final int port;
    private Snmp snmp;

    public SNMPMonitoringServer(int port) throws IOException {
        this.port = port;
        this.serverStatuses = new ConcurrentHashMap<>();
        
        // Initialize SNMP Trap Receiver
        TransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/" + port));
        snmp = new Snmp(transport);
        snmp.addCommandResponder(this);
    }

    public void start() {
        try {
            snmp.listen();
            logger.info("SNMP Monitoring Server started and listening for traps on port {}", port);
        } catch (IOException e) {
            logger.error("Error starting SNMP Monitoring Server: {}", e.getMessage());
        }
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        PDU pdu = event.getPDU();
        if (pdu != null) {
            logger.info("Received PDU from {}: {}", event.getPeerAddress(), pdu.toString());
            
            OID trapOID = null;
            String serverName = "Unknown";
            String serverIp = event.getPeerAddress().toString(); // Default to peer address
            String description = "N/A";
            double cpuUsage = 0.0;
            double memoryUsage = 0.0;
            double diskUsage = 0.0;
            double networkUsage = 0.0;
            boolean isAlarmed = false;
            long timestamp = System.currentTimeMillis();

            for (VariableBinding vb : pdu.getVariableBindings()) {
                OID oid = vb.getOid();
                if (oid.equals(SnmpConstants.snmpTrapOID)) {
                    trapOID = (OID) vb.getVariable();
                } else if (oid.equals(new OID(SNMPClientServer.OID_HEALTH_REPORT + ".1")) || oid.equals(new OID(SNMPClientServer.OID_ERROR_REPORT + ".1"))) {
                    serverName = vb.getVariable().toString();
                } else if (oid.equals(new OID(SNMPClientServer.OID_HEALTH_REPORT + ".2")) || oid.equals(new OID(SNMPClientServer.OID_ERROR_REPORT + ".2"))) {
                    serverIp = vb.getVariable().toString();
                } else if (oid.equals(SNMPClientServer.OID_CPU_USAGE)) {
                    try { cpuUsage = Double.parseDouble(vb.getVariable().toString()); } catch (NumberFormatException e) { /* ignore */ }
                } else if (oid.equals(SNMPClientServer.OID_MEMORY_USAGE)) {
                    try { memoryUsage = Double.parseDouble(vb.getVariable().toString()); } catch (NumberFormatException e) { /* ignore */ }
                } else if (oid.equals(SNMPClientServer.OID_DISK_USAGE)) {
                    try { diskUsage = Double.parseDouble(vb.getVariable().toString()); } catch (NumberFormatException e) { /* ignore */ }
                } else if (oid.equals(SNMPClientServer.OID_NETWORK_USAGE)) {
                    try { networkUsage = Double.parseDouble(vb.getVariable().toString()); } catch (NumberFormatException e) { /* ignore */ }
                } else if (oid.equals(SNMPClientServer.OID_IS_ALARMED)) {
                    isAlarmed = Boolean.parseBoolean(vb.getVariable().toString());
                } else if (oid.equals(SNMPClientServer.OID_ERROR_DESCRIPTION)) {
                    description = vb.getVariable().toString();
                }
                // Add more OID mappings as needed
            }

            if (trapOID != null) {
                if (trapOID.equals(SNMPClientServer.OID_HEALTH_REPORT)) {
                    logger.info("Processing Health Report Trap from {}: CPU={:.2f}%%, Mem={:.2f}%%, Disk={:.2f}%%, Alarmed={}",
                                serverName, cpuUsage, memoryUsage, diskUsage, isAlarmed);
                    DatabaseManager.saveReport(
                        serverName, serverIp, cpuUsage, memoryUsage, diskUsage, networkUsage, isAlarmed
                    );
                } else if (trapOID.equals(SNMPClientServer.OID_ERROR_REPORT)) {
                    logger.warn("Processing Error Report Trap from {}: {}", serverName, description);
                    DatabaseManager.saveErrorReport(
                        serverName, serverIp, description, timestamp
                    );
                    DatabaseManager.handleServerError(serverName, serverIp, "error_report", description);
                } else {
                    logger.warn("Received unknown SNMP Trap OID: {}", trapOID);
                }
            } else {
                logger.warn("Received SNMP PDU without snmpTrapOID: {}", pdu.toString());
            }
        } else {
            logger.warn("Received null PDU in CommandResponderEvent");
        }
    }

    public void stop() {
        if (snmp != null) {
            try {
                snmp.close();
                logger.info("SNMP Monitoring Server stopped");
            } catch (IOException e) {
                logger.error("Error stopping SNMP Monitoring Server: {}", e.getMessage());
            }
        }
        DatabaseManager.close();
    }

    public Map<String, ServerStatus> getServerStatuses() {
        return new ConcurrentHashMap<>(serverStatuses);
    }

    public static void main(String[] args) {
        int port;
        if (args.length == 0) {
            port = SNMPClientServer.DEFAULT_TRAP_PORT; // Use client's default trap port
            logger.info("No port specified, using default trap port: {}", port);
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
            DatabaseManager.addDefaultActionRule();

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
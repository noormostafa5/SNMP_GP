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
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPMonitoringServer {
    private static final Logger logger = LoggerFactory.getLogger(SNMPMonitoringServer.class);
    private final Snmp snmp;
    private final Map<String, ServerStatus> serverStatuses;
    private final int port;
    private final DatabaseManager databaseManager;

    public SNMPMonitoringServer(int port) throws IOException {
        this.port = port;
        this.serverStatuses = new ConcurrentHashMap<>();
        this.databaseManager = new DatabaseManager();

        TransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress("127.0.0.1/" + port));
        this.snmp = new Snmp(transport);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());

        snmp.addCommandResponder(new CommandResponder() {
            @Override
            public void processPdu(CommandResponderEvent event) {
                logger.info("--- PDU Received by CommandResponder ---");
                PDU pdu = event.getPDU();
                if (pdu != null) {
                    logger.info("PDU is not null, processing trap...");
                    processTrap(pdu);
                } else {
                    logger.warn("Received a null PDU from event: {}", event);
                }
            }
        });

        transport.listen();
    }

    private void processTrap(PDU pdu) {
        logger.info("Inside processTrap method. PDU details: {}", pdu);
        try {
            String serverName = pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.2")).toString();
            String serverIP = pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.3")).toString();
            int serverPort = pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.4")).toInt();
            String alarmStatus = pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.5")).toString();
            double cpuUsage = Double.parseDouble(pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.6")).toString());
            double memoryUsage = Double.parseDouble(pdu.getVariable(new OID("1.3.6.1.4.1.9999.1.7")).toString());

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

            ServerStatus status = new ServerStatus(
                    serverName, serverIP, serverPort,
                    cpuUsage, memoryUsage, diskStatus.toString(),
                    alarmStatus.equals("ALARMED")
            );

            serverStatuses.put(serverName, status);
            logger.info("Received health report from {}:\n{}", serverName, status);

            databaseManager.saveServerReport(status);

        } catch (Exception e) {
            logger.error("Error processing trap", e);
        }
    }

    public void start() {
        logger.info("Starting SNMP Monitoring Server on port {}", port);
    }

    public void stop() {
        try {
            snmp.close();
            databaseManager.close();
            logger.info("SNMP Monitoring Server stopped");
        } catch (IOException e) {
            logger.error("Error stopping SNMP Monitoring Server", e);
        }
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
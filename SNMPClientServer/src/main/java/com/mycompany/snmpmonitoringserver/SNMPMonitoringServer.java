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
    
    public SNMPMonitoringServer(int port) throws IOException {
        this.port = port;
        this.serverStatuses = new ConcurrentHashMap<>();
        
        // تهيئة SNMP
        TransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress(port));
        this.snmp = new Snmp(transport);
        
        // إضافة معالج للتنبيهات
        snmp.addCommandResponder(new CommandResponder() {
            @Override
            public void processPdu(CommandResponderEvent event) {
                PDU pdu = event.getPDU();
                if (pdu != null) {
                    processTrap(pdu);
                }
            }
        });
        
        transport.listen();
    }
    
    private void processTrap(PDU pdu) {
        try {
            // استخراج معلومات الخادم
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
            
            // تحديث حالة الخادم
            ServerStatus status = new ServerStatus(
                serverName, serverIP, serverPort,
                cpuUsage, memoryUsage, diskStatus.toString(),
                alarmStatus.equals("ALARMED")
            );
            
            serverStatuses.put(serverName, status);
            logger.info("Received health report from {}:\n{}", serverName, status);
            
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
            logger.info("SNMP Monitoring Server stopped");
        } catch (IOException e) {
            logger.error("Error stopping SNMP Monitoring Server", e);
        }
    }
    
    public Map<String, ServerStatus> getServerStatuses() {
        return new ConcurrentHashMap<>(serverStatuses);
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SNMPMonitoringServer <port>");
            System.exit(1);
        }
        
        try {
            int port = Integer.parseInt(args[0]);
            SNMPMonitoringServer server = new SNMPMonitoringServer(port);
            
            // إضافة معالج لإغلاق الخادم بشكل نظيف
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            
            server.start();
            
            // انتظار الإغلاق
            Thread.currentThread().join();
            
        } catch (Exception e) {
            logger.error("Failed to start SNMP Monitoring Server", e);
            System.exit(1);
        }
    }
} 
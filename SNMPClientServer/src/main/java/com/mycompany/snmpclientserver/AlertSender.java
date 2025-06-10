package com.mycompany.snmpclientserver;

import org.snmp4j.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlertSender {
    private static final Logger logger = LoggerFactory.getLogger(AlertSender.class);
    private final Snmp snmp;
    private final String serverName;
    private final String serverIP;
    private final int serverPort;
    private final String monitoringServerIP;
    private final int monitoringServerPort;
<<<<<<< HEAD
    
    public AlertSender(String serverName, String serverIP, int serverPort,
                      String monitoringServerIP, int monitoringServerPort) throws IOException {
=======

    public AlertSender(String serverName, String serverIP, int serverPort,
                       String monitoringServerIP, int monitoringServerPort) throws IOException {
>>>>>>> origin/Monitor-and-Client-Server
        this.serverName = serverName;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.monitoringServerIP = monitoringServerIP;
        this.monitoringServerPort = monitoringServerPort;
<<<<<<< HEAD
        
=======

>>>>>>> origin/Monitor-and-Client-Server
        TransportMapping transport = new DefaultUdpTransportMapping();
        this.snmp = new Snmp(transport);
        transport.listen();
    }
<<<<<<< HEAD
    
    public void sendHealthReport(double cpuUsage, double memoryUsage, String diskStatus) throws IOException {
        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        
        // إضافة معلومات الخادم
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.2"), new OctetString(serverName)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.3"), new OctetString(serverIP)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.4"), new Integer32(serverPort)));
        
        // إضافة حالة التنبيه
        String alarmStatus = (cpuUsage > 90.0 || memoryUsage > 90.0) ? "ALARMED" : "CLEAR";
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.5"), new OctetString(alarmStatus)));
        
        // إضافة معلومات الموارد
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.6"), new OctetString(String.format("%.2f", cpuUsage))));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.7"), new OctetString(String.format("%.2f", memoryUsage))));
        
        // إضافة معلومات القرص
=======

    public void sendHealthReport(double cpuUsage, double memoryUsage, String diskStatus) throws IOException {
        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);

        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.2"), new OctetString(serverName)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.3"), new OctetString(serverIP)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.4"), new Integer32(serverPort)));

        String alarmStatus = (cpuUsage > 90.0 || memoryUsage > 90.0) ? "ALARMED" : "CLEAR";
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.5"), new OctetString(alarmStatus)));

        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.6"), new OctetString(String.format("%.2f", cpuUsage))));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.7"), new OctetString(String.format("%.2f", memoryUsage))));

>>>>>>> origin/Monitor-and-Client-Server
        String[] diskLines = diskStatus.split("\n");
        for (int i = 0; i < diskLines.length; i++) {
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.8." + (i + 1)), new OctetString(diskLines[i].trim())));
        }
<<<<<<< HEAD
        
        // إرسال التقرير
=======

>>>>>>> origin/Monitor-and-Client-Server
        Address targetAddress = new UdpAddress(monitoringServerIP + "/" + monitoringServerPort);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(3000);
        target.setRetries(3);
<<<<<<< HEAD
        
        snmp.send(pdu, target);
        logger.info("Health report sent to monitoring server");
    }
    
    public void sendTrap(String description) throws IOException {
        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        
        // إضافة معلومات الخادم
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.2.1"), new OctetString(serverName)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.2.2"), new OctetString(serverIP)));
        
        // إضافة وصف الخطأ
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.2.3"), new OctetString(description)));
        
        // إضافة الطابع الزمني
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.2.4"), new OctetString(timestamp)));
        
        // إرسال التنبيه
=======

        snmp.send(pdu, target);
        logger.info("Health report sent to monitoring server");
    }

    public void sendTrap(String description) throws IOException {
        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);

        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.2.1"), new OctetString(serverName)));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.2.2"), new OctetString(serverIP)));

        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.2.3"), new OctetString(description)));

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.2.4"), new OctetString(timestamp)));

>>>>>>> origin/Monitor-and-Client-Server
        Address targetAddress = new UdpAddress(monitoringServerIP + "/" + monitoringServerPort);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(3000);
        target.setRetries(3);
<<<<<<< HEAD
        
        snmp.send(pdu, target);
        logger.info("Trap sent to monitoring server: {}", description);
    }
    
=======

        snmp.send(pdu, target);
        logger.info("Trap sent to monitoring server: {}", description);
    }

>>>>>>> origin/Monitor-and-Client-Server
    public void close() {
        try {
            snmp.close();
        } catch (IOException e) {
            logger.error("Error closing SNMP connection", e);
        }
    }
} 
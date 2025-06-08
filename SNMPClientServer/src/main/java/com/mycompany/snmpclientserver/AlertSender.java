package com.mycompany.snmpclientserver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class AlertSender {
    private static final Logger logger = LoggerFactory.getLogger(AlertSender.class);
    private final Snmp snmp;
    private final String serverName;
    private final String serverIP;
    private final int serverPort;
    private final String monitoringServerIP;
    private final int monitoringServerPort;
    
    public AlertSender(String serverName, String serverIP, int serverPort,
                      String monitoringServerIP, int monitoringServerPort) throws IOException {
        this.serverName = serverName;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.monitoringServerIP = monitoringServerIP;
        this.monitoringServerPort = monitoringServerPort;
        
        TransportMapping transport = new DefaultUdpTransportMapping();
        this.snmp = new Snmp(transport);
        transport.listen();
    }
    
    public void sendHealthReport(double cpuUsage, double memoryUsage, String diskStatus) throws IOException {
        logger.info("Preparing to send health report to {}:{}", monitoringServerIP, monitoringServerPort);
        
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
        
        logger.info("PDU prepared with server info - Name: {}, IP: {}, Port: {}, CPU: {}%, Memory: {}%, Alarm: {}", 
                   serverName, serverIP, serverPort, 
                   String.format("%.2f", cpuUsage), 
                   String.format("%.2f", memoryUsage),
                   alarmStatus);
        
        // إضافة معلومات القرص
        String[] diskLines = diskStatus.split("\n");
        for (int i = 0; i < diskLines.length; i++) {
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9999.1.8." + (i + 1)), new OctetString(diskLines[i].trim())));
        }
        logger.info("Added {} disk status entries to PDU", diskLines.length);
        
        // إرسال التقرير
        Address targetAddress = new UdpAddress(monitoringServerIP + "/" + monitoringServerPort);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(3000);
        target.setRetries(3);
        
        logger.info("Sending PDU to {}:{} with community 'public'", monitoringServerIP, monitoringServerPort);
        snmp.send(pdu, target);
        logger.info("Health report sent successfully to monitoring server");
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
        Address targetAddress = new UdpAddress(monitoringServerIP + "/" + monitoringServerPort);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(3000);
        target.setRetries(3);
        
        snmp.send(pdu, target);
        logger.info("Trap sent to monitoring server: {}", description);
    }
    
    public void close() {
        try {
            snmp.close();
        } catch (IOException e) {
            logger.error("Error closing SNMP connection", e);
        }
    }
} 
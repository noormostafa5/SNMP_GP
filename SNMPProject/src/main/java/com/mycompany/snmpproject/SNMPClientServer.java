package com.mycompany.snmpproject;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
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
    private static final String MONITORING_SERVER_IP = "localhost"; // تغيير إلى IP خادم المراقبة
    private static final int MONITORING_SERVER_PORT = 162;
    private static final String COMMUNITY = "public";
    
    private Snmp snmp;
    private String clientIP;
    private Timer monitoringTimer;
    
    public SNMPClientServer() {
        try {
            // تهيئة SNMP
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();
            
            // الحصول على عنوان IP الخاص بالخادم
            clientIP = InetAddress.getLocalHost().getHostAddress();
            
            // بدء المراقبة
            startMonitoring();
            
        } catch (IOException e) {
            e.printStackTrace();
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
        
        // فحص استخدام CPU
        double cpuLoad = osBean.getSystemLoadAverage();
        if (cpuLoad > 0.8) { // 80% استخدام
            sendTrap("CPU_USAGE_HIGH", "CPU usage is above 80%: " + (cpuLoad * 100) + "%");
        }
        
        // فحص استخدام الذاكرة
        double memoryUsage = (double) memoryBean.getHeapMemoryUsage().getUsed() / 
                           memoryBean.getHeapMemoryUsage().getMax();
        if (memoryUsage > 0.8) { // 80% استخدام
            sendTrap("MEMORY_USAGE_HIGH", "Memory usage is above 80%: " + (memoryUsage * 100) + "%");
        }
        
        // فحص مساحة القرص
        java.io.File root = new java.io.File("/");
        double diskUsage = 1 - ((double) root.getFreeSpace() / root.getTotalSpace());
        if (diskUsage > 0.8) { // 80% استخدام
            sendTrap("DISK_USAGE_HIGH", "Disk usage is above 80%: " + (diskUsage * 100) + "%");
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
    
    public static void main(String[] args) {
        new SNMPClientServer();
    }
} 
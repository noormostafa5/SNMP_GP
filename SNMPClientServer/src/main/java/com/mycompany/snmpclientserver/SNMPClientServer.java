package com.mycompany.snmpclientserver;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SNMPClientServer {
    private static final Logger logger = LoggerFactory.getLogger(SNMPClientServer.class);
    private final SystemMonitor systemMonitor;
    private final AlertSender alertSender;
    private final ScheduledExecutorService scheduler;
    
    public SNMPClientServer(String serverName, String serverIP, int serverPort,
                           String monitoringServerIP, int monitoringServerPort) throws IOException {
        this.systemMonitor = new SystemMonitor();
        this.alertSender = new AlertSender(serverName, serverIP, serverPort,
                                         monitoringServerIP, monitoringServerPort);
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    public void start() {
        // Send an immediate test trap
        logger.info("Sending initial test trap...");
        try {
            double cpuUsage = systemMonitor.getCpuUsage();
            double memoryUsage = systemMonitor.getMemoryUsage();
            String diskStatus = systemMonitor.getDiskStatus();
            alertSender.sendHealthReport(cpuUsage, memoryUsage, diskStatus);
            logger.info("Initial test trap sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send initial test trap", e);
        }

        // تشغيل المراقبة كل دقيقة
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("Sending scheduled health report...");
                // جمع معلومات النظام
                double cpuUsage = systemMonitor.getCpuUsage();
                double memoryUsage = systemMonitor.getMemoryUsage();
                String diskStatus = systemMonitor.getDiskStatus();
                
                logger.info("System metrics - CPU: {}%, Memory: {}%, Disk Status: {}", 
                           String.format("%.2f", cpuUsage),
                           String.format("%.2f", memoryUsage),
                           diskStatus);
                
                // إرسال تقرير الصحة
                alertSender.sendHealthReport(cpuUsage, memoryUsage, diskStatus);
                logger.info("Health report sent successfully");
                
                // التحقق من وجود أخطاء وإرسال تنبيهات
                if (!systemMonitor.isSystemHealthy()) {
                    StringBuilder errorDesc = new StringBuilder();
                    if (cpuUsage > 90.0) {
                        errorDesc.append("CPU usage is high: ").append(String.format("%.2f", cpuUsage)).append("%\n");
                    }
                    if (memoryUsage > 90.0) {
                        errorDesc.append("Memory usage is high: ").append(String.format("%.2f", memoryUsage)).append("%\n");
                    }
                    for (SystemMonitor.DiskStatus disk : systemMonitor.getDiskStatuses()) {
                        if (disk.getUsagePercent() > 90.0) {
                            errorDesc.append("Disk ").append(disk.getMountPoint())
                                   .append(" usage is high: ").append(String.format("%.2f", disk.getUsagePercent()))
                                   .append("%\n");
                        }
                    }
                    if (errorDesc.length() > 0) {
                        logger.info("Sending alarm trap: {}", errorDesc.toString());
                        alertSender.sendTrap(errorDesc.toString());
                    }
                }
            } catch (Exception e) {
                logger.error("Error in monitoring cycle: {}", e.getMessage(), e);
            }
        }, 0, 1, TimeUnit.MINUTES);
        
        logger.info("SNMP Client Server started - Sending health reports every minute");
    }
    
    public void stop() {
        scheduler.shutdown();
        alertSender.close();
        logger.info("SNMP Client Server stopped");
    }
    
    public static void main(String[] args) {
        // Default values
        String serverName = "BTS-1";
        String serverIP = "127.0.0.1";
        int serverPort = 2162;
        String monitoringServerIP = "127.0.0.1";
        int monitoringServerPort = 1161;
        
        // Override defaults if arguments are provided
        if (args.length > 0) {
            if (args.length != 5) {
                System.out.println("Usage: java -jar SNMPClientServer.jar [serverName] [serverIP] [serverPort >1024] [monitoringServerIP] [monitoringServerPort >1024]");
                System.out.println("Using default values:");
                System.out.println("serverName: " + serverName);
                System.out.println("serverIP: " + serverIP);
                System.out.println("serverPort: " + serverPort);
                System.out.println("monitoringServerIP: " + monitoringServerIP);
                System.out.println("monitoringServerPort: " + monitoringServerPort);
            } else {
                serverName = args[0];
                serverIP = args[1];
                serverPort = Integer.parseInt(args[2]);
                monitoringServerIP = args[3];
                monitoringServerPort = Integer.parseInt(args[4]);
            }
        }
        
        try {
            SNMPClientServer server = new SNMPClientServer(serverName, serverIP, serverPort,
                                                         monitoringServerIP, monitoringServerPort);
            server.start();
            
            // إضافة hook للإغلاق النظيف
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            
        } catch (Exception e) {
            logger.error("Error starting SNMP Client Server", e);
            System.exit(1);
        }
    }
} 
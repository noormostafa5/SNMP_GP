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
        scheduler.scheduleAtFixedRate(() -> {
            try {
                double cpuUsage = systemMonitor.getCpuUsage();
                double memoryUsage = systemMonitor.getMemoryUsage();
                String diskStatus = systemMonitor.getDiskStatus();

                alertSender.sendHealthReport(cpuUsage, memoryUsage, diskStatus);

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
                    alertSender.sendTrap(errorDesc.toString());
                }
            } catch (Exception e) {
                logger.error("Error in monitoring cycle", e);
            }
        }, 0, 10, TimeUnit.SECONDS);

        logger.info("SNMP Client Server started");
    }

    public void stop() {
        scheduler.shutdown();
        alertSender.close();
        logger.info("SNMP Client Server stopped");
    }

    public static void main(String[] args) {
        String serverName;
        String serverIP;
        int serverPort;
        String monitoringServerIP;
        int monitoringServerPort;

        if (args.length == 0) {
            try {
                java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
                serverName = localMachine.getHostName();
                serverIP = localMachine.getHostAddress();
            } catch (java.net.UnknownHostException e) {
                logger.error("Failed to get local host info, using fallback defaults.", e);
                serverName = "unknown-host";
                serverIP = "127.0.0.1";
            }
            serverPort = 161;
            monitoringServerIP = "127.0.0.1";
            monitoringServerPort = 161;
            logger.info("No arguments specified, using default values with dynamic host info.");
            logger.info("Client reporting as: {} ({})", serverName, serverIP);
        } else if (args.length == 5) {
            serverName = args[0];
            serverIP = args[1];
            serverPort = Integer.parseInt(args[2]);
            monitoringServerIP = args[3];
            monitoringServerPort = Integer.parseInt(args[4]);
        } else {
            System.out.println("Usage: java -jar SNMPClientServer.jar [serverName serverIP serverPort monitoringServerIP monitoringServerPort]");
            System.exit(1);
            return;
        }

        try {
            SNMPClientServer server = new SNMPClientServer(serverName, serverIP, serverPort,
                    monitoringServerIP, monitoringServerPort);
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        } catch (Exception e) {
            logger.error("Error starting SNMP Client Server", e);
            System.exit(1);
        }
    }
} 
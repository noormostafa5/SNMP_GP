package com.mycompany.snmpmonitoringserver;

import java.time.LocalDateTime;

public class ServerStatus {
    private final String serverName;
    private final String serverIP;
    private final int serverPort;
    private final double cpuUsage;
    private final double memoryUsage;
    private final String diskStatus;
    private final boolean isAlarmed;
    private final LocalDateTime lastUpdate;
    
    public ServerStatus(String serverName, String serverIP, int serverPort,
                       double cpuUsage, double memoryUsage, String diskStatus,
                       boolean isAlarmed) {
        this.serverName = serverName;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskStatus = diskStatus;
        this.isAlarmed = isAlarmed;
        this.lastUpdate = LocalDateTime.now();
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public String getServerIP() {
        return serverIP;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public double getCpuUsage() {
        return cpuUsage;
    }
    
    public double getMemoryUsage() {
        return memoryUsage;
    }
    
    public String getDiskStatus() {
        return diskStatus;
    }
    
    public boolean isAlarmed() {
        return isAlarmed;
    }
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
    
    @Override
    public String toString() {
        return String.format("Server: %s (%s:%d)\n" +
                           "Status: %s\n" +
                           "Last Update: %s\n" +
                           "CPU Usage: %.2f%%\n" +
                           "Memory Usage: %.2f%%\n" +
                           "Disk Status:\n%s",
                           serverName, serverIP, serverPort,
                           isAlarmed ? "ALARMED" : "OK",
                           lastUpdate,
                           cpuUsage,
                           memoryUsage,
                           diskStatus);
    }
} 
package com.mycompany.snmpclientserver;

public class HealthReport {
    private String serverName;
    private String serverIp;
    private double cpuUsage;
    private double memoryUsage;
    private double diskUsage;
    private double networkUsage;

    public HealthReport() {
    }

    public HealthReport(String serverName, String serverIp, double cpuUsage,
                        double memoryUsage, double diskUsage, double networkUsage) {
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.networkUsage = networkUsage;
    }

    // Getters
    public String getServerName() {
        return serverName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public double getNetworkUsage() {
        return networkUsage;
    }

    // Setters
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public void setDiskUsage(double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public void setNetworkUsage(double networkUsage) {
        this.networkUsage = networkUsage;
    }
}
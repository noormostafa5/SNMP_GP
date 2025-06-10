package com.mycompany.snmpclientserver;

public class ErrorReport {
    private String serverName;
    private String serverIp;
    private String description;
    private long timestamp;

    public ErrorReport() {
    }

    public ErrorReport(String serverName, String serverIp, String description, long timestamp) {
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getters
    public String getServerName() {
        return serverName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
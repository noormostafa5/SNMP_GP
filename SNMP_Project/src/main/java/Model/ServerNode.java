package Model;

/*

|->  Node
		|-> CPU
		|-> Memory
		|-> IP address (Server address) IP:port
		|-> Server name (Node)
		|-> date
		|-> Action -> from class Action
		|-> Error.status
*/

import java.util.Date;

public class ServerNode {
    private int id;
    private String serverName;
    private String serverIp;
    private Date reportTime;
    private double cpuUsage;
    private double memoryUsage;
    private double diskUsage;
    private double networkUsage;
    private String status;

    public ServerNode() {
        this.serverName = "";
        this.serverIp = "";
        this.reportTime = new Date();
        this.cpuUsage = 0.0;
        this.memoryUsage = 0.0;
        this.diskUsage = 0.0;
        this.networkUsage = 0.0;
        this.status = "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }

    public String getServerIp() { return serverIp; }
    public void setServerIp(String serverIp) { this.serverIp = serverIp; }

    public Date getReportTime() { return reportTime; }
    public void setReportTime(Date reportTime) { this.reportTime = reportTime; }

    public double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }

    public double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }

    public double getDiskUsage() { return diskUsage; }
    public void setDiskUsage(double diskUsage) { this.diskUsage = diskUsage; }

    public double getNetworkUsage() { return networkUsage; }
    public void setNetworkUsage(double networkUsage) { this.networkUsage = networkUsage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

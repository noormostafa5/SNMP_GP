package Model;

/*

|->  Node
		|-> CPU
		|-> Memory
		|-> IP address (Server address) IP:port
		|-> Server name (Node)
		|-> date
		|-> Action -> from class Action
		|-> Alarm.status
*/

import java.util.Date;

public class ServerNode {

    private double cpu_Usage;
    private String ipAddress;
    private int port;
    private Date report;
    private double disk_Usage;
    private String usage;

    public ServerNode() {
      this.ipAddress="0.0.0.0";
      this.port=0;
      this.cpu_Usage=0.0;
      this.report= new Date();
      this.usage="no usage";
    }
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Date getReport() {
        return report;
    }

    public void setReport(Date report) {
        this.report = report;
    }

    public double getDisk_Usage() {
        return disk_Usage;
    }

    public void setDisk_Usage(double disk_Usage) {
        this.disk_Usage = disk_Usage;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public double getCpu_Usage() {
        return cpu_Usage;
    }

    public void setCpu_Usage(double cpu_Usage) {
        this.cpu_Usage = cpu_Usage;
    }



}

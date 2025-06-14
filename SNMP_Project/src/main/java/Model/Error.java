package Model;
/*
	|-> "Error"
		|->id
		|->server_name
		|->server_ip
		|->description
		|->report_time
 */

import java.util.Date;

public class Error {
    private int id;
    private String serverName;
    private String serverIp;
    private String description;
    private Date reportTime;

    public Error(){

    }

    public Error(String serverName, String serverIp, String description){
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.description = description;
        this.reportTime = new Date();
    }

    public Error(int id, String serverName, String serverIp, String description, Date reportTime){
        this.id = id;
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.description = description;
        this.reportTime = reportTime;
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    
    public String getServerName(){
        return serverName;
    }
    public void setServerName(String serverName){
        this.serverName = serverName;
    }
    
    public String getServerIp(){
        return serverIp;
    }
    public void setServerIp(String serverIp){
        this.serverIp = serverIp;
    }
    
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    
    public Date getReportTime(){
        return reportTime;
    }
    public void setReportTime(Date reportTime){
        this.reportTime = reportTime;
    }
}

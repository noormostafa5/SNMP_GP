package Model;
/*
	|-> "Alarm"
		|->date
		|->node.nodename
		|->status
		|-> Action -> from class Action
		date msh time
 */


import java.util.Date;

public class Alarm {
    private Date reportTime;
    private String serverName;
    private String status;

    public Alarm(){

    }

    public Alarm(Date reportTime, String serverName, String status){
        this.reportTime = reportTime;
        this.serverName = serverName;
        this.status = status;

    }
    public Date getReportTime(){
        return reportTime;
    }
    public void setReportTime(Date reportTime){
        this.reportTime = reportTime;
    }
    public String getServerName(){
        return serverName;
    }
    public void setServerName(){
        this.serverName = serverName;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(){
        this.status = status;
    }
}

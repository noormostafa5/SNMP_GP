package Model;

public class ServerNode {
    private int id;
    private String nodeName;
    private String nodeIp;
    private int nodePort;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServerName() {
        return nodeName;
    }

    public void setServerName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getServerIp() {
        return nodeIp;
    }

    public void setServerIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public int getPort() {
        return nodePort;
    }

    public void setPort(int nodePort) {
        this.nodePort = nodePort;
    }

    // For backward compatibility with JSP
    public int getServerPort() {
        return nodePort;
    }
}
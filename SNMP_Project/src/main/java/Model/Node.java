package Model;

public class Node {
    private int nodeId;
    private String nodeName;
    private String nodeIp;
    private int nodePort;

    public Node() {
        this.nodeId = 0;
        this.nodeName = "no name provided";
        this.nodeIp = "0.0.0.0";
        this.nodePort = 0;
    }

    public Node(String nodeName, String nodeIp, int nodePort) {
        this.nodeName = nodeName;
        this.nodeIp = nodeIp;
        this.nodePort = nodePort;
    }

    public Node(int nodeId, String nodeName, String nodeIp, int nodePort) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.nodeIp = nodeIp;
        this.nodePort = nodePort;
    }

    public int getNodeId() {
        return nodeId;
    }
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeIp() {
        return nodeIp;
    }
    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public int getNodePort() {
        return nodePort;
    }
    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }
}

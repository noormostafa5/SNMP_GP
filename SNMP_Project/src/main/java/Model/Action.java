package Model;

public class Action {
    private int id;
    private int serverId;
    private String actionType;
    private String rootEmail;
    private String nodeName; // this is not from action_rules, but useful for dropdown or display

    public Action() {
        // default values align with DB defaults
        this.actionType = "sending an email to the root user of the clientserver";
        this.rootEmail = "mohamedmeselhy999@gmail.com";
    }

    public Action(int id, int serverId, String actionType, String rootEmail, String nodeName) {
        this.id = id;
        this.serverId = serverId;
        this.actionType = actionType;
        this.rootEmail = rootEmail;
        this.nodeName = nodeName;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getServerId() {
        return serverId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getRootEmail() {
        return rootEmail;
    }

    public String getNodeName() {
        return nodeName;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setRootEmail(String rootEmail) {
        this.rootEmail = rootEmail;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}

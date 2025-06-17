package Database_Connection;

import Model.Action;
import Model.Node;
import Model.ServerNode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActionDatabaseOperation {

    private static final String SELECT_ALL_RULES_WITH_NODE_NAME =
            "SELECT ar.id, ar.server_id, ar.action_type, ar.root_email, n.node_name " +
                    "FROM action_rules ar " +
                    "JOIN nodes n ON ar.server_id = n.node_id";

    private static final String SELECT_ALL_NODES =
            "SELECT node_id, node_name FROM nodes";

    private static final String INSERT_ACTION_RULE =
            "INSERT INTO action_rules (server_id, action_type, root_email) VALUES (?, ?, ?)";

    private static final String UPDATE_ACTION_RULE =
            "UPDATE action_rules SET server_id = ?, action_type = ?, root_email = ? WHERE id = ?";

    private static final String DELETE_ACTION_RULE =
            "DELETE FROM action_rules WHERE id = ?";

    // Fetch all action rules with corresponding node names
    public static List<Action> getAllActionRulesWithNodeNames() throws SQLException {
        List<Action> actions = new ArrayList<>();
        DatabaseMetaData DBConnection;
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_RULES_WITH_NODE_NAME);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Action action = new Action();
                action.setId(rs.getInt("id"));
                action.setServerId(rs.getInt("server_id"));
                action.setActionType(rs.getString("action_type"));
                action.setRootEmail(rs.getString("root_email"));
                action.setNodeName(rs.getString("node_name")); // additional attribute
                actions.add(action);
            }
        }
        System.out.println("Fetched rules: " + actions.size());
        return actions;
    }

    // Fetch all available nodes for dropdown
    public static List<Node> getAllNodes() throws SQLException {
        List<Node> nodes = new ArrayList<>();

        String sql = "SELECT node_id, node_name FROM nodes";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Node node = new Node();
                node.setNodeId(rs.getInt("node_id"));
                node.setNodeName(rs.getString("node_name"));
                nodes.add(node);
            }
        }

        return nodes;
    }


    // Insert a new action rule
    public static void insertActionRule(Action action) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_ACTION_RULE)) {
            ps.setInt(1, action.getServerId());
            ps.setString(2, action.getActionType());
            ps.setString(3, action.getRootEmail());
            ps.executeUpdate();
        }
    }

    // Update an existing action rule
    public static void updateActionRule(Action action) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_ACTION_RULE)) {
            ps.setInt(1, action.getServerId());
            ps.setString(2, action.getActionType());
            ps.setString(3, action.getRootEmail());
            ps.setInt(4, action.getId());
            ps.executeUpdate();
        }
    }

    // Delete an action rule by ID
    public static void deleteActionRule(int id) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ACTION_RULE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}

package Database_Connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Model.ServerNode;

public class ServerDatabaseOperation {

    private static final String INSERT_SQL =
            "INSERT INTO nodes (node_name, node_ip, node_port) VALUES (?, ?::inet, ?)";

    private static final String SELECT_BY_IP_SQL =
            "SELECT * FROM nodes WHERE node_ip = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM nodes";

    private static final String UPDATE_SQL =
            "UPDATE nodes SET node_name = ?, node_ip = ?::inet, node_port = ? WHERE node_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM nodes WHERE node_ip = ?::inet";

    private static final String SEARCH_BY_NAME_OR_IP_SQL =
            "SELECT * FROM nodes WHERE node_name ILIKE ? OR node_ip::TEXT ILIKE ?";

    public static boolean createServerNode(ServerNode serverNode) throws SQLException {
        System.out.println("=== ServerDatabaseOperation.createServerNode() called ===");
        System.out.println("ServerNode details: " + serverNode.getServerName() + ", " + serverNode.getServerIp() + ", " + serverNode.getPort());

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {

            pstmt.setString(1, serverNode.getServerName());
            pstmt.setString(2, serverNode.getServerIp());
            pstmt.setInt(3, serverNode.getPort());

            System.out.println("Executing INSERT SQL: " + INSERT_SQL);
            int result = pstmt.executeUpdate();
            System.out.println("INSERT result: " + result + " rows affected");

            return result > 0;
        } catch (SQLException e) {
            System.out.println("SQL Exception in createServerNode: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            throw e;
        }
    }

    public static ServerNode getServerNode(String ipAddress) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_IP_SQL)) {

            pstmt.setString(1, ipAddress);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToServerNode(rs);
                }
            }
        }
        return null;
    }

    public static List<ServerNode> getAllServerNodes() throws SQLException {
        List<ServerNode> nodes = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                nodes.add(mapResultSetToServerNode(rs));
            }
        }
        return nodes;
    }

    public static boolean updateServerNode(ServerNode serverNode) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {

            pstmt.setString(1, serverNode.getServerName());
            pstmt.setString(2, serverNode.getServerIp());
            pstmt.setInt(3, serverNode.getPort());
            pstmt.setInt(4, serverNode.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean deleteServerNode(String ipAddress) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_SQL)) {

            pstmt.setString(1, ipAddress);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static List<ServerNode> searchByNameOrIp(String pattern) throws SQLException {
        List<ServerNode> nodes = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_BY_NAME_OR_IP_SQL)) {

            pstmt.setString(1, "%" + pattern + "%");
            pstmt.setString(2, "%" + pattern + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    nodes.add(mapResultSetToServerNode(rs));
                }
            }
        }
        return nodes;
    }

    private static ServerNode mapResultSetToServerNode(ResultSet rs) throws SQLException {
        System.out.println("=== Mapping ResultSet to ServerNode ===");
        ServerNode node = new ServerNode();

        try {
            node.setId(rs.getInt("node_id"));
            System.out.println("Mapped node_id: " + node.getId());

            node.setServerName(rs.getString("node_name"));
            System.out.println("Mapped node_name: " + node.getServerName());

            node.setServerIp(rs.getString("node_ip"));
            System.out.println("Mapped node_ip: " + node.getServerIp());

            node.setPort(rs.getInt("node_port"));
            System.out.println("Mapped node_port: " + node.getPort());

            System.out.println("=== Mapping complete ===");
        } catch (SQLException e) {
            System.out.println("Error mapping ResultSet to ServerNode: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            throw e;
        }

        return node;
    }

    private static Connection getConnection() throws SQLException {
        return DataBaseConnection.getConnection();
    }
}

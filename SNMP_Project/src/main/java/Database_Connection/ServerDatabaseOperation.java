package Database_Connection;


import Model.ServerNode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ServerDatabaseOperation {
    //changes
    private static final String INSERT_SQL =
            "INSERT INTO server_reports (cpu_usage, ip_address, port, report_date, disk_usage, usage) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_IP_PORT_SQL =
            "SELECT * FROM server_reports WHERE ip_address = ? AND port = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM server_reports";

    private static final String UPDATE_SQL =
            "UPDATE server_reports SET cpu_usage = ?, disk_usage = ?, usage = ?, report_date = ?" +
            "WHERE ip_address = ? AND port = ?";

    private static final String DELETE_SQL =
            "DELETE FROM server_reports WHERE ip_address = ? AND port = ?";

    private static final String SEARCH_BY_USAGE_SQL=
            "SELECT * FROM server_reports WHERE usage ILIKE ?";


    public static boolean createServerNode(ServerNode serverNode) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {

            setServerNodeParameters(pstmt, serverNode);
            return pstmt.executeUpdate() > 0;
        }

    }
    public static ServerNode getServerNode(String ipAddress, int port) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_IP_PORT_SQL)) {

            pstmt.setString(1, ipAddress);
            pstmt.setInt(2, port);

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

        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                nodes.add(mapResultSetToServerNode(rs));
            }
        }
        return nodes;
    }
    public static boolean updateServerNode(ServerNode serverNode) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {

            pstmt.setDouble(1, serverNode.getCpu_Usage());
            pstmt.setDouble(2, serverNode.getDisk_Usage());
            pstmt.setString(3, serverNode.getUsage());
            pstmt.setTimestamp(4, new Timestamp(serverNode.getReport().getTime()));
            pstmt.setString(5, serverNode.getIpAddress());
            pstmt.setInt(6, serverNode.getPort());

            return pstmt.executeUpdate() > 0;
        }
    }

    public static List<ServerNode> searchByUsage(String usagePattern) throws SQLException {
        List<ServerNode> nodes = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_BY_USAGE_SQL)) {

            pstmt.setString(1, "%" + usagePattern + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    nodes.add(mapResultSetToServerNode(rs));
                }
            }
        }
        return nodes;
    }

    public static boolean deleteServerNode(String ipAddress, int port) throws SQLException{
        try (Connection conn = DataBaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(DELETE_SQL)) {
            pstmt.setString(1, ipAddress);
            pstmt.setInt(2, port);
            return pstmt.executeUpdate() > 0;
        }
    }


    private static void setServerNodeParameters(PreparedStatement pstmt, ServerNode serverNode)
            throws SQLException {
        pstmt.setDouble(1, serverNode.getCpu_Usage());
        pstmt.setString(2, serverNode.getIpAddress());
        pstmt.setInt(3, serverNode.getPort());
        pstmt.setTimestamp(4, new Timestamp(serverNode.getReport().getTime()));
        pstmt.setDouble(5, serverNode.getDisk_Usage());
        pstmt.setString(6, serverNode.getUsage());
    }


    private static ServerNode mapResultSetToServerNode(ResultSet rs) throws SQLException {
        ServerNode node = new ServerNode();
        node.setCpu_Usage(rs.getDouble("cpu_usage"));
        node.setIpAddress(rs.getString("ip_address"));
        node.setPort(rs.getInt("port"));
        node.setReport(new Date(rs.getTimestamp("report_date").getTime()));
        node.setDisk_Usage(rs.getDouble("disk_usage"));
        node.setUsage(rs.getString("usage"));
        return node;
    }
}


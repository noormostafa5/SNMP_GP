package Database_Connection;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import Model.ServerNode;



public class ServerDatabaseOperation {

    private static final String INSERT_SQL =
            "INSERT INTO server_reports (server_name, server_ip, report_time, cpu_usage, memory_usage, disk_usage, network_usage, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_IP_PORT_SQL =
            "SELECT * FROM server_reports WHERE server_ip = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM server_reports";

    private static final String UPDATE_SQL =
            "UPDATE server_reports SET server_name = ?, server_ip = ?, report_time = ?, cpu_usage = ?, memory_usage = ?, disk_usage = ?, network_usage = ?, status = ? " +
            "WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM server_reports WHERE server_ip = ?";

    private static final String SEARCH_BY_USAGE_SQL=
            "SELECT * FROM server_reports WHERE server_name ILIKE ? OR server_ip ILIKE ?";


    public static boolean createServerNode(ServerNode serverNode) throws SQLException {
        System.out.println("=== ServerDatabaseOperation.createServerNode() called ===");
        System.out.println("ServerNode details: " + serverNode.getServerName() + ", " + serverNode.getServerIp() + ", " + serverNode.getStatus());
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {

            setServerNodeParameters(pstmt, serverNode);
            
            System.out.println("Executing INSERT SQL: " + INSERT_SQL);
            int result = pstmt.executeUpdate();
            System.out.println("INSERT result: " + result + " rows affected");
            
            return result > 0;
        }
    }
    public static ServerNode getServerNode(String ipAddress) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_IP_PORT_SQL)) {

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
        String sql = "UPDATE server_reports SET server_name = ?, server_ip = ?, report_time = ?, cpu_usage = ?, memory_usage = ?, disk_usage = ?, network_usage = ?, status = ? WHERE id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, serverNode.getServerName());
            pstmt.setString(2, serverNode.getServerIp());
            pstmt.setTimestamp(3, new Timestamp(serverNode.getReportTime().getTime()));
            pstmt.setDouble(4, serverNode.getCpuUsage());
            pstmt.setDouble(5, serverNode.getMemoryUsage());
            pstmt.setDouble(6, serverNode.getDiskUsage());
            pstmt.setDouble(7, serverNode.getNetworkUsage());
            pstmt.setString(8, serverNode.getStatus());
            pstmt.setInt(9, serverNode.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public static List<ServerNode> searchByUsage(String usagePattern) throws SQLException {
        List<ServerNode> nodes = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_BY_USAGE_SQL)) {

            pstmt.setString(1, "%" + usagePattern + "%");
            pstmt.setString(2, "%" + usagePattern + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    nodes.add(mapResultSetToServerNode(rs));
                }
            }
        }
        return nodes;
    }

    public static boolean deleteServerNode(String ipAddress) throws SQLException{
        try (Connection conn = DataBaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(DELETE_SQL)) {
            pstmt.setString(1, ipAddress);
            return pstmt.executeUpdate() > 0;
        }
    }


    private static void setServerNodeParameters(PreparedStatement pstmt, ServerNode serverNode)
            throws SQLException {
        System.out.println("Setting parameters for PreparedStatement:");
        System.out.println("  serverName: " + serverNode.getServerName());
        System.out.println("  serverIp: " + serverNode.getServerIp());
        System.out.println("  reportTime: " + serverNode.getReportTime());
        System.out.println("  cpuUsage: " + serverNode.getCpuUsage());
        System.out.println("  memoryUsage: " + serverNode.getMemoryUsage());
        System.out.println("  diskUsage: " + serverNode.getDiskUsage());
        System.out.println("  networkUsage: " + serverNode.getNetworkUsage());
        System.out.println("  status: " + serverNode.getStatus());
        
        pstmt.setString(1, serverNode.getServerName());
        pstmt.setString(2, serverNode.getServerIp());
        pstmt.setTimestamp(3, new Timestamp(serverNode.getReportTime().getTime()));
        pstmt.setDouble(4, serverNode.getCpuUsage());
        pstmt.setDouble(5, serverNode.getMemoryUsage());
        pstmt.setDouble(6, serverNode.getDiskUsage());
        pstmt.setDouble(7, serverNode.getNetworkUsage());
        pstmt.setString(8, serverNode.getStatus());
        
        System.out.println("All parameters set successfully");
    }


    private static ServerNode mapResultSetToServerNode(ResultSet rs) throws SQLException {
        ServerNode node = new ServerNode();
        node.setId(rs.getInt("id"));
        node.setServerName(rs.getString("server_name"));
        node.setServerIp(rs.getString("server_ip"));
        node.setReportTime(rs.getTimestamp("report_time"));
        node.setCpuUsage(rs.getDouble("cpu_usage"));
        node.setMemoryUsage(rs.getDouble("memory_usage"));
        node.setDiskUsage(rs.getDouble("disk_usage"));
        node.setNetworkUsage(rs.getDouble("network_usage"));
        node.setStatus(rs.getString("status"));
        return node;
    }
}


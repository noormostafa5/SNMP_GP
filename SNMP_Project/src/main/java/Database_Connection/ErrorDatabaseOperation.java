package Database_Connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Model.Error;

public class ErrorDatabaseOperation {

    // SQL queries for error_reports table
    private static final String INSERT_SQL = 
            "INSERT INTO error_reports (server_name, server_ip, description) VALUES (?, ?, ?)";
    
    private static final String SELECT_ALL_SQL = 
            "SELECT * FROM error_reports ORDER BY report_time DESC";
    
    private static final String SELECT_BY_ID_SQL = 
            "SELECT * FROM error_reports WHERE id = ?";
    
    private static final String SELECT_BY_SERVER_IP_SQL = 
            "SELECT * FROM error_reports WHERE server_ip = ?";
    
    private static final String UPDATE_SQL = 
            "UPDATE error_reports SET server_name = ?, server_ip = ?, description = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
            "DELETE FROM error_reports WHERE id = ?";
    
    private static final String DELETE_BY_SERVER_IP_SQL = 
            "DELETE FROM error_reports WHERE server_ip = ?";

    /**
     * Create a new error report
     * @param error The error object to insert
     * @return true if successful, false otherwise
     */
    public static boolean createError(Error error) throws SQLException {
        System.out.println("=== ErrorDatabaseOperation.createError() called ===");
        System.out.println("Error details: " + error.getServerName() + ", " + error.getServerIp() + ", " + error.getDescription());
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {
            
            pstmt.setString(1, error.getServerName());
            pstmt.setString(2, error.getServerIp());
            pstmt.setString(3, error.getDescription());
            
            System.out.println("Executing INSERT SQL: " + INSERT_SQL);
            int result = pstmt.executeUpdate();
            System.out.println("INSERT result: " + result + " rows affected");
            
            return result > 0;
        }
    }

    /**
     * Get all error reports ordered by report_time DESC
     * @return List of all error reports
     */
    public static List<Error> getAllErrors() throws SQLException {
        System.out.println("=== ErrorDatabaseOperation.getAllErrors() called ===");
        List<Error> errors = new ArrayList<>();
        
        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            
            System.out.println("Executing SELECT SQL: " + SELECT_ALL_SQL);
            
            while (rs.next()) {
                Error error = mapResultSetToError(rs);
                errors.add(error);
                System.out.println("Found error: ID=" + error.getId() + ", Server=" + error.getServerName() + ", IP=" + error.getServerIp());
            }
            
            System.out.println("Total errors found: " + errors.size());
        }
        return errors;
    }

    /**
     * Get error report by ID
     * @param id The error ID
     * @return Error object or null if not found
     */
    public static Error getErrorById(int id) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToError(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all error reports for a specific server IP
     * @param serverIp The server IP address
     * @return List of error reports for the server
     */
    public static List<Error> getErrorsByServerIp(String serverIp) throws SQLException {
        List<Error> errors = new ArrayList<>();
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_SERVER_IP_SQL)) {
            
            pstmt.setString(1, serverIp);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    errors.add(mapResultSetToError(rs));
                }
            }
        }
        return errors;
    }

    /**
     * Update an existing error report
     * @param error The error object with updated values
     * @return true if successful, false otherwise
     */
    public static boolean updateError(Error error) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {
            
            pstmt.setString(1, error.getServerName());
            pstmt.setString(2, error.getServerIp());
            pstmt.setString(3, error.getDescription());
            pstmt.setInt(4, error.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete error report by ID
     * @param id The error ID to delete
     * @return true if successful, false otherwise
     */
    public static boolean deleteError(int id) throws SQLException {
        System.out.println("=== ErrorDatabaseOperation.deleteError() called ===");
        System.out.println("Attempting to delete error with ID: " + id);
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_SQL)) {
            
            pstmt.setInt(1, id);
            
            System.out.println("Executing DELETE SQL: " + DELETE_SQL + " with ID=" + id);
            int result = pstmt.executeUpdate();
            System.out.println("DELETE result: " + result + " rows affected");
            
            return result > 0;
        }
    }

    /**
     * Delete all error reports for a specific server IP
     * @param serverIp The server IP address
     * @return true if successful, false otherwise
     */
    public static boolean deleteErrorsByServerIp(String serverIp) throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_BY_SERVER_IP_SQL)) {
            
            pstmt.setString(1, serverIp);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Get total count of error reports
     * @return Total number of error reports
     */
    public static int getErrorCount() throws SQLException {
        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM error_reports")) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Map ResultSet to Error object
     * @param rs The ResultSet containing error data
     * @return Error object
     */
    private static Error mapResultSetToError(ResultSet rs) throws SQLException {
        Error error = new Error();
        error.setId(rs.getInt("id"));
        error.setServerName(rs.getString("server_name"));
        error.setServerIp(rs.getString("server_ip"));
        error.setDescription(rs.getString("description"));
        error.setReportTime(rs.getTimestamp("report_time"));
        return error;
    }
}

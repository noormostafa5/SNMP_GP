package Database_Connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Model.User;

public class DatabaseOperation {
    // Validation method for nationalID
    private static boolean isValidNationalID(String nationalID) {
        return nationalID != null && nationalID.matches("^\\d{20}$");
    }

    // CREATE operation
    public static boolean createUser(User user) throws SQLException {
        if (!isValidNationalID(user.getNationalID())) {
            throw new IllegalArgumentException("Invalid National ID format. Must be 20 digits.");
        }

        String sql = "INSERT INTO person (firstName, lastName, phoneNumber, nationalID, DOB) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getfName());
            pstmt.setString(2, user.getlName());
            pstmt.setInt(3, user.getPhoneNumber());
            pstmt.setString(4, user.getNationalID());
            pstmt.setDate(5, new java.sql.Date(user.getDOB().getTime()));
            
            return pstmt.executeUpdate() > 0;
        }
    }

    // READ operations
    public static User getUserByNationalID(String nationalID) throws SQLException {
        if (!isValidNationalID(nationalID)) {
            throw new IllegalArgumentException("Invalid National ID format. Must be 20 digits.");
        }

        String sql = "SELECT * FROM person WHERE nationalID = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nationalID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setfName(rs.getString("firstName"));
                user.setlName(rs.getString("lastName"));
                user.setPhoneNumber(rs.getInt("phoneNumber"));
                user.setNationalID(rs.getString("nationalID"));
                user.setDOB(rs.getDate("DOB"));
                return user;
            }
            return null;
        }
    }

    public static List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM person";
        
        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setfName(rs.getString("firstName"));
                user.setlName(rs.getString("lastName"));
                user.setPhoneNumber(rs.getInt("phoneNumber"));
                user.setNationalID(rs.getString("nationalID"));
                user.setDOB(rs.getDate("DOB"));
                users.add(user);
            }
        }
        return users;
    }

    // UPDATE operation
    public static boolean updateUser(User user) throws SQLException {
        if (!isValidNationalID(user.getNationalID())) {
            throw new IllegalArgumentException("Invalid National ID format. Must be 20 digits.");
        }

        String sql = "UPDATE person SET firstName = ?, lastName = ?, phoneNumber = ?, DOB = ? WHERE nationalID = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getfName());
            pstmt.setString(2, user.getlName());
            pstmt.setInt(3, user.getPhoneNumber());
            pstmt.setDate(4, new java.sql.Date(user.getDOB().getTime()));
            pstmt.setString(5, user.getNationalID());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    // DELETE operation
    public static boolean deleteUser(String nationalID) throws SQLException {
        if (!isValidNationalID(nationalID)) {
            throw new IllegalArgumentException("Invalid National ID format. Must be 20 digits.");
        }

        String sql = "DELETE FROM person WHERE nationalID = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nationalID);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Search operations
    public static List<User> searchByFirstName(String firstName) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM person WHERE firstName ILIKE ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + firstName + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                User user = new User();
                user.setfName(rs.getString("firstName"));
                user.setlName(rs.getString("lastName"));
                user.setPhoneNumber(rs.getInt("phoneNumber"));
                user.setNationalID(rs.getString("nationalID"));
                user.setDOB(rs.getDate("DOB"));
                users.add(user);
            }
        }
        return users;
    }

    public static List<User> searchByLastName(String lastName) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM person WHERE lastName ILIKE ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + lastName + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                User user = new User();
                user.setfName(rs.getString("firstName"));
                user.setlName(rs.getString("lastName"));
                user.setPhoneNumber(rs.getInt("phoneNumber"));
                user.setNationalID(rs.getString("nationalID"));
                user.setDOB(rs.getDate("DOB"));
                users.add(user);
            }
        }
        return users;
    }

    // LoginServlet operation
    public static User loginUser(String firstName, String lastName, String password) throws SQLException {
        String sql = "SELECT * FROM person WHERE firstName = ? AND lastName = ? AND password = ?";
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, password); // In a real application, you should use password hashing
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setfName(rs.getString("firstName"));
                user.setlName(rs.getString("lastName"));
                user.setPhoneNumber(rs.getInt("phoneNumber"));
                user.setNationalID(rs.getString("nationalID"));
                user.setDOB(rs.getDate("DOB"));
                return user;
            }
            return null;
        }
    }
}

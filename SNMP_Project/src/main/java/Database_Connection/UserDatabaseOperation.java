package Database_Connection;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import Model.User;

public class UserDatabaseOperation {
    // Validation method for nationalID
    private static boolean isValidNationalID(String nationalID) {
        return nationalID != null && nationalID.matches("^\\d{20}$");
    }

    // Password hashing method
    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    // CREATE operation
    public static boolean createUser(User user, String password) throws SQLException, NoSuchAlgorithmException {
        if (!isValidNationalID(user.getNationalID())) {
            throw new IllegalArgumentException("Invalid National ID format. Must be 20 digits.");
        }

        // Debug: Print password before hashing
        System.out.println("=== DATABASE DEBUG ===");
        System.out.println("Password before hashing: " + password);
        System.out.println("Hashed password: " + hashPassword(password));
        System.out.println("=====================");

        String sql = "INSERT INTO person (firstName, lastName, phoneNumber, nationalID, DOB, password) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getfName());
            pstmt.setString(2, user.getlName());
            pstmt.setInt(3, user.getPhoneNumber());
            pstmt.setString(4, user.getNationalID());
            pstmt.setDate(5, new java.sql.Date(user.getDOB().getTime()));
            pstmt.setString(6, hashPassword(password));

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
        String sql = "SELECT * FROM person WHERE firstname ILIKE ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + firstName + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setfName(rs.getString("firstname"));
                user.setlName(rs.getString("lastname"));
                user.setPhoneNumber(rs.getInt("phonenumber"));
                user.setNationalID(rs.getString("nationalid"));
                user.setDOB(rs.getDate("dob"));
                users.add(user);
            }
        }
        return users;
    }

    public static List<User> searchByLastName(String lastName) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM person WHERE lastname ILIKE ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + lastName + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setfName(rs.getString("firstname"));
                user.setlName(rs.getString("lastname"));
                user.setPhoneNumber(rs.getInt("phonenumber"));
                user.setNationalID(rs.getString("nationalid"));
                user.setDOB(rs.getDate("dob"));
                users.add(user);
            }
        }
        return users;
    }

    // LoginServlet operation
    public static User loginUser(String firstName, String lastName, String password) throws SQLException, NoSuchAlgorithmException {
        String sql = "SELECT * FROM person WHERE firstname = ? AND lastname = ? AND password = ?";

        // Debug: Print login attempt details
        System.out.println("=== LOGIN ATTEMPT DEBUG ===");
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Password (plain): " + password);
        System.out.println("Password (hashed): " + hashPassword(password));
        System.out.println("SQL Query: " + sql);
        System.out.println("===========================");

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // First try with plain text password
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Found user with plain text password
                User user = new User();
                user.setfName(rs.getString("firstname"));
                user.setlName(rs.getString("lastname"));
                user.setPhoneNumber(rs.getInt("phonenumber"));
                user.setNationalID(rs.getString("nationalid"));
                user.setDOB(rs.getDate("dob"));
                
                // Debug: Print database values
                System.out.println("=== DATABASE VALUES DEBUG ===");
                System.out.println("DB firstname: " + rs.getString("firstname"));
                System.out.println("DB lastname: " + rs.getString("lastname"));
                System.out.println("DB phonenumber: " + rs.getInt("phonenumber"));
                System.out.println("DB nationalid: " + rs.getString("nationalid"));
                System.out.println("DB dob: " + rs.getDate("dob"));
                System.out.println("DB password: " + rs.getString("password"));
                System.out.println("Login successful with plain text password");
                System.out.println("=============================");
                
                System.out.println("=== LOGIN SUCCESS ===");
                System.out.println("User found: " + user.getfName() + " " + user.getlName());
                System.out.println("====================");
                return user;
            }

            // If not found with plain text, try with hashed password
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, hashPassword(password));

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Found user with hashed password
                User user = new User();
                user.setfName(rs.getString("firstname"));
                user.setlName(rs.getString("lastname"));
                user.setPhoneNumber(rs.getInt("phonenumber"));
                user.setNationalID(rs.getString("nationalid"));
                user.setDOB(rs.getDate("dob"));
                
                // Debug: Print database values
                System.out.println("=== DATABASE VALUES DEBUG ===");
                System.out.println("DB firstname: " + rs.getString("firstname"));
                System.out.println("DB lastname: " + rs.getString("lastname"));
                System.out.println("DB phonenumber: " + rs.getInt("phonenumber"));
                System.out.println("DB nationalid: " + rs.getString("nationalid"));
                System.out.println("DB dob: " + rs.getDate("dob"));
                System.out.println("DB password: " + rs.getString("password"));
                System.out.println("Login successful with hashed password");
                System.out.println("=============================");
                
                System.out.println("=== LOGIN SUCCESS ===");
                System.out.println("User found: " + user.getfName() + " " + user.getlName());
                System.out.println("====================");
                return user;
            } else {
                System.out.println("=== LOGIN FAILED ===");
                System.out.println("No user found with these credentials (tried both plain text and hashed)");
                System.out.println("====================");
                return null;
            }
        }
    }

    // Method to update all plain text passwords to hashed passwords
    public static void updateAllPasswordsToHashed() throws SQLException, NoSuchAlgorithmException {
        String sql = "UPDATE person SET password = ? WHERE password != ? AND password != ? AND password != ?";
        
        // Get the hashed versions of common passwords
        String hashedPassword123 = hashPassword("password123");
        String hashedPassword456 = hashPassword("password456");
        String hashedPassword789 = hashPassword("password789");
        
        System.out.println("=== UPDATING PASSWORDS ===");
        System.out.println("Hashed password123: " + hashedPassword123);
        System.out.println("Hashed password456: " + hashedPassword456);
        System.out.println("Hashed password789: " + hashedPassword789);
        
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Update all plain text "password123" to hashed version
            pstmt.setString(1, hashedPassword123);
            pstmt.setString(2, hashedPassword123); // Don't update already hashed passwords
            pstmt.setString(3, hashedPassword456);
            pstmt.setString(4, hashedPassword789);
            
            int updatedRows = pstmt.executeUpdate();
            System.out.println("Updated " + updatedRows + " passwords to hashed version");
            System.out.println("=========================");
        }
    }
}

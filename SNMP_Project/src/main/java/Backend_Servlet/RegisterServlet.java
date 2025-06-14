package Backend_Servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import Database_Connection.UserDatabaseOperation;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get parameters from request
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String nationalID = request.getParameter("nationalID");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phoneNumberStr = request.getParameter("phoneNumber");
        String dobStr = request.getParameter("dob");

        // Debug: Print received parameters
        System.out.println("=== REGISTRATION DEBUG ===");
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("National ID: " + nationalID);
        System.out.println("Password: " + password);
        System.out.println("Confirm Password: " + confirmPassword);
        System.out.println("==========================");

        // Validate required parameters
        if (firstName == null || lastName == null || nationalID == null || 
            password == null || confirmPassword == null ||
            firstName.trim().isEmpty() || lastName.trim().isEmpty() || 
            nationalID.trim().isEmpty() || password.trim().isEmpty() || 
            confirmPassword.trim().isEmpty()) {
            request.setAttribute("error", "All required fields must be filled");
            request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
            return;
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            request.setAttribute("firstName", firstName);
            request.setAttribute("lastName", lastName);
            request.setAttribute("nationalID", nationalID);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
            return;
        }

        // Validate national ID format (20 digits)
        if (!nationalID.matches("^\\d{20}$")) {
            request.setAttribute("error", "National ID must be exactly 20 digits");
            request.setAttribute("firstName", firstName);
            request.setAttribute("lastName", lastName);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
            return;
        }

        // Validate password strength (at least 6 characters)
        if (password.length() < 6) {
            request.setAttribute("error", "Password must be at least 6 characters long");
            request.setAttribute("firstName", firstName);
            request.setAttribute("lastName", lastName);
            request.setAttribute("nationalID", nationalID);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
            return;
        }

        try {
            // Check if user already exists
            User existingUser = UserDatabaseOperation.getUserByNationalID(nationalID);
            if (existingUser != null) {
                request.setAttribute("error", "User with this National ID already exists");
                request.setAttribute("firstName", firstName);
                request.setAttribute("lastName", lastName);
                request.setAttribute("email", email);
                request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
                return;
            }

            // Create new user
            User user = new User();
            user.setfName(firstName.trim());
            user.setlName(lastName.trim());
            user.setNationalID(nationalID.trim());

            // Set phone number if provided
            if (phoneNumberStr != null && !phoneNumberStr.trim().isEmpty()) {
                try {
                    int phoneNumber = Integer.parseInt(phoneNumberStr.trim());
                    user.setPhoneNumber(phoneNumber);
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "Invalid phone number format");
                    request.setAttribute("firstName", firstName);
                    request.setAttribute("lastName", lastName);
                    request.setAttribute("nationalID", nationalID);
                    request.setAttribute("email", email);
                    request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
                    return;
                }
            }

            // Set date of birth if provided
            if (dobStr != null && !dobStr.trim().isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date dob = dateFormat.parse(dobStr.trim());
                    user.setDOB(dob);
                } catch (ParseException e) {
                    request.setAttribute("error", "Invalid date format. Use YYYY-MM-DD");
                    request.setAttribute("firstName", firstName);
                    request.setAttribute("lastName", lastName);
                    request.setAttribute("nationalID", nationalID);
                    request.setAttribute("email", email);
                    request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
                    return;
                }
            }

            // Create user in database
            boolean success = UserDatabaseOperation.createUser(user, password);

            if (success) {
                // Registration successful, redirect to login page
                request.setAttribute("success", "Registration successful! Please login with your credentials.");
                response.sendRedirect(request.getContextPath() + "/login");
            } else {
                request.setAttribute("error", "Registration failed. Please try again.");
                request.setAttribute("firstName", firstName);
                request.setAttribute("lastName", lastName);
                request.setAttribute("nationalID", nationalID);
                request.setAttribute("email", email);
                request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            request.setAttribute("error", "Database error occurred during registration");
            e.printStackTrace(); // Log the error
            request.setAttribute("firstName", firstName);
            request.setAttribute("lastName", lastName);
            request.setAttribute("nationalID", nationalID);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "An unexpected error occurred during registration");
            e.printStackTrace(); // Log the error
            request.setAttribute("firstName", firstName);
            request.setAttribute("lastName", lastName);
            request.setAttribute("nationalID", nationalID);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to signup page (URL will remain /register)
        request.getRequestDispatcher("/FrontEnd/Pages/signUp.jsp").forward(request, response);
    }
}

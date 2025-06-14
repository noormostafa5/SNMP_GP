package Backend_Servlet;

import java.io.IOException;
import java.sql.SQLException;

import Database_Connection.UserDatabaseOperation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/updatePasswords")
public class PasswordUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Update all plain text passwords to hashed passwords
            UserDatabaseOperation.updateAllPasswordsToHashed();
            
            response.setContentType("text/html");
            response.getWriter().println("<html><body>");
            response.getWriter().println("<h2>Password Update Complete!</h2>");
            response.getWriter().println("<p>All plain text passwords have been updated to hashed passwords.</p>");
            response.getWriter().println("<p>You can now login with your credentials.</p>");
            response.getWriter().println("<a href='" + request.getContextPath() + "/login'>Go to Login</a>");
            response.getWriter().println("</body></html>");
            
        } catch (SQLException e) {
            response.setContentType("text/html");
            response.getWriter().println("<html><body>");
            response.getWriter().println("<h2>Error Updating Passwords</h2>");
            response.getWriter().println("<p>Database error: " + e.getMessage() + "</p>");
            response.getWriter().println("</body></html>");
            e.printStackTrace();
        } catch (Exception e) {
            response.setContentType("text/html");
            response.getWriter().println("<html><body>");
            response.getWriter().println("<h2>Error Updating Passwords</h2>");
            response.getWriter().println("<p>Unexpected error: " + e.getMessage() + "</p>");
            response.getWriter().println("</body></html>");
            e.printStackTrace();
        }
    }
} 
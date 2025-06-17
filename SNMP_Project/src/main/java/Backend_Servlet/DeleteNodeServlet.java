package Backend_Servlet;

import java.io.IOException;
import java.sql.SQLException;

import Database_Connection.ServerDatabaseOperation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/deleteNode")
public class DeleteNodeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n=== DeleteNodeServlet doPost called ===");
        System.out.println("Request parameters:");
        request.getParameterMap().forEach((key, value) -> {
            System.out.println("  " + key + ": " + String.join(", ", value));
        });

        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("User not authenticated, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Get server IP parameter
        String serverIp = request.getParameter("serverIp");

        System.out.println("\nReceived parameters:");
        System.out.println("  serverIp: " + serverIp);

        // Validate input
        if (serverIp == null || serverIp.trim().isEmpty()) {
            System.out.println("Validation failed - missing server IP");
            request.setAttribute("error", "Server IP is required");
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;
        }

        try {
            // Delete from database
            System.out.println("\nAttempting to delete from database...");
            boolean success = ServerDatabaseOperation.deleteServerNode(serverIp.trim());

            System.out.println("Database operation result: " + success);

            if (success) {
                System.out.println("Node deleted successfully");
                request.setAttribute("success", "Node deleted successfully");
            } else {
                System.out.println("Failed to delete node");
                request.setAttribute("error", "Failed to delete node");
            }

        } catch (SQLException e) {
            System.out.println("SQL Exception occurred: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
        }

        // Forward back to dashboard
        request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect GET requests to dashboard
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
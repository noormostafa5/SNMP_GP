package Backend_Servlet;

import java.io.IOException;
import java.sql.SQLException;

import Database_Connection.ServerDatabaseOperation;
import Model.ServerNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/updateNode")
public class UpdateNodeServlet extends HttpServlet {

    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        // Basic IP address format validation
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.trim().matches(ipPattern);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n=== UpdateNodeServlet doPost called ===");
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

        // Get form parameters
        String nodeIdStr = request.getParameter("nodeId");
        String serverName = request.getParameter("serverName");
        String serverIp = request.getParameter("serverIp");
        String serverPortStr = request.getParameter("serverPort");

        System.out.println("\nReceived parameters:");
        System.out.println("  nodeId: " + nodeIdStr);
        System.out.println("  serverName: " + serverName);
        System.out.println("  serverIp: " + serverIp);
        System.out.println("  serverPort: " + serverPortStr);

        // Validate input
        if (nodeIdStr == null || nodeIdStr.trim().isEmpty() ||
                serverName == null || serverName.trim().isEmpty() ||
                serverIp == null || serverIp.trim().isEmpty() ||
                serverPortStr == null || serverPortStr.trim().isEmpty()) {

            System.out.println("Validation failed - missing required parameters");
            request.setAttribute("error", "All fields are required");
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;
        }

        // Validate IP address format
        if (!isValidIpAddress(serverIp)) {
            System.out.println("Invalid IP address format: " + serverIp);
            request.setAttribute("error", "Invalid IP address format. Please use IPv4 format (e.g., 192.168.1.1)");
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;
        }

        try {
            // Parse node ID and port number
            int nodeId = Integer.parseInt(nodeIdStr);
            int serverPort = Integer.parseInt(serverPortStr);

            // Validate port range
            if (serverPort < 1 || serverPort > 65535) {
                System.out.println("Invalid port number: " + serverPort);
                request.setAttribute("error", "Port number must be between 1 and 65535");
                request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
                return;
            }

            // Create server node object
            ServerNode node = new ServerNode();
            node.setId(nodeId);
            node.setServerName(serverName.trim());
            node.setServerIp(serverIp.trim());
            node.setPort(serverPort);

            System.out.println("\nCreated ServerNode object:");
            System.out.println("  ID: " + node.getId());
            System.out.println("  Name: " + node.getServerName());
            System.out.println("  IP: " + node.getServerIp());
            System.out.println("  Port: " + node.getPort());

            // Update in database
            System.out.println("\nAttempting to update in database...");
            boolean success = ServerDatabaseOperation.updateServerNode(node);

            System.out.println("Database operation result: " + success);

            if (success) {
                System.out.println("Node updated successfully");
                request.setAttribute("success", "Node updated successfully");
            } else {
                System.out.println("Failed to update node");
                request.setAttribute("error", "Failed to update node");
            }

        } catch (NumberFormatException e) {
            System.out.println("Number format exception: " + e.getMessage());
            request.setAttribute("error", "Invalid node ID or port number");
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;
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
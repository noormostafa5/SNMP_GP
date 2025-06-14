package Backend_Servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import Database_Connection.ServerDatabaseOperation;
import Model.ServerNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/addNode")
public class AddNodeServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== AddNodeServlet doPost called ===");
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            System.out.println("User not authenticated, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Get form parameters
        String serverName = request.getParameter("serverName");
        String serverIp = request.getParameter("serverIp");
        String cpuUsageStr = request.getParameter("cpuUsage");
        String memoryUsageStr = request.getParameter("memoryUsage");
        String diskUsageStr = request.getParameter("diskUsage");
        String networkUsageStr = request.getParameter("networkUsage");
        String status = request.getParameter("status");
        
        System.out.println("Received parameters:");
        System.out.println("  serverName: " + serverName);
        System.out.println("  serverIp: " + serverIp);
        System.out.println("  cpuUsage: " + cpuUsageStr);
        System.out.println("  memoryUsage: " + memoryUsageStr);
        System.out.println("  diskUsage: " + diskUsageStr);
        System.out.println("  networkUsage: " + networkUsageStr);
        System.out.println("  status: " + status);
        
        // Validate input
        if (serverName == null || serverName.trim().isEmpty() ||
            serverIp == null || serverIp.trim().isEmpty()) {
            
            System.out.println("Validation failed - missing required parameters");
            request.setAttribute("error", "Server name and IP address are required");
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;
        }
        
        try {
            // Parse numeric values with defaults
            double cpuUsage = 0.0;
            double memoryUsage = 0.0;
            double diskUsage = 0.0;
            double networkUsage = 0.0;
            
            if (cpuUsageStr != null && !cpuUsageStr.trim().isEmpty()) {
                cpuUsage = Double.parseDouble(cpuUsageStr);
            }
            if (memoryUsageStr != null && !memoryUsageStr.trim().isEmpty()) {
                memoryUsage = Double.parseDouble(memoryUsageStr);
            }
            if (diskUsageStr != null && !diskUsageStr.trim().isEmpty()) {
                diskUsage = Double.parseDouble(diskUsageStr);
            }
            if (networkUsageStr != null && !networkUsageStr.trim().isEmpty()) {
                networkUsage = Double.parseDouble(networkUsageStr);
            }
            
            // Set default status if not provided
            if (status == null || status.trim().isEmpty()) {
                status = "Active";
            }
            
            // Create new server node
            ServerNode newNode = new ServerNode();
            newNode.setServerName(serverName.trim());
            newNode.setServerIp(serverIp.trim());
            newNode.setReportTime(new Date());
            newNode.setCpuUsage(cpuUsage);
            newNode.setMemoryUsage(memoryUsage);
            newNode.setDiskUsage(diskUsage);
            newNode.setNetworkUsage(networkUsage);
            newNode.setStatus(status);
            
            System.out.println("Created ServerNode object:");
            System.out.println("  Name: " + newNode.getServerName());
            System.out.println("  IP: " + newNode.getServerIp());
            System.out.println("  CPU: " + newNode.getCpuUsage());
            System.out.println("  Memory: " + newNode.getMemoryUsage());
            System.out.println("  Disk: " + newNode.getDiskUsage());
            System.out.println("  Network: " + newNode.getNetworkUsage());
            System.out.println("  Status: " + newNode.getStatus());
            
            // Save to database
            System.out.println("Attempting to save to database...");
            boolean success = ServerDatabaseOperation.createServerNode(newNode);
            
            System.out.println("Database operation result: " + success);
            
            if (success) {
                System.out.println("Node added successfully");
                request.setAttribute("success", "Node added successfully");
            } else {
                System.out.println("Failed to add node");
                request.setAttribute("error", "Failed to add node");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Number format exception: " + e.getMessage());
            request.setAttribute("error", "Invalid numeric values provided");
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;
        } catch (SQLException e) {
            System.out.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
        }
        
        // Redirect back to dashboard
        System.out.println("Redirecting to dashboard");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirect GET requests to dashboard
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
} 
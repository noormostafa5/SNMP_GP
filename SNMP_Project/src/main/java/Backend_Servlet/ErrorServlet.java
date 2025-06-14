package Backend_Servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Database_Connection.ErrorDatabaseOperation;
import Model.Error;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/alarms")
public class ErrorServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== ErrorServlet doGet called ===");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Request URI: " + request.getRequestURI());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            System.out.println("Fetching all errors from database...");
            List<Error> errors = ErrorDatabaseOperation.getAllErrors();
            System.out.println("Found " + errors.size() + " errors in database");
            
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();
            
            String jsonResponse = gson.toJson(errors);
            System.out.println("JSON Response: " + jsonResponse);
            response.getWriter().write(jsonResponse);
            
        } catch (SQLException e) {
            System.out.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== ErrorServlet doPost called ===");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String serverName = request.getParameter("serverName");
            String serverIp = request.getParameter("serverIp");
            String description = request.getParameter("description");
            
            System.out.println("Received parameters:");
            System.out.println("  serverName: " + serverName);
            System.out.println("  serverIp: " + serverIp);
            System.out.println("  description: " + description);
            
            if (serverName == null || serverIp == null || description == null) {
                System.out.println("Missing required parameters");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing required parameters\"}");
                return;
            }
            
            Error error = new Error(serverName, serverIp, description);
            System.out.println("Created Error object: " + error.getServerName() + ", " + error.getServerIp() + ", " + error.getDescription());
            
            boolean success = ErrorDatabaseOperation.createError(error);
            System.out.println("Database operation result: " + success);
            
            if (success) {
                System.out.println("Error report created successfully");
                response.getWriter().write("{\"success\": true, \"message\": \"Error report created successfully\"}");
            } else {
                System.out.println("Failed to create error report");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Failed to create error report\"}");
            }
            
        } catch (SQLException e) {
            System.out.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== ErrorServlet doDelete called ===");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String idParam = request.getParameter("id");
            System.out.println("Delete request for ID: " + idParam);
            
            if (idParam == null || idParam.trim().isEmpty()) {
                System.out.println("Missing error ID");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing error ID\"}");
                return;
            }
            
            int id = Integer.parseInt(idParam);
            System.out.println("Attempting to delete error with ID: " + id);
            
            boolean success = ErrorDatabaseOperation.deleteError(id);
            System.out.println("Delete operation result: " + success);
            
            if (success) {
                System.out.println("Error report deleted successfully");
                response.getWriter().write("{\"success\": true, \"message\": \"Error report deleted successfully\"}");
            } else {
                System.out.println("Error report not found");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Error report not found\"}");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid error ID format: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid error ID format\"}");
        } catch (SQLException e) {
            System.out.println("SQL Exception occurred: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
} 
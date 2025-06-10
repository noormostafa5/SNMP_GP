package Backend_Servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import Database_Connection.DatabaseOperation;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get parameters from request
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String password = request.getParameter("password");

        // Validate input parameters
        if (firstName == null || lastName == null || password == null ||
                firstName.trim().isEmpty() || lastName.trim().isEmpty() || password.trim().isEmpty()) {
            request.setAttribute("error", "First name, last name, and password are required");
            request.getRequestDispatcher("/FrontEnd/login.jsp").forward(request, response);
            return;
        }

        try {
            // Attempt to login user
            User user = DatabaseOperation.loginUser(firstName, lastName, password);

            if (user != null) {
                // Invalidate any existing session to prevent session fixation
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                // Create new session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60); // 30 minutes

                // Regenerate session ID to prevent session fixation
                request.changeSessionId();

                // Add security headers
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("X-XSS-Protection", "1; mode=block");
                response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

                // Redirect to dashboard
                response.sendRedirect(request.getContextPath() + "/FrontEnd/dashboard.jsp");
            } else {
                // Add delay to prevent brute force attacks
                Thread.sleep(1000);

                request.setAttribute("error", "Invalid credentials");
                request.setAttribute("firstName", firstName);
                request.setAttribute("lastName", lastName);
                request.getRequestDispatcher("/FrontEnd/login.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            request.setAttribute("error", "Database error occurred");
            e.printStackTrace(); // Log the error
            request.getRequestDispatcher("/FrontEnd/login.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "An unexpected error occurred");
            e.printStackTrace(); // Log the error
            request.getRequestDispatcher("/FrontEnd/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // User is already logged in, redirect to dashboard
            System.out.println("Success");
            response.sendRedirect(request.getContextPath() + "/FrontEnd/dashboard.jsp");
        } else {
            // No active session, show login page
            request.getRequestDispatcher("/FrontEnd/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set response content type
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Get response writer
        PrintWriter out = response.getWriter();
        Map<String, Object> responseMap = new HashMap<>();

        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            responseMap.put("success", true);
            responseMap.put("message", "Logged out successfully");
        } else {
            responseMap.put("success", false);
            responseMap.put("message", "No active session to logout");
        }

        // Send response
        out.print(gson.toJson(responseMap));
        out.flush();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Add security headers
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");

        super.service(request, response);
    }
}


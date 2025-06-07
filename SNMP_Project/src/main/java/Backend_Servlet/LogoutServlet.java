package Backend_Servlet;

import java.io.IOException;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get the current session
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Remove all session attributes
            session.removeAttribute("user");
            
            // Invalidate the session
            session.invalidate();
            
            // Clear all cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    Cookie newCookie = new Cookie(cookie.getName(), "");
                    newCookie.setMaxAge(0);
                    newCookie.setPath("/");
                    response.addCookie(newCookie);
                }
            }
            
            // Add cache control headers to prevent caching
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            // Redirect to home.jsp
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            // No active session, redirect to home.jsp
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if there's an active session
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("user") != null) {
            // Forward to logout.jsp to show the logout confirmation page
            request.getRequestDispatcher("/FrontEnd/logout.jsp").forward(request, response);
        } else {
            // No active session, redirect to home.jsp
            response.sendRedirect(request.getContextPath() + "/FrontEnd/home.jsp");
        }
    }

    // Prevent access to user data after logout
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
        
        // Check if the request is a GET or POST
        String method = request.getMethod();
        
        if ("GET".equals(method) || "POST".equals(method)) {
            super.service(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.sendRedirect(request.getContextPath() + "/FrontEnd/home.jsp");
        }
    }
}

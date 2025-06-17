package Backend_Servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;

import Database_Connection.ActionDatabaseOperation;
import Model.Node;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ActionServlet")
public class ActionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n=== ActionServlet doGet called ===");

        /* -------- Login check -------- */
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        System.out.println("Action = " + action);

        try {
            if ("getNodes".equalsIgnoreCase(action)) {
                List<Node> nodeList = ActionDatabaseOperation.getAllNodes();

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String json = new Gson().toJson(nodeList);
                response.getWriter().write(json);
                return;
            }

            // Add more `action=` handlers here if needed later.

            // No known action
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action: " + action);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Reserved for future POST actions
        response.sendRedirect(request.getContextPath() + "/rules.jsp");
    }
}

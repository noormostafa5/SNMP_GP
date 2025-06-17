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

@WebServlet("/addNode")
public class AddNodeServlet extends HttpServlet {

    /* Simple IPv4 validator */
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) return false;
        String ipPattern =
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.trim().matches(ipPattern);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n=== AddNodeServlet doPost called ===");

        /* -------- Login check -------- */
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        /* -------- Read form fields -------- */
        String serverName   = request.getParameter("serverName");
        String serverIp     = request.getParameter("serverIp");
        String serverPortStr= request.getParameter("serverPort");

        System.out.printf("Params -> name:%s, ip:%s, port:%s%n",
                serverName, serverIp, serverPortStr);

        /* -------- Basic validation -------- */
        if (serverName == null || serverName.trim().isEmpty() ||
                serverIp   == null || serverIp.trim().isEmpty()   ||
                serverPortStr == null || serverPortStr.trim().isEmpty()) {

            request.setAttribute("error", "Server name, IP address, and port are required");
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;
        }

        /* -------- IP syntax check -------- */
        if (!isValidIpAddress(serverIp)) {
            request.setAttribute("error", "Invalid IP format (e.g. 192.168.1.10)");
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;
        }

        try {
            int serverPort = Integer.parseInt(serverPortStr);
            if (serverPort < 1 || serverPort > 65535) {
                request.setAttribute("error", "Port must be 1‑65535");
                request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
                return;
            }

            /* -------- Build model -------- */
            ServerNode newNode = new ServerNode();
            newNode.setServerName(serverName.trim());
            newNode.setServerIp(serverIp.trim());
            newNode.setPort(serverPort);          // ✅ correct setter

            /* -------- Persist to DB -------- */
            boolean success = ServerDatabaseOperation.createServerNode(newNode);

            if (success) {
                request.setAttribute("success", "Node added successfully");
            } else {
                request.setAttribute("error", "Failed to add node");
            }
            /* Forward back to dashboard so message appears */
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;

        } catch (NumberFormatException ex) {
            request.setAttribute("error", "Port must be numeric");
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;

        } catch (SQLException ex) {
            ex.printStackTrace();   // real diagnostics in logs
            request.setAttribute("error", "Database error: " + ex.getMessage());
            request.getRequestDispatcher("/FrontEnd/Pages/dashboard.jsp").forward(request, response);
            return;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
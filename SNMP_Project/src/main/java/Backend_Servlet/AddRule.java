package Backend_Servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import Database_Connection.DataBaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;

@WebServlet("/AddRule")
public class AddRule extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try (BufferedReader reader = request.getReader()) {
            JsonObject jsonInput = JsonParser.parseReader(reader).getAsJsonObject();

            int serverId = jsonInput.get("serverId").getAsInt();
            String actionType = jsonInput.get("actionType").getAsString();
            String rootEmail = jsonInput.get("rootEmail").getAsString();

            try (Connection conn = DataBaseConnection.getConnection()) {

                // Insert rule and retrieve generated ID
                String insertSQL = "INSERT INTO action_rules (server_id, action_type, root_email) VALUES (?, ?, ?) RETURNING id";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    insertStmt.setInt(1, serverId);
                    insertStmt.setString(2, actionType);
                    insertStmt.setString(3, rootEmail);

                    ResultSet rs = insertStmt.executeQuery();

                    if (rs.next()) {
                        int newId = rs.getInt("id");

                        // Get node name using serverId
                        String getNodeNameSQL = "SELECT node_name FROM nodes WHERE node_id = ?";
                        try (PreparedStatement nameStmt = conn.prepareStatement(getNodeNameSQL)) {
                            nameStmt.setInt(1, serverId);
                            ResultSet nameRs = nameStmt.executeQuery();

                            String nodeName = nameRs.next() ? nameRs.getString("node_name") : "Unknown";

                            JsonObject jsonResponse = new JsonObject();
                            jsonResponse.addProperty("success", true);
                            jsonResponse.addProperty("insertId", newId);
                            jsonResponse.addProperty("nodeName", nodeName);

                            out.print(gson.toJson(jsonResponse));
                        }
                    } else {
                        JsonObject errorResponse = new JsonObject();
                        errorResponse.addProperty("success", false);
                        errorResponse.addProperty("message", "Insertion failed, no ID returned.");
                        out.print(gson.toJson(errorResponse));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("message", "Database error: " + e.getMessage());
                out.print(gson.toJson(errorResponse));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("success", false);
            errorResponse.addProperty("message", "Invalid input: " + e.getMessage());
            out.print(gson.toJson(errorResponse));
        }
    }
}

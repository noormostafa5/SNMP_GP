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

@WebServlet("/editRule")
public class EditRule extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try (BufferedReader reader = request.getReader()) {
            JsonObject jsonInput = JsonParser.parseReader(reader).getAsJsonObject();

            int id = jsonInput.get("id").getAsInt();
            String actionType = jsonInput.get("actionType").getAsString();
            String rootEmail = jsonInput.get("rootEmail").getAsString();

            try (Connection conn = DataBaseConnection.getConnection()) {
                String sql = "UPDATE action_rules SET action_type = ?, root_email = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, actionType);
                    stmt.setString(2, rootEmail);
                    stmt.setInt(3, id);

                    int updated = stmt.executeUpdate();
                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("success", updated > 0);
                    out.print(gson.toJson(jsonResponse));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", e.getMessage());
            out.print(gson.toJson(error));
        }
    }
}

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

@WebServlet("/deleteRule")
public class DeleteRule extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try (BufferedReader reader = request.getReader()) {
            JsonObject jsonInput = JsonParser.parseReader(reader).getAsJsonObject();

            int id = jsonInput.get("id").getAsInt();

            try (Connection conn = DataBaseConnection.getConnection()) {
                String sql = "DELETE FROM action_rules WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, id);

                    int deleted = stmt.executeUpdate();
                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("success", deleted > 0);
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

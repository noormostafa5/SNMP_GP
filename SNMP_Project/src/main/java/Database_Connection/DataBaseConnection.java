package Database_Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//my-snmp-public.ca5cwqo86nt5.us-east-1.rds.amazonaws.com
public class DataBaseConnection {
    private static final String URL = "jdbc:postgresql://my-snmp-public.ca5cwqo86nt5.us-east-1.rds.amazonaws.com:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "my-aws-database-password";

    public static Connection getConnection() throws SQLException {
        System.out.println("success");
        try {
            Class.forName("org.postgresql.Driver");  // Load driver
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

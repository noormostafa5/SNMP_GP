package com.mycompany.snmpproject.snmpserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPMonitoringServer implements CommandResponder {
    private static final String DB_URL = "jdbc:postgresql://my-snmp-public.ca5cwqo86nt5.us-east-1.rds.amazonaws.com:5432/snmp";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "Mayar123m";
    private static final int SNMP_PORT = 162;
    
    private Snmp snmp;
    private Connection dbConnection;

    public SNMPMonitoringServer() {
        try {
            initializeDatabase();
            
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.addCommandResponder(this);
            transport.listen();
            
            System.out.println("SNMP Monitoring Server started on port " + SNMP_PORT);
            System.out.println("Waiting for traps...");
            
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeDatabase() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS snmp_traps (" +
                    "id SERIAL PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "ip VARCHAR(50)," +
                    "port INTEGER," +
                    "description_of_error TEXT," +
                    "status VARCHAR(10)," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            try (PreparedStatement stmt = dbConnection.prepareStatement(createTableSQL)) {
                stmt.execute();
            }
            
            System.out.println("Database connection established successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found");
            e.printStackTrace();
        }
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        PDU pdu = event.getPDU();
        if (pdu != null) {
            try {
                String clientIP = event.getPeerAddress().toString().split("/")[0];
                String errorDescription = "";
                String errorType = "";
                String status = "Alarm";

                for (VariableBinding vb : pdu.getVariableBindings()) {
                    OID oid = vb.getOid();
                    if (oid.toString().equals("1.3.6.1.4.1.9999.1.1.2")) {
                        clientIP = vb.getVariable().toString();
                    } else if (oid.toString().equals("1.3.6.1.4.1.9999.1.1.3")) {
                        errorType = vb.getVariable().toString();
                    } else if (oid.toString().equals("1.3.6.1.4.1.9999.1.1.4")) {
                        errorDescription = vb.getVariable().toString();
                    }
                }
                
                if (clientIP.isEmpty() || clientIP.equals("Client IP: ")) {
                     clientIP = event.getPeerAddress().toString().split("/")[0];
                } else {
                     clientIP = clientIP.replace("Client IP: ", "");
                }

                saveTrapToDatabase(clientIP, errorType, SNMP_PORT, errorDescription, status);
                
                System.out.println("Received trap from: " + clientIP);
                System.out.println("Error Type: " + errorType);
                System.out.println("Error Description: " + errorDescription);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveTrapToDatabase(String ip, String name, int port, String descriptionOfError, String status) {
        String insertSQL = "INSERT INTO snmp_traps (name, ip, port, description_of_error, status) " +
                          "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(insertSQL)) {
            pstmt.setString(1, name.isEmpty() ? ip : name);
            pstmt.setString(2, ip);
            pstmt.setInt(3, port);
            pstmt.setString(4, descriptionOfError);
            pstmt.setString(5, status);
            
            pstmt.executeUpdate();
            System.out.println("Trap saved to database successfully");
            
        } catch (SQLException e) {
            System.err.println("Error saving trap to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SNMPMonitoringServer();
    }
} 
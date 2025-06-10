package com.mycompany.snmpproject.snmpclient;

import java.io.IOException;
import java.util.Scanner;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPTrapSender {

    private String monitoringServerIp;
    private String community = "public";
    private int snmpPort = 162;

    public SNMPTrapSender() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== SNMP Trap Sender Configuration ===");
        System.out.print("Enter Monitoring Server IP: ");
        this.monitoringServerIp = scanner.nextLine();
        System.out.println("Using port: " + snmpPort);
        System.out.println("Using community: " + community);
        System.out.println("================================");
    }

    public void sendTrap(String errorDescription) throws IOException {
        System.out.println("Initializing SNMP transport...");
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();
        System.out.println("SNMP transport initialized successfully");

        System.out.println("Configuring target...");
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(new UdpAddress(monitoringServerIp + "/" + snmpPort));
        target.setRetries(2);
        target.setTimeout(1500);
        System.out.println("Target configured: " + monitoringServerIp + ":" + snmpPort);

        System.out.println("Creating PDU...");
        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);

        pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
        pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID("1.3.6.1.4.1.4976.6.1.1")));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.4976.6.1.2"),
                new OctetString("Client IP: " + getLocalIp())));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.4976.6.1.3"),
                new OctetString("Error: " + errorDescription)));
        System.out.println("PDU created successfully");

        System.out.println("Sending trap...");
        Snmp snmp = new Snmp(transport);
        snmp.send(pdu, target);
        snmp.close();

        System.out.println("Trap sent successfully to " + monitoringServerIp + " with error: " + errorDescription);
    }

    private String getLocalIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static void main(String[] args) {
        SNMPTrapSender sender = new SNMPTrapSender();
        try {
            sender.sendTrap("Simulated error: Disk full on /var");
        } catch (IOException e) {
            System.err.println("Error sending trap: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
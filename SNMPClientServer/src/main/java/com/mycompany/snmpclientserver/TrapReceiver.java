package com.mycompany.snmpclientserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.TransportMapping;

import java.io.IOException;
import java.util.Vector;

public class TrapReceiver implements CommandResponder {

    private static final Logger logger = LoggerFactory.getLogger(TrapReceiver.class);
    private TransportMapping<?> transportMapping;
    private Snmp snmp;
    private final int trapPort;

    public TrapReceiver(int trapPort) {
        this.trapPort = trapPort;
    }

    public void start() {
        try {
            UdpAddress address = new UdpAddress("127.0.0.1/" + trapPort);
            transportMapping = new DefaultUdpTransportMapping(address);

            snmp = new Snmp(transportMapping);
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());

            // USM usm = new USM(SecurityProtocols.getInstance(), new OctetString("SHAs"), null, null);
            // SecurityModels.getInstance().addSecurityModel(usm);

            snmp.addCommandResponder(this);

            transportMapping.listen(); // Start listening for incoming messages
            logger.info("SNMP Trap Receiver started on port {}", trapPort);

        } catch (IOException e) {
            logger.error("Error starting SNMP Trap Receiver: {}", e.getMessage());
        }
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        logger.info("Received SNMP Trap from {}/{}", event.getPeerAddress(), event.getSecurityName());
        logger.info("PDU Type: {}", event.getPDU().getType());
        logger.info("Variables: {}", event.getPDU().getVariableBindings());

        try {
            // Here you can add logic to process the trap, e.g., send to monitoring server
            // For now, we'll just log it.
            // You might want to parse the VariableBindings and extract meaningful data.
        } catch (Exception e) {
            logger.error("Error processing received trap: {}", e.getMessage());
        }
    }

    public void stop() {
        if (snmp != null) {
            try {
                snmp.close();
                logger.info("SNMP Trap Receiver stopped.");
            } catch (IOException e) {
                logger.error("Error stopping SNMP Trap Receiver: {}", e.getMessage());
            }
        }
    }
} 
package com.mycompany.snmpclientserver;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailTest {

    public static void main(String[] args) {
        final String username = "mohamedmeselhy999@gmail.com";
        final String password = "nrab dpzf koio xtnf"; // Your app password
        final String toEmail = "mohamedmeselhy999@gmail.com"; // Send to yourself for testing

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Test Email from SNMP Client Server Project");
            message.setText("This is a test email sent from the EmailTest.java program.");

            System.out.println("Attempting to send test email...");
            Transport.send(message);
            System.out.println("Test email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Error sending test email: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
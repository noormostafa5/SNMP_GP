package API;

public class MailSenderTest {
    public static void main(String[] args) {
        // Replace with a verified recipient email
        String recipientEmail = "mayarheshamfahmy@gmail.com";
        String recipientName = "Mayar Hesham";
        String subject = "Test Email from Java App";
        String messageContent = "This is a test email sent using Mailjet API in Java.";

        // Call the MailSender utility to send the email
        MailSender.sendEmail(recipientEmail, recipientName, subject, messageContent);
    }
}

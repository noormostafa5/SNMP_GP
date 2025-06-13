package API;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

public class MailSender {

    private static final String API_KEY = "your_public_api_key";
    private static final String API_SECRET = "your_private_api_key";

    public static void sendEmail(String toEmail, String toName, String subject, String content) {
        // Create Mailjet client with default options (v3.1 is automatically used when Emailv31 is used)
        MailjetClient client = new MailjetClient(API_KEY, API_SECRET);

        try {
            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put("From", new JSONObject()
                                            .put("Email", "mayarfahmy201@gmail.com")
                                            .put("Name", "Test"))
                                    .put("To", new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", toEmail)
                                                    .put("Name", toName)))
                                    .put("Subject", subject)
                                    .put("TextPart", content)
                                    .put("HTMLPart", "<h3>" + content + "</h3>")
                            ));

            MailjetResponse response = client.post(request);
            System.out.println("Status: " + response.getStatus());
            System.out.println("Response: " + response.getData());

        } catch (MailjetException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace(); // Logs full stack trace
        }
    }
}

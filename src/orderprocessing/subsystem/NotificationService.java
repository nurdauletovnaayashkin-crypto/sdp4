package orderprocessing.subsystem;

public class NotificationService {

    public void send(String email, String message) {
        if (email == null || email.isBlank()) {
            throw new NotificationException("Recipient email is missing");
        }
        System.out.println("[Notification] To " + email + ": " + message);
    }
}
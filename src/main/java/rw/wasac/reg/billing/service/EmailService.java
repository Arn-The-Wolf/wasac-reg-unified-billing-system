package rw.wasac.reg.billing.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}

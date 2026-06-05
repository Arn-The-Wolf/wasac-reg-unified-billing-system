/**
 * Service contract defining EmailService operations.
 */
package rw.wasac.reg.billing.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void sendHtmlEmail(String to, String subject, String htmlBody, String plainTextFallback);
}

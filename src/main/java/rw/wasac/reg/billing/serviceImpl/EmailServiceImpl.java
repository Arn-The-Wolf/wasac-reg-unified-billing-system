/**
 * Sends plain-text and HTML emails via Gmail SMTP with WASAC branding.
 */
package rw.wasac.reg.billing.serviceImpl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import rw.wasac.reg.billing.service.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:wasac-billing@wasac.rw}")
    private String fromAddress;

    @Value("${app.mail.from-name:Utility Billing System}")
    private String fromName;

    @Override
    public void sendEmail(String to, String subject, String body) {
        sendHtmlEmail(to, subject, null, body);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody, String plainTextFallback) {
        try {
            if (htmlBody != null && !htmlBody.isBlank()) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromName + " <" + fromAddress + ">");
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(plainTextFallback != null ? plainTextFallback : stripHtml(htmlBody), htmlBody);
                mailSender.send(mimeMessage);
            } else {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromName + " <" + fromAddress + ">");
                message.setTo(to);
                message.setSubject(subject);
                message.setText(plainTextFallback);
                mailSender.send(message);
            }
            log.info("Email sent to {}", to);
        } catch (Exception ex) {
            log.warn("Email delivery failed for {} (logged for traceability): {}", to, ex.getMessage());
            log.info("EMAIL FALLBACK -> to={}, subject={}, body={}", to, subject, plainTextFallback);
        }
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}

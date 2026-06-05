/**
 * Builds branded HTML email templates for WASAC utility billing notifications.
 */
package rw.wasac.reg.billing.utils;

/**
 * WASAC blue-themed HTML email templates for water & utility billing communications.
 */
public final class EmailTemplateBuilder {

    private static final String WASAC_BLUE = "#0066B3";
    private static final String WASAC_DARK = "#004080";
    private static final String WASAC_LIGHT = "#E8F4FC";
    private static final String WASAC_ACCENT = "#00A3E0";

    private EmailTemplateBuilder() {
    }

    public static String buildOtpEmail(String recipientName, String otpCode, int expiryMinutes) {
        String body = """
                <p style="margin:0 0 16px;font-size:16px;color:#333;">Dear <strong>%s</strong>,</p>
                <p style="margin:0 0 24px;font-size:15px;color:#555;line-height:1.6;">
                  Use the verification code below to complete your request. This code expires in
                  <strong>%d minutes</strong>.
                </p>
                <div style="background:%s;border-radius:8px;padding:24px;text-align:center;margin:24px 0;">
                  <p style="margin:0 0 8px;font-size:13px;color:%s;text-transform:uppercase;letter-spacing:1px;">
                    Verification Code
                  </p>
                  <p style="margin:0;font-size:36px;font-weight:bold;color:%s;letter-spacing:8px;">%s</p>
                </div>
                <p style="margin:0;font-size:13px;color:#888;">
                  If you did not request this code, please ignore this email or contact WASAC support.
                </p>
                """.formatted(recipientName, expiryMinutes, WASAC_LIGHT, WASAC_DARK, WASAC_BLUE, otpCode);

        return wrap("WASAC Verification Code", body);
    }

    public static String buildWelcomeEmail(String recipientName) {
        String body = """
                <p style="margin:0 0 16px;font-size:16px;color:#333;">Dear <strong>%s</strong>,</p>
                <p style="margin:0 0 16px;font-size:15px;color:#555;line-height:1.6;">
                  Your WASAC/REG utility billing account has been <strong style="color:%s;">activated successfully</strong>.
                </p>
                <p style="margin:0;font-size:15px;color:#555;line-height:1.6;">
                  You can now log in to view bills, payment history, and manage your water & electricity services.
                </p>
                """.formatted(recipientName, WASAC_BLUE);

        return wrap("Welcome to WASAC Billing", body);
    }

    public static String buildBillingNotificationEmail(String title, String message) {
        String formattedMessage = message
                .replace("\n\n", "</p><p style=\"margin:0 0 12px;font-size:15px;color:#555;line-height:1.6;\">")
                .replace("Dear ", "</p><p style=\"margin:0 0 16px;font-size:16px;color:#333;\">Dear ");

        String body = """
                <div style="border-left:4px solid %s;padding-left:16px;margin-bottom:20px;">
                  <h2 style="margin:0;font-size:18px;color:%s;">%s</h2>
                </div>
                <p style="margin:0 0 12px;font-size:15px;color:#555;line-height:1.6;">%s</p>
                <div style="background:%s;border-radius:6px;padding:16px;margin-top:24px;">
                  <p style="margin:0;font-size:13px;color:%s;">
                    💧 <strong>Tip:</strong> Pay on time to avoid late penalties. Use Mobile Money or bank transfer for convenience.
                  </p>
                </div>
                """.formatted(WASAC_ACCENT, WASAC_DARK, title, formattedMessage, WASAC_LIGHT, WASAC_DARK);

        return wrap(title, body);
    }

    private static String wrap(String title, String bodyContent) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f0f6fc;font-family:'Segoe UI',Arial,sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%s;">
                    <tr><td align="center" style="padding:28px 16px;">
                      <p style="margin:0;font-size:28px;font-weight:bold;color:#ffffff;letter-spacing:2px;">WASAC</p>
                      <p style="margin:6px 0 0;font-size:13px;color:#cce5ff;">Water &amp; Sanitation Corporation — Rwanda</p>
                      <p style="margin:4px 0 0;font-size:11px;color:#99ccff;">Utility Billing System</p>
                    </td></tr>
                  </table>
                  <table role="presentation" width="600" align="center" cellpadding="0" cellspacing="0"
                         style="max-width:600px;width:100%%;background:#ffffff;margin:24px auto;border-radius:10px;
                                box-shadow:0 2px 8px rgba(0,102,179,0.12);">
                    <tr><td style="padding:36px 32px;">%s</td></tr>
                  </table>
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                    <tr><td align="center" style="padding:20px 16px 32px;font-size:12px;color:#888;">
                      &copy; WASAC/REG Rwanda &bull; Utility Billing System<br>
                      <span style="color:%s;">wasac.rw</span>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(WASAC_BLUE, bodyContent, WASAC_BLUE);
    }
}

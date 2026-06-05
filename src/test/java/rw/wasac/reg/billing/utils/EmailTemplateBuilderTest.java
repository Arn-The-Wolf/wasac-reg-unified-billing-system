package rw.wasac.reg.billing.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateBuilderTest {

    @Test
    void buildOtpEmail_containsCodeAndWasacBranding() {
        String html = EmailTemplateBuilder.buildOtpEmail("Jean Uwimana", "482910", 10);
        assertThat(html).contains("482910");
        assertThat(html).contains("Jean Uwimana");
        assertThat(html).contains("WASAC");
        assertThat(html).contains("#0066B3");
        assertThat(html).contains("<!DOCTYPE html>");
    }

    @Test
    void buildWelcomeEmail_containsRecipientName() {
        String html = EmailTemplateBuilder.buildWelcomeEmail("Marie Uwase");
        assertThat(html).contains("Marie Uwase");
        assertThat(html).contains("activated successfully");
    }

    @Test
    void buildActionRequiredEmail_containsApprovalBanner() {
        String html = EmailTemplateBuilder.buildActionRequiredEmail(
                "Finance Officer", "Payment Approval", "Payment #5 is waiting for your approval.");
        assertThat(html).contains("Action Required");
        assertThat(html).contains("waiting for your approval");
        assertThat(html).contains("#0066B3");
    }

    @Test
    void buildStaffAlertEmail_containsRecipientAndTitle() {
        String html = EmailTemplateBuilder.buildStaffAlertEmail(
                "Admin User", "New Bill Generated", "Bill BILL-001 created for Jean.");
        assertThat(html).contains("Admin User");
        assertThat(html).contains("New Bill Generated");
        assertThat(html).contains("WASAC");
    }

    @Test
    void buildBillingNotificationEmail_containsTitleAndMessage() {
        String html = EmailTemplateBuilder.buildBillingNotificationEmail(
                "New Utility Bill", "Dear Jean, Your June/2025 utility bill of 1500 FRW has been processed.");
        assertThat(html).contains("New Utility Bill");
        assertThat(html).contains("Jean");
        assertThat(html).contains("Water");
    }
}

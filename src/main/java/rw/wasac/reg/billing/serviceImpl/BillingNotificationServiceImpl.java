/**
 * Sends branded HTML emails for billing events. DB notifications are handled by PostgreSQL triggers (Task 6).
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.wasac.reg.billing.entity.Bill;
import rw.wasac.reg.billing.entity.Customer;
import rw.wasac.reg.billing.entity.Payment;
import rw.wasac.reg.billing.service.BillingNotificationService;
import rw.wasac.reg.billing.service.EmailService;
import rw.wasac.reg.billing.utils.EmailTemplateBuilder;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BillingNotificationServiceImpl implements BillingNotificationService {

    private final EmailService emailService;

    @Override
    public void notifyBillGenerated(Bill bill) {
        Customer customer = bill.getCustomer();
        String monthYear = formatMonthYear(bill.getBillingMonth(), bill.getBillingYear());
        String message = String.format(
                "Dear %s, Your %s utility bill of %s FRW has been successfully processed.",
                customer.getFullName(), monthYear, bill.getTotalAmount().toPlainString());
        sendBrandedEmail(customer, "New Utility Bill — " + monthYear, message);
    }

    @Override
    public void notifyPaymentReceived(Bill bill, Payment payment) {
        Customer customer = bill.getCustomer();
        String monthYear = formatMonthYear(bill.getBillingMonth(), bill.getBillingYear());
        String message = String.format(
                "Dear %s, Your payment of %s FRW via %s for the %s utility bill has been received. "
                        + "Remaining balance: %s FRW.",
                customer.getFullName(),
                payment.getAmount().toPlainString(),
                formatPaymentMethod(payment.getPaymentMethod()),
                monthYear,
                bill.getBalance().toPlainString());
        sendBrandedEmail(customer, "Payment Received — " + monthYear, message);
    }

    @Override
    public void notifyBillFullyPaid(Bill bill) {
        Customer customer = bill.getCustomer();
        String monthYear = formatMonthYear(bill.getBillingMonth(), bill.getBillingYear());
        String message = String.format(
                "Dear %s, Your %s utility bill of %s FRW has been successfully processed.",
                customer.getFullName(), monthYear, bill.getTotalAmount().toPlainString());
        sendBrandedEmail(customer, "Bill Fully Paid — " + monthYear, message);
    }

    @Override
    public void notifyPaymentRejected(Bill bill, Payment payment) {
        Customer customer = bill.getCustomer();
        String monthYear = formatMonthYear(bill.getBillingMonth(), bill.getBillingYear());
        String message = String.format(
                "Dear %s, Your payment of %s FRW via %s for bill %s was not approved. "
                        + "Please contact WASAC/REG finance or submit a new payment.",
                customer.getFullName(),
                payment.getAmount().toPlainString(),
                formatPaymentMethod(payment.getPaymentMethod()),
                bill.getReference());
        sendBrandedEmail(customer, "Payment Not Approved", message);
    }

    private void sendBrandedEmail(Customer customer, String title, String plainMessage) {
        String html = EmailTemplateBuilder.buildBillingNotificationEmail(title, plainMessage);
        emailService.sendHtmlEmail(customer.getEmail(), "WASAC — " + title, html, plainMessage);
    }

    private String formatMonthYear(int month, int year) {
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return monthName + "/" + year;
    }

    private String formatPaymentMethod(rw.wasac.reg.billing.enums.PaymentMethod method) {
        return method.name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
    }
}

package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.entity.Bill;
import rw.wasac.reg.billing.entity.Customer;
import rw.wasac.reg.billing.entity.CustomerNotification;
import rw.wasac.reg.billing.entity.Payment;
import rw.wasac.reg.billing.repository.NotificationRepository;
import rw.wasac.reg.billing.service.BillingNotificationService;
import rw.wasac.reg.billing.service.EmailService;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BillingNotificationServiceImpl implements BillingNotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void notifyBillGenerated(Bill bill) {
        Customer customer = bill.getCustomer();
        String monthYear = formatMonthYear(bill.getBillingMonth(), bill.getBillingYear());
        String message = String.format(
                "Dear %s, Your %s utility bill of %s FRW has been successfully processed.",
                customer.getFullName(),
                monthYear,
                bill.getTotalAmount().toPlainString());

        saveAndSend(customer, message, bill.getBillingMonth(), bill.getBillingYear(), monthYear);
    }

    @Override
    @Transactional
    public void notifyPaymentReceived(Bill bill, Payment payment) {
        Customer customer = bill.getCustomer();
        String monthYear = formatMonthYear(bill.getBillingMonth(), bill.getBillingYear());
        String message = String.format(
                "Dear %s, Your payment of %s FRW for the %s utility bill has been received. Remaining balance: %s FRW.",
                customer.getFullName(),
                payment.getAmount().toPlainString(),
                monthYear,
                bill.getBalance().toPlainString());

        saveAndSend(customer, message, bill.getBillingMonth(), bill.getBillingYear(), monthYear);
    }

    private void saveAndSend(Customer customer, String message, int billingMonth, int billingYear, String monthYear) {
        notificationRepository.save(CustomerNotification.builder()
                .customer(customer)
                .message(message)
                .billingMonth(billingMonth)
                .billingYear(billingYear)
                .monthYear(monthYear)
                .build());

        emailService.sendEmail(customer.getEmail(), "WASAC/REG Billing Notification", message);
    }

    private String formatMonthYear(int month, int year) {
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return monthName + "/" + year;
    }
}

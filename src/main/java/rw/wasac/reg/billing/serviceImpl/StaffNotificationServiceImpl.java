/**
 * Delivers real-world staff email alerts: approval queues, billing events, and account changes.
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.wasac.reg.billing.entity.*;
import rw.wasac.reg.billing.enums.Role;
import rw.wasac.reg.billing.enums.UserStatus;
import rw.wasac.reg.billing.repository.UserRepository;
import rw.wasac.reg.billing.service.EmailService;
import rw.wasac.reg.billing.service.StaffNotificationService;
import rw.wasac.reg.billing.utils.EmailTemplateBuilder;
import rw.wasac.reg.billing.utils.SecurityUtils;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaffNotificationServiceImpl implements StaffNotificationService {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Override
    public void notifyPaymentAwaitingApproval(Bill bill, Payment payment) {
        Customer customer = bill.getCustomer();
        String period = formatPeriod(bill.getBillingMonth(), bill.getBillingYear());
        String message = String.format(
                "Payment #%d of %s FRW submitted by %s for bill %s (%s) is waiting for your approval. "
                        + "Please review and approve or reject in the finance dashboard.",
                payment.getId(),
                payment.getAmount().toPlainString(),
                customer.getFullName(),
                bill.getReference(),
                period);

        emailStaffByRoles(
                List.of(Role.ROLE_FINANCE, Role.ROLE_ADMIN),
                "Action Required — Payment Awaiting Approval",
                "Payment is Waiting for Your Approval",
                message,
                true);
    }

    @Override
    public void notifyPaymentApproved(Bill bill, Payment payment) {
        String message = String.format(
                "Payment #%d of %s FRW for bill %s (%s) has been approved. "
                        + "Customer: %s. Remaining balance: %s FRW.",
                payment.getId(),
                payment.getAmount().toPlainString(),
                bill.getReference(),
                formatPeriod(bill.getBillingMonth(), bill.getBillingYear()),
                bill.getCustomer().getFullName(),
                bill.getBalance().toPlainString());

        emailStaffByRoles(
                List.of(Role.ROLE_FINANCE),
                "Payment Approved — " + bill.getReference(),
                "Payment Approved",
                message,
                false);
    }

    @Override
    public void notifyPaymentRejected(Bill bill, Payment payment) {
        String message = String.format(
                "Payment #%d of %s FRW for bill %s was rejected. Customer %s has been notified. "
                        + "Follow up if a new payment is expected.",
                payment.getId(),
                payment.getAmount().toPlainString(),
                bill.getReference(),
                bill.getCustomer().getFullName());

        emailStaffByRoles(
                List.of(Role.ROLE_FINANCE, Role.ROLE_ADMIN),
                "Payment Rejected — " + bill.getReference(),
                "Payment Rejected",
                message,
                false);
    }

    @Override
    public void notifyBillGenerated(Bill bill) {
        String message = String.format(
                "A new utility bill %s has been generated for %s (meter %s). "
                        + "Period: %s. Total amount: %s FRW. The customer has been notified.",
                bill.getReference(),
                bill.getCustomer().getFullName(),
                bill.getMeter().getMeterNumber(),
                formatPeriod(bill.getBillingMonth(), bill.getBillingYear()),
                bill.getTotalAmount().toPlainString());

        emailStaffByRoles(
                List.of(Role.ROLE_ADMIN),
                "New Bill Generated — " + bill.getReference(),
                "Billing Cycle Update",
                message,
                false);
    }

    @Override
    public void notifyMeterReadingCaptured(MeterReading reading) {
        Meter meter = reading.getMeter();
        Customer customer = meter.getCustomer();
        String consumption = reading.getCurrentReading().subtract(reading.getPreviousReading()).toPlainString();
        String message = String.format(
                "A new meter reading has been recorded for meter %s (%s customer: %s). "
                        + "Period: %s. Consumption: %s units. Please verify during field inspection.",
                meter.getMeterNumber(),
                meter.getType().name(),
                customer.getFullName(),
                formatPeriod(reading.getBillingMonth(), reading.getBillingYear()),
                consumption);

        emailStaffByRoles(
                List.of(Role.ROLE_INSPECTOR),
                "New Meter Reading — " + meter.getMeterNumber(),
                "Meter Reading Ready for Inspection",
                message,
                false);
    }

    @Override
    public void notifyOperatorReadingConfirmation(MeterReading reading) {
        Optional<User> currentUser = resolveCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        User user = currentUser.get();
        if (user.getRole() != Role.ROLE_OPERATOR && user.getRole() != Role.ROLE_INSPECTOR) {
            return;
        }

        Meter meter = reading.getMeter();
        String message = String.format(
                "You successfully recorded a meter reading for %s on %s. "
                        + "Previous: %s, Current: %s. Billing period: %s.",
                meter.getMeterNumber(),
                reading.getReadingDate(),
                reading.getPreviousReading().toPlainString(),
                reading.getCurrentReading().toPlainString(),
                formatPeriod(reading.getBillingMonth(), reading.getBillingYear()));

        sendStaffEmail(
                user,
                "Reading Recorded — " + meter.getMeterNumber(),
                "Meter Reading Confirmed",
                message,
                false);
    }

    @Override
    public void notifyCustomerActivated(Customer customer) {
        String message = String.format(
                "Dear %s, your WASAC utility account has been reactivated. "
                        + "You can now receive bills and make payments for your water and electricity services.",
                customer.getFullName());
        String html = EmailTemplateBuilder.buildAccountStatusEmail(customer.getFullName(), "Account Reactivated", message, true);
        emailService.sendHtmlEmail(customer.getEmail(), "WASAC — Account Reactivated", html, message);
    }

    @Override
    public void notifyCustomerDeactivated(Customer customer) {
        String customerMessage = String.format(
                "Dear %s, your WASAC utility account has been deactivated. "
                        + "New bills and meter readings will not be processed until your account is reactivated. "
                        + "Contact WASAC support for assistance.",
                customer.getFullName());
        String html = EmailTemplateBuilder.buildAccountStatusEmail(
                customer.getFullName(), "Account Deactivated", customerMessage, false);
        emailService.sendHtmlEmail(customer.getEmail(), "WASAC — Account Deactivated", html, customerMessage);

        String staffMessage = String.format(
                "Customer %s (ID %d) has been deactivated. No new billing cycles will start until reactivation.",
                customer.getFullName(), customer.getId());
        emailStaffByRoles(
                List.of(Role.ROLE_ADMIN),
                "Customer Deactivated — " + customer.getFullName(),
                "Customer Account Deactivated",
                staffMessage,
                false);
    }

    @Override
    public void notifyUserDeactivated(User user) {
        String message = String.format(
                "Dear %s, your WASAC billing system user account (%s) has been deactivated by an administrator. "
                        + "You will no longer be able to sign in. Contact your system administrator if you believe this is an error.",
                user.getFullName(), user.getRole().name().replace("ROLE_", ""));
        String html = EmailTemplateBuilder.buildAccountStatusEmail(user.getFullName(), "User Account Deactivated", message, false);
        emailService.sendHtmlEmail(user.getEmail(), "WASAC — User Account Deactivated", html, message);
    }

    private void emailStaffByRoles(List<Role> roles, String subject, String title, String message, boolean actionRequired) {
        List<User> recipients = userRepository.findByRoleInAndStatus(roles, UserStatus.ACTIVE);
        for (User user : recipients) {
            sendStaffEmail(user, subject, title, message, actionRequired);
        }
    }

    private void sendStaffEmail(User user, String subject, String title, String message, boolean actionRequired) {
        String html = actionRequired
                ? EmailTemplateBuilder.buildActionRequiredEmail(user.getFullName(), title, message)
                : EmailTemplateBuilder.buildStaffAlertEmail(user.getFullName(), title, message);
        emailService.sendHtmlEmail(user.getEmail(), "WASAC — " + subject, html, message);
    }

    private Optional<User> resolveCurrentUser() {
        try {
            return userRepository.findByEmail(SecurityUtils.getCurrentUserEmail());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String formatPeriod(int month, int year) {
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return monthName + "/" + year;
    }
}

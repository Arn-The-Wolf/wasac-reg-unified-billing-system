/**
 * Sends role-based email alerts to WASAC staff (admin, finance, operator, inspector).
 */
package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.entity.Bill;
import rw.wasac.reg.billing.entity.Customer;
import rw.wasac.reg.billing.entity.MeterReading;
import rw.wasac.reg.billing.entity.Payment;
import rw.wasac.reg.billing.entity.User;

public interface StaffNotificationService {

    void notifyPaymentAwaitingApproval(Bill bill, Payment payment);

    void notifyPaymentApproved(Bill bill, Payment payment);

    void notifyPaymentRejected(Bill bill, Payment payment);

    void notifyBillGenerated(Bill bill);

    void notifyMeterReadingCaptured(MeterReading reading);

    void notifyOperatorReadingConfirmation(MeterReading reading);

    void notifyCustomerActivated(Customer customer);

    void notifyCustomerDeactivated(Customer customer);

    void notifyUserDeactivated(User user);
}

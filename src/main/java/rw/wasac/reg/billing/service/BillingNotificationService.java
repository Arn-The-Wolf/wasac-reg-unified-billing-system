/**
 * Service contract defining BillingNotificationService operations.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.entity.Bill;
import rw.wasac.reg.billing.entity.Payment;

public interface BillingNotificationService {
    void notifyBillGenerated(Bill bill);
    void notifyPaymentSubmitted(Bill bill, Payment payment);
    void notifyPaymentReceived(Bill bill, Payment payment);
    void notifyBillFullyPaid(Bill bill);
    void notifyPaymentRejected(Bill bill, Payment payment);
}

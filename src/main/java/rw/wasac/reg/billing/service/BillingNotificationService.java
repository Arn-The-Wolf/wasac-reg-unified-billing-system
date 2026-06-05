package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.entity.Bill;
import rw.wasac.reg.billing.entity.Payment;

public interface BillingNotificationService {
    void notifyBillGenerated(Bill bill);
    void notifyPaymentReceived(Bill bill, Payment payment);
}

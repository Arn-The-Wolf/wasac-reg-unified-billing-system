/**
 * Service contract defining PaymentService operations.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.request.PaymentRequest;
import rw.wasac.reg.billing.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse recordPayment(PaymentRequest request);
    PaymentResponse approvePayment(Long paymentId);
    PaymentResponse rejectPayment(Long paymentId);
    List<PaymentResponse> getAll();
    List<PaymentResponse> getByBillId(Long billId);
    List<PaymentResponse> getPending();
}

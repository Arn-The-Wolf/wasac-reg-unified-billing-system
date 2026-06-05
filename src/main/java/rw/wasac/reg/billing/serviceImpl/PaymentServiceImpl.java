/**
 * Service implementation providing Payment business logic.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.PaymentRequest;
import rw.wasac.reg.billing.dto.response.PaymentResponse;
import rw.wasac.reg.billing.entity.Bill;
import rw.wasac.reg.billing.entity.Payment;
import rw.wasac.reg.billing.enums.BillStatus;
import rw.wasac.reg.billing.enums.PaymentStatus;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.exception.ResourceNotFoundException;
import rw.wasac.reg.billing.repository.BillRepository;
import rw.wasac.reg.billing.repository.PaymentRepository;
import rw.wasac.reg.billing.service.BillingNotificationService;
import rw.wasac.reg.billing.service.PaymentService;
import rw.wasac.reg.billing.service.StaffNotificationService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final BillingNotificationService billingNotificationService;
    private final StaffNotificationService staffNotificationService;

    @Override
    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Bill bill = billRepository.findByIdWithDetails(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill not found with id: " + request.getBillId()));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("This bill has already been fully paid");
        }

        if (request.getAmount().compareTo(bill.getBalance()) > 0) {
            throw new BadRequestException(
                    "Payment amount exceeds outstanding balance of " + bill.getBalance() + " FRW");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(request.getPaymentDate())
                .status(PaymentStatus.PENDING)
                .notes(request.getNotes())
                .build();

        Payment saved = paymentRepository.save(payment);
        billingNotificationService.notifyPaymentSubmitted(bill, saved);
        staffNotificationService.notifyPaymentAwaitingApproval(bill, saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse approvePayment(Long paymentId) {
        Payment payment = findEntity(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Only pending payments can be approved");
        }

        Bill bill = payment.getBill();
        BigDecimal newAmountPaid = bill.getAmountPaid().add(payment.getAmount());
        BigDecimal newBalance = bill.getTotalAmount().subtract(newAmountPaid);

        bill.setAmountPaid(newAmountPaid);
        bill.setBalance(newBalance.max(BigDecimal.ZERO));

        boolean fullyPaid = newBalance.compareTo(BigDecimal.ZERO) <= 0;
        if (fullyPaid) {
            bill.setStatus(BillStatus.PAID);
            bill.setBalance(BigDecimal.ZERO);
        } else {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }

        payment.setStatus(PaymentStatus.APPROVED);
        billRepository.save(bill);
        Payment saved = paymentRepository.save(payment);

        if (fullyPaid) {
            billingNotificationService.notifyBillFullyPaid(bill);
        } else {
            billingNotificationService.notifyPaymentReceived(bill, saved);
        }
        staffNotificationService.notifyPaymentApproved(bill, saved);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse rejectPayment(Long paymentId) {
        Payment payment = findEntity(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Only pending payments can be rejected");
        }

        payment.setStatus(PaymentStatus.REJECTED);
        Payment saved = paymentRepository.save(payment);
        billingNotificationService.notifyPaymentRejected(payment.getBill(), saved);
        staffNotificationService.notifyPaymentRejected(payment.getBill(), saved);

        return toResponse(saved);
    }

    @Override
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<PaymentResponse> getByBillId(Long billId) {
        return paymentRepository.findByBillId(billId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<PaymentResponse> getPending() {
        return paymentRepository.findByStatus(PaymentStatus.PENDING).stream().map(this::toResponse).toList();
    }

    private Payment findEntity(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .billId(payment.getBill().getId())
                .billReference(payment.getBill().getReference())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

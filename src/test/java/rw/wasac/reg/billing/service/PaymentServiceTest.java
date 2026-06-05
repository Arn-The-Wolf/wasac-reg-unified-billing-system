package rw.wasac.reg.billing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.wasac.reg.billing.dto.request.PaymentRequest;
import rw.wasac.reg.billing.entity.Bill;
import rw.wasac.reg.billing.entity.Customer;
import rw.wasac.reg.billing.entity.Payment;
import rw.wasac.reg.billing.enums.BillStatus;
import rw.wasac.reg.billing.enums.PaymentStatus;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.repository.BillRepository;
import rw.wasac.reg.billing.repository.PaymentRepository;
import rw.wasac.reg.billing.serviceImpl.PaymentServiceImpl;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private BillingNotificationService billingNotificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void recordPayment_rejectsAmountAboveBalance() {
        Bill bill = Bill.builder()
                .id(1L)
                .totalAmount(new BigDecimal("1000"))
                .balance(new BigDecimal("500"))
                .amountPaid(new BigDecimal("500"))
                .status(BillStatus.PARTIALLY_PAID)
                .customer(Customer.builder().id(1L).email("c@wasac.rw").build())
                .build();

        PaymentRequest request = new PaymentRequest();
        request.setBillId(1L);
        request.setAmount(new BigDecimal("600"));

        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() -> paymentService.recordPayment(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void approvePayment_marksBillPaidWhenFullyCovered() {
        Bill bill = Bill.builder()
                .id(1L)
                .totalAmount(new BigDecimal("1000"))
                .balance(new BigDecimal("400"))
                .amountPaid(new BigDecimal("600"))
                .status(BillStatus.PARTIALLY_PAID)
                .customer(Customer.builder().id(1L).email("c@wasac.rw").fullName("Customer").build())
                .build();

        Payment payment = Payment.builder()
                .id(10L)
                .bill(bill)
                .amount(new BigDecimal("400"))
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = paymentService.approvePayment(10L);

        assertThat(bill.getStatus()).isEqualTo(BillStatus.PAID);
        assertThat(bill.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        verify(billingNotificationService).notifyPaymentReceived(bill, payment);
    }
}

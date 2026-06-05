package rw.wasac.reg.billing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.wasac.reg.billing.entity.*;
import rw.wasac.reg.billing.enums.*;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.repository.*;
import rw.wasac.reg.billing.serviceImpl.PdfReportServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfReportServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private MeterReadingRepository meterReadingRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PdfReportServiceImpl pdfReportService;

    @Test
    void generatePaymentReceipt_producesPdfForApprovedPayment() {
        Customer customer = Customer.builder()
                .id(1L)
                .fullName("Jean Uwimana")
                .nationalId("1199887766554433")
                .phone("+250788000001")
                .address("Kigali")
                .build();
        Bill bill = Bill.builder()
                .id(1L)
                .reference("BILL-2025-06-001")
                .customer(customer)
                .billingMonth(6)
                .billingYear(2025)
                .balance(BigDecimal.ZERO)
                .status(BillStatus.PAID)
                .build();
        Payment payment = Payment.builder()
                .id(5L)
                .bill(bill)
                .amount(new BigDecimal("1500"))
                .paymentMethod(PaymentMethod.MOBILE_MONEY)
                .paymentDate(LocalDate.of(2025, 6, 15))
                .status(PaymentStatus.APPROVED)
                .build();

        when(paymentRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(payment));

        byte[] pdf = pdfReportService.generatePaymentReceipt(5L);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generatePaymentReceipt_rejectsPendingPayment() {
        Payment payment = Payment.builder()
                .id(5L)
                .bill(Bill.builder().id(1L).customer(Customer.builder().id(1L).build()).build())
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> pdfReportService.generatePaymentReceipt(5L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void generateBillReceipt_producesPdf() {
        Customer customer = Customer.builder().id(1L).fullName("Marie Uwase").build();
        Meter meter = Meter.builder().id(1L).meterNumber("MTR-001").type(MeterType.WATER).build();
        Bill bill = Bill.builder()
                .id(2L)
                .reference("BILL-2025-06-002")
                .customer(customer)
                .meter(meter)
                .billingMonth(6)
                .billingYear(2025)
                .consumption(new BigDecimal("12"))
                .tariffAmount(new BigDecimal("1000"))
                .fixedChargeAmount(new BigDecimal("200"))
                .taxAmount(new BigDecimal("180"))
                .penaltyAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("1380"))
                .amountPaid(new BigDecimal("1380"))
                .balance(BigDecimal.ZERO)
                .status(BillStatus.PAID)
                .build();

        when(billRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(bill));

        byte[] pdf = pdfReportService.generateBillReceipt(2L);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generateAdminBillingReport_producesPdf() {
        Customer customer = Customer.builder().id(1L).fullName("Jean").build();
        Meter meter = Meter.builder().id(1L).meterNumber("MTR-001").build();
        Bill bill = Bill.builder()
                .reference("BILL-001")
                .customer(customer)
                .meter(meter)
                .totalAmount(new BigDecimal("1000"))
                .amountPaid(new BigDecimal("500"))
                .balance(new BigDecimal("500"))
                .status(BillStatus.PARTIALLY_PAID)
                .build();

        when(billRepository.findByPeriodWithDetails(6, 2025)).thenReturn(List.of(bill));
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        byte[] pdf = pdfReportService.generateAdminBillingReport(6, 2025);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generateInspectorMeterReport_producesPdf() {
        Customer customer = Customer.builder().id(1L).fullName("Jean").build();
        Meter meter = Meter.builder().id(1L).meterNumber("MTR-001").type(MeterType.WATER).customer(customer).build();
        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(new BigDecimal("100"))
                .currentReading(new BigDecimal("112"))
                .readingDate(LocalDate.of(2025, 6, 10))
                .billingMonth(6)
                .billingYear(2025)
                .build();

        when(meterReadingRepository.findByPeriodWithDetails(6, 2025)).thenReturn(List.of(reading));

        byte[] pdf = pdfReportService.generateInspectorMeterReport(6, 2025);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generateAdminBillingReport_rejectsInvalidMonth() {
        assertThatThrownBy(() -> pdfReportService.generateAdminBillingReport(13, 2025))
                .isInstanceOf(BadRequestException.class);
    }
}

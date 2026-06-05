package rw.wasac.reg.billing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.wasac.reg.billing.entity.*;
import rw.wasac.reg.billing.enums.*;
import rw.wasac.reg.billing.repository.UserRepository;
import rw.wasac.reg.billing.serviceImpl.StaffNotificationServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffNotificationServiceTest {

    @Mock
    private EmailService emailService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StaffNotificationServiceImpl staffNotificationService;

    @Test
    void notifyPaymentAwaitingApproval_emailsFinanceAndAdminWithActionRequired() {
        User finance = User.builder().fullName("Finance Officer").email("finance@wasac.rw").role(Role.ROLE_FINANCE).build();
        User admin = User.builder().fullName("Admin").email("admin@wasac.rw").role(Role.ROLE_ADMIN).build();

        when(userRepository.findByRoleInAndStatus(anyList(), eq(UserStatus.ACTIVE)))
                .thenReturn(List.of(finance, admin));

        Bill bill = sampleBill();
        Payment payment = Payment.builder()
                .id(12L)
                .amount(new BigDecimal("500"))
                .paymentMethod(PaymentMethod.MOBILE_MONEY)
                .paymentDate(LocalDate.now())
                .status(PaymentStatus.PENDING)
                .build();

        staffNotificationService.notifyPaymentAwaitingApproval(bill, payment);

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(2)).sendHtmlEmail(anyString(), subjectCaptor.capture(), htmlCaptor.capture(), anyString());

        assertThat(subjectCaptor.getAllValues()).allMatch(s -> s.contains("Awaiting Approval"));
        assertThat(htmlCaptor.getAllValues()).allMatch(html -> html.contains("Action Required"));
        assertThat(htmlCaptor.getAllValues()).allMatch(html -> html.contains("waiting for your approval"));
    }

    @Test
    void notifyMeterReadingCaptured_emailsInspector() {
        User inspector = User.builder().fullName("Inspector").email("inspector@wasac.rw").role(Role.ROLE_INSPECTOR).build();
        when(userRepository.findByRoleInAndStatus(anyList(), eq(UserStatus.ACTIVE)))
                .thenReturn(List.of(inspector));

        MeterReading reading = MeterReading.builder()
                .meter(Meter.builder()
                        .meterNumber("MTR-100")
                        .type(MeterType.WATER)
                        .customer(Customer.builder().fullName("Jean").build())
                        .build())
                .previousReading(new BigDecimal("50"))
                .currentReading(new BigDecimal("62"))
                .readingDate(LocalDate.of(2025, 6, 10))
                .billingMonth(6)
                .billingYear(2025)
                .build();

        staffNotificationService.notifyMeterReadingCaptured(reading);

        verify(emailService).sendHtmlEmail(
                eq("inspector@wasac.rw"),
                contains("Meter Reading"),
                contains("Inspection"),
                anyString());
    }

    private Bill sampleBill() {
        return Bill.builder()
                .id(1L)
                .reference("BILL-ABC")
                .billingMonth(6)
                .billingYear(2025)
                .totalAmount(new BigDecimal("1500"))
                .balance(new BigDecimal("1500"))
                .customer(Customer.builder().fullName("Jean Uwimana").email("jean@wasac.rw").build())
                .meter(Meter.builder().meterNumber("MTR-001").build())
                .build();
    }
}

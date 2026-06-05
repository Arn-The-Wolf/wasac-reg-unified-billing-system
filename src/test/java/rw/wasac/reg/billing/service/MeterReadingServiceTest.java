package rw.wasac.reg.billing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.wasac.reg.billing.dto.request.MeterReadingRequest;
import rw.wasac.reg.billing.entity.Customer;
import rw.wasac.reg.billing.entity.Meter;
import rw.wasac.reg.billing.entity.MeterReading;
import rw.wasac.reg.billing.enums.CustomerStatus;
import rw.wasac.reg.billing.enums.MeterStatus;
import rw.wasac.reg.billing.enums.MeterType;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.repository.BillRepository;
import rw.wasac.reg.billing.repository.MeterReadingRepository;
import rw.wasac.reg.billing.repository.MeterRepository;
import rw.wasac.reg.billing.serviceImpl.CustomerServiceImpl;
import rw.wasac.reg.billing.serviceImpl.MeterReadingServiceImpl;
import rw.wasac.reg.billing.serviceImpl.MeterServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterReadingServiceTest {

    @Mock
    private MeterReadingRepository meterReadingRepository;
    @Mock
    private MeterRepository meterRepository;
    @Mock
    private MeterServiceImpl meterService;
    @Mock
    private CustomerServiceImpl customerService;
    @Mock
    private BillRepository billRepository;
    @Mock
    private StaffNotificationService staffNotificationService;

    @InjectMocks
    private MeterReadingServiceImpl meterReadingService;

    @Test
    void create_rejectsCurrentLessThanOrEqualPrevious() {
        MeterReadingRequest request = new MeterReadingRequest();
        request.setMeterId(1L);
        request.setPreviousReading(new BigDecimal("100"));
        request.setCurrentReading(new BigDecimal("100"));
        request.setReadingDate(LocalDate.now());

        Customer customer = Customer.builder().id(1L).status(CustomerStatus.ACTIVE).build();
        Meter meter = Meter.builder().id(1L).meterNumber("WTR-001").type(MeterType.WATER)
                .status(MeterStatus.ACTIVE).customer(customer).build();
        when(meterRepository.findById(1L)).thenReturn(Optional.of(meter));
        doNothing().when(meterService).assertMeterActive(any());
        doNothing().when(customerService).assertCustomerActive(any());

        assertThatThrownBy(() -> meterReadingService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("greater than previous");
    }

    @Test
    void create_rejectsDuplicatePeriod() {
        MeterReadingRequest request = new MeterReadingRequest();
        request.setMeterId(1L);
        request.setPreviousReading(new BigDecimal("100"));
        request.setCurrentReading(new BigDecimal("150"));
        request.setReadingDate(LocalDate.of(2025, 6, 1));

        Customer customer = Customer.builder().id(1L).status(CustomerStatus.ACTIVE).build();
        Meter meter = Meter.builder().id(1L).meterNumber("WTR-001").type(MeterType.WATER)
                .status(MeterStatus.ACTIVE).customer(customer).build();

        when(meterRepository.findById(1L)).thenReturn(Optional.of(meter));
        doNothing().when(meterService).assertMeterActive(any());
        doNothing().when(customerService).assertCustomerActive(any());
        when(meterReadingRepository.existsByMeterIdAndBillingMonthAndBillingYear(1L, 6, 2025))
                .thenReturn(true);

        assertThatThrownBy(() -> meterReadingService.create(request))
                .isInstanceOf(rw.wasac.reg.billing.exception.DuplicateResourceException.class);
    }

    @Test
    void getSuggestedPreviousReading_returnsZeroWhenNoHistory() {
        when(meterRepository.existsById(5L)).thenReturn(true);
        when(meterReadingRepository.findFirstByMeterIdOrderByReadingDateDesc(5L)).thenReturn(Optional.empty());

        assertThat(meterReadingService.getSuggestedPreviousReading(5L))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void create_autoFillsPreviousReadingWhenOmitted() {
        MeterReadingRequest request = new MeterReadingRequest();
        request.setMeterId(1L);
        request.setPreviousReading(null);
        request.setCurrentReading(new BigDecimal("200"));
        request.setReadingDate(LocalDate.of(2025, 7, 1));

        Customer customer = Customer.builder().id(1L).status(CustomerStatus.ACTIVE).build();
        Meter meter = Meter.builder().id(1L).meterNumber("WTR-001").type(MeterType.WATER)
                .status(MeterStatus.ACTIVE).customer(customer).build();

        when(meterRepository.findById(1L)).thenReturn(Optional.of(meter));
        doNothing().when(meterService).assertMeterActive(any());
        doNothing().when(customerService).assertCustomerActive(any());
        when(meterReadingRepository.findFirstByMeterIdOrderByReadingDateDesc(1L))
                .thenReturn(Optional.of(MeterReading.builder().currentReading(new BigDecimal("150")).build()));
        when(meterReadingRepository.existsByMeterIdAndBillingMonthAndBillingYear(1L, 7, 2025)).thenReturn(false);
        when(meterReadingRepository.save(any())).thenAnswer(inv -> {
            MeterReading r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        var response = meterReadingService.create(request);
        assertThat(response.getPreviousReading()).isEqualByComparingTo("150");
    }
}

package rw.wasac.reg.billing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.wasac.reg.billing.dto.request.MeterReadingRequest;
import rw.wasac.reg.billing.entity.Customer;
import rw.wasac.reg.billing.entity.Meter;
import rw.wasac.reg.billing.enums.CustomerStatus;
import rw.wasac.reg.billing.enums.MeterStatus;
import rw.wasac.reg.billing.enums.MeterType;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.repository.MeterReadingRepository;
import rw.wasac.reg.billing.repository.MeterRepository;
import rw.wasac.reg.billing.serviceImpl.CustomerServiceImpl;
import rw.wasac.reg.billing.serviceImpl.MeterReadingServiceImpl;
import rw.wasac.reg.billing.serviceImpl.MeterServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

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

    @InjectMocks
    private MeterReadingServiceImpl meterReadingService;

    @Test
    void create_rejectsCurrentLessThanOrEqualPrevious() {
        MeterReadingRequest request = new MeterReadingRequest();
        request.setMeterId(1L);
        request.setPreviousReading(new BigDecimal("100"));
        request.setCurrentReading(new BigDecimal("100"));
        request.setReadingDate(LocalDate.now());

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
}

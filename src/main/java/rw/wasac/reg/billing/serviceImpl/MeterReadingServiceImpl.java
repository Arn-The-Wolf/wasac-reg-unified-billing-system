package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.MeterReadingRequest;
import rw.wasac.reg.billing.dto.response.MeterReadingResponse;
import rw.wasac.reg.billing.entity.Meter;
import rw.wasac.reg.billing.entity.MeterReading;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.exception.DuplicateResourceException;
import rw.wasac.reg.billing.exception.ResourceNotFoundException;
import rw.wasac.reg.billing.repository.MeterReadingRepository;
import rw.wasac.reg.billing.repository.MeterRepository;
import rw.wasac.reg.billing.service.MeterReadingService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingServiceImpl implements MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterRepository meterRepository;
    private final MeterServiceImpl meterService;
    private final CustomerServiceImpl customerService;

    @Override
    @Transactional
    public MeterReadingResponse create(MeterReadingRequest request) {
        if (request.getCurrentReading().compareTo(request.getPreviousReading()) <= 0) {
            throw new BadRequestException("Current reading must be greater than previous reading");
        }

        Meter meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + request.getMeterId()));

        meterService.assertMeterActive(meter);
        customerService.assertCustomerActive(meter.getCustomer());

        int billingMonth = request.getReadingDate().getMonthValue();
        int billingYear = request.getReadingDate().getYear();

        if (meterReadingRepository.existsByMeterIdAndBillingMonthAndBillingYear(
                meter.getId(), billingMonth, billingYear)) {
            throw new DuplicateResourceException(
                    "Reading already exists for meter " + meter.getMeterNumber()
                            + " in " + billingMonth + "/" + billingYear);
        }

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .readingDate(request.getReadingDate())
                .billingMonth(billingMonth)
                .billingYear(billingYear)
                .build();

        return toResponse(meterReadingRepository.save(reading));
    }

    @Override
    public MeterReadingResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    public List<MeterReadingResponse> getByMeterId(Long meterId) {
        return meterReadingRepository.findByMeterIdOrderByReadingDateDesc(meterId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<MeterReadingResponse> getAll() {
        return meterReadingRepository.findAll().stream().map(this::toResponse).toList();
    }

    private MeterReading findEntity(Long id) {
        return meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found with id: " + id));
    }

    private MeterReadingResponse toResponse(MeterReading reading) {
        return MeterReadingResponse.builder()
                .id(reading.getId())
                .meterId(reading.getMeter().getId())
                .meterNumber(reading.getMeter().getMeterNumber())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .readingDate(reading.getReadingDate())
                .billingMonth(reading.getBillingMonth())
                .billingYear(reading.getBillingYear())
                .createdAt(reading.getCreatedAt())
                .build();
    }
}

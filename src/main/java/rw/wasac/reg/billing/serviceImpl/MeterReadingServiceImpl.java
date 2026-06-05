/**
 * Operator meter reading capture with auto-filled previous values and correction support.
 */
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
import rw.wasac.reg.billing.repository.BillRepository;
import rw.wasac.reg.billing.repository.MeterReadingRepository;
import rw.wasac.reg.billing.repository.MeterRepository;
import rw.wasac.reg.billing.service.MeterReadingService;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingServiceImpl implements MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterRepository meterRepository;
    private final BillRepository billRepository;
    private final MeterServiceImpl meterService;
    private final CustomerServiceImpl customerService;

    @Override
    @Transactional
    public MeterReadingResponse create(MeterReadingRequest request) {
        Meter meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + request.getMeterId()));

        meterService.assertMeterActive(meter);
        customerService.assertCustomerActive(meter.getCustomer());

        BigDecimal previousReading = request.getPreviousReading() != null
                ? request.getPreviousReading()
                : meterReadingRepository.findFirstByMeterIdOrderByReadingDateDesc(meter.getId())
                        .map(MeterReading::getCurrentReading)
                        .orElse(BigDecimal.ZERO);

        if (request.getCurrentReading().compareTo(previousReading) <= 0) {
            throw new BadRequestException(
                    "Current reading (" + request.getCurrentReading() + ") must be greater than previous reading ("
                            + previousReading + ")");
        }

        int billingMonth = request.getReadingDate().getMonthValue();
        int billingYear = request.getReadingDate().getYear();

        if (meterReadingRepository.existsByMeterIdAndBillingMonthAndBillingYear(
                meter.getId(), billingMonth, billingYear)) {
            throw new DuplicateResourceException(
                    "A reading already exists for meter " + meter.getMeterNumber() + " in "
                            + billingMonth + "/" + billingYear + ". Void the incorrect reading first.");
        }

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(previousReading)
                .currentReading(request.getCurrentReading())
                .readingDate(request.getReadingDate())
                .billingMonth(billingMonth)
                .billingYear(billingYear)
                .build();

        return toResponse(meterReadingRepository.save(reading));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSuggestedPreviousReading(Long meterId) {
        if (!meterRepository.existsById(meterId)) {
            throw new ResourceNotFoundException("Meter not found with id: " + meterId);
        }
        return meterReadingRepository.findFirstByMeterIdOrderByReadingDateDesc(meterId)
                .map(MeterReading::getCurrentReading)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public void voidReading(Long id) {
        MeterReading reading = findEntity(id);
        if (billRepository.existsByMeterReadingId(id)) {
            throw new BadRequestException(
                    "Cannot void reading id " + id + " because a bill has already been generated from it");
        }
        meterReadingRepository.delete(reading);
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

package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.MeterRequest;
import rw.wasac.reg.billing.dto.response.MeterResponse;
import rw.wasac.reg.billing.entity.Customer;
import rw.wasac.reg.billing.entity.Meter;
import rw.wasac.reg.billing.enums.MeterStatus;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.exception.DuplicateResourceException;
import rw.wasac.reg.billing.exception.ResourceNotFoundException;
import rw.wasac.reg.billing.repository.CustomerRepository;
import rw.wasac.reg.billing.repository.MeterRepository;
import rw.wasac.reg.billing.service.MeterService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterServiceImpl implements MeterService {

    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;
    private final CustomerServiceImpl customerService;

    @Override
    @Transactional
    public MeterResponse create(MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already exists: " + request.getMeterNumber());
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        customerService.assertCustomerActive(customer);

        Meter meter = Meter.builder()
                .meterNumber(request.getMeterNumber())
                .type(request.getType())
                .installationDate(request.getInstallationDate())
                .status(request.getStatus() != null ? request.getStatus() : MeterStatus.ACTIVE)
                .customer(customer)
                .build();

        return toResponse(meterRepository.save(meter));
    }

    @Override
    @Transactional
    public MeterResponse update(Long id, MeterRequest request) {
        Meter meter = findEntity(id);

        if (!meter.getMeterNumber().equals(request.getMeterNumber())
                && meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already exists: " + request.getMeterNumber());
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        meter.setMeterNumber(request.getMeterNumber());
        meter.setType(request.getType());
        meter.setInstallationDate(request.getInstallationDate());
        if (request.getStatus() != null) {
            meter.setStatus(request.getStatus());
        }
        meter.setCustomer(customer);

        return toResponse(meterRepository.save(meter));
    }

    @Override
    public MeterResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    public List<MeterResponse> getAll() {
        return meterRepository.findAllWithCustomer().stream().map(this::toResponse).toList();
    }

    @Override
    public List<MeterResponse> getByCustomerId(Long customerId) {
        return meterRepository.findByCustomerIdWithCustomer(customerId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!meterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meter not found with id: " + id);
        }
        meterRepository.deleteById(id);
    }

    @Override
    @Transactional
    public MeterResponse activate(Long id) {
        Meter meter = findEntity(id);
        meter.setStatus(MeterStatus.ACTIVE);
        return toResponse(meterRepository.save(meter));
    }

    @Override
    @Transactional
    public MeterResponse deactivate(Long id) {
        Meter meter = findEntity(id);
        meter.setStatus(MeterStatus.INACTIVE);
        return toResponse(meterRepository.save(meter));
    }

    public void assertMeterActive(Meter meter) {
        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BadRequestException("Meter is inactive: " + meter.getMeterNumber());
        }
    }

    private Meter findEntity(Long id) {
        return meterRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + id));
    }

    private MeterResponse toResponse(Meter meter) {
        return MeterResponse.builder()
                .id(meter.getId())
                .meterNumber(meter.getMeterNumber())
                .type(meter.getType())
                .installationDate(meter.getInstallationDate())
                .status(meter.getStatus())
                .customerId(meter.getCustomer().getId())
                .customerName(meter.getCustomer().getFullName())
                .createdAt(meter.getCreatedAt())
                .build();
    }
}

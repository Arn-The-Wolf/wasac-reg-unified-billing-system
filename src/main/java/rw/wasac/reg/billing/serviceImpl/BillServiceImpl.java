/**
 * Service implementation providing Bill business logic.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.BillGenerateRequest;
import rw.wasac.reg.billing.dto.response.BillResponse;
import rw.wasac.reg.billing.entity.*;
import rw.wasac.reg.billing.enums.BillStatus;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.exception.DuplicateResourceException;
import rw.wasac.reg.billing.exception.ResourceNotFoundException;
import rw.wasac.reg.billing.repository.*;
import rw.wasac.reg.billing.service.BillService;
import rw.wasac.reg.billing.service.BillingNotificationService;
import rw.wasac.reg.billing.utils.BillingCalculator;
import rw.wasac.reg.billing.utils.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final TariffRepository tariffRepository;
    private final FixedChargeRepository fixedChargeRepository;
    private final TaxRepository taxRepository;
    private final PenaltyRepository penaltyRepository;
    private final BillingCalculator billingCalculator;
    private final CustomerServiceImpl customerService;
    private final UserRepository userRepository;
    private final BillingNotificationService billingNotificationService;

    @Override
    @Transactional
    public BillResponse generateBill(BillGenerateRequest request) {
        MeterReading reading = meterReadingRepository.findById(request.getMeterReadingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Meter reading not found with id: " + request.getMeterReadingId()));

        Meter meter = reading.getMeter();
        Customer customer = meter.getCustomer();
        customerService.assertCustomerActive(customer);

        if (billRepository.findByMeterIdAndBillingMonthAndBillingYear(
                meter.getId(), reading.getBillingMonth(), reading.getBillingYear()).isPresent()) {
            throw new DuplicateResourceException("Bill already exists for this meter and billing period");
        }

        LocalDate periodStart = LocalDate.of(reading.getBillingYear(), reading.getBillingMonth(), 1);

        Tariff tariff = tariffRepository.findActiveTariffForPeriod(meter.getType(), periodStart)
                .orElseThrow(() -> new BadRequestException(
                        "No active tariff for meter type " + meter.getType() + " on " + periodStart));

        BigDecimal consumption = reading.getCurrentReading().subtract(reading.getPreviousReading());
        BigDecimal tariffAmount = billingCalculator.calculateTariffAmount(tariff, consumption);

        BigDecimal fixedChargeAmount = fixedChargeRepository.findActiveForPeriod(periodStart)
                .map(FixedCharge::getAmount)
                .orElse(BigDecimal.ZERO);

        BigDecimal subtotal = tariffAmount.add(fixedChargeAmount);

        BigDecimal taxAmount = taxRepository.findActiveForPeriod(periodStart)
                .map(tax -> billingCalculator.calculateTax(subtotal, tax.getPercentage()))
                .orElse(BigDecimal.ZERO);

        BigDecimal penaltyAmount = penaltyRepository.findActiveForPeriod(periodStart)
                .map(penalty -> billingCalculator.calculatePenalty(subtotal, penalty.getPercentage()))
                .orElse(BigDecimal.ZERO);

        BigDecimal totalAmount = subtotal.add(taxAmount).add(penaltyAmount);

        Bill bill = Bill.builder()
                .reference("BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(customer)
                .meter(meter)
                .meterReading(reading)
                .billingMonth(reading.getBillingMonth())
                .billingYear(reading.getBillingYear())
                .consumption(consumption)
                .tariffAmount(tariffAmount)
                .fixedChargeAmount(fixedChargeAmount)
                .taxAmount(taxAmount)
                .penaltyAmount(penaltyAmount)
                .totalAmount(totalAmount)
                .amountPaid(BigDecimal.ZERO)
                .balance(totalAmount)
                .status(BillStatus.PENDING)
                .build();

        Bill saved = billRepository.save(bill);
        billingNotificationService.notifyBillGenerated(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getById(Long id) {
        Bill bill = findEntity(id);
        assertCustomerCanAccessBill(bill);
        return toResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getByReference(String reference) {
        Bill bill = billRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with reference: " + reference));
        assertCustomerCanAccessBill(bill);
        return toResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getAll() {
        return billRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getByCustomerId(Long customerId) {
        Long scopedCustomerId = resolveCustomerScope(customerId);
        return billRepository.findByCustomerId(scopedCustomerId).stream().map(this::toResponse).toList();
    }

    private Bill findEntity(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }

    private Long resolveCustomerScope(Long requestedCustomerId) {
        if (!SecurityUtils.isCustomer()) {
            return requestedCustomerId;
        }
        Long ownCustomerId = getCurrentCustomerId();
        if (requestedCustomerId != null && !requestedCustomerId.equals(ownCustomerId)) {
            throw new AccessDeniedException("Customers may only access their own bills");
        }
        return ownCustomerId;
    }

    private void assertCustomerCanAccessBill(Bill bill) {
        if (!SecurityUtils.isCustomer()) {
            return;
        }
        Long ownCustomerId = getCurrentCustomerId();
        if (!bill.getCustomer().getId().equals(ownCustomerId)) {
            throw new AccessDeniedException("Customers may only access their own bills");
        }
    }

    private Long getCurrentCustomerId() {
        User user = userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
        if (user.getCustomer() == null) {
            throw new AccessDeniedException("Customer account is not linked to a customer profile");
        }
        return user.getCustomer().getId();
    }

    private BillResponse toResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .reference(bill.getReference())
                .customerId(bill.getCustomer().getId())
                .customerName(bill.getCustomer().getFullName())
                .meterId(bill.getMeter().getId())
                .meterNumber(bill.getMeter().getMeterNumber())
                .billingMonth(bill.getBillingMonth())
                .billingYear(bill.getBillingYear())
                .consumption(bill.getConsumption())
                .tariffAmount(bill.getTariffAmount())
                .fixedChargeAmount(bill.getFixedChargeAmount())
                .taxAmount(bill.getTaxAmount())
                .penaltyAmount(bill.getPenaltyAmount())
                .totalAmount(bill.getTotalAmount())
                .amountPaid(bill.getAmountPaid())
                .balance(bill.getBalance())
                .status(bill.getStatus())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}

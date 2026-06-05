/**
 * Generates styled PDF receipts for customers and operational reports for admin/inspector roles.
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.entity.*;
import rw.wasac.reg.billing.enums.BillStatus;
import rw.wasac.reg.billing.enums.PaymentStatus;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.exception.ResourceNotFoundException;
import rw.wasac.reg.billing.repository.*;
import rw.wasac.reg.billing.service.PdfReportService;
import rw.wasac.reg.billing.utils.SecurityUtils;
import rw.wasac.reg.billing.utils.WasacPdfBuilder;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfReportServiceImpl implements PdfReportService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePaymentReceipt(Long paymentId) {
        Payment payment = paymentRepository.findByIdWithDetails(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new BadRequestException("Receipt is only available for approved payments");
        }

        Bill bill = payment.getBill();
        assertCustomerOwnsBill(bill);

        String period = formatPeriod(bill.getBillingMonth(), bill.getBillingYear());
        return WasacPdfBuilder.build("Payment Receipt", document -> {
            WasacPdfBuilder.addHighlightBox(document,
                    "Receipt No: PAY-" + payment.getId() + "  |  Status: APPROVED");
            WasacPdfBuilder.addSectionTitle(document, "Customer Details");
            WasacPdfBuilder.addKeyValueRow(document, "Customer Name", bill.getCustomer().getFullName());
            WasacPdfBuilder.addKeyValueRow(document, "National ID", bill.getCustomer().getNationalId());
            WasacPdfBuilder.addKeyValueRow(document, "Phone", bill.getCustomer().getPhone());
            WasacPdfBuilder.addKeyValueRow(document, "Address", bill.getCustomer().getAddress());

            WasacPdfBuilder.addSectionTitle(document, "Payment Details");
            WasacPdfBuilder.addKeyValueRow(document, "Bill Reference", bill.getReference());
            WasacPdfBuilder.addKeyValueRow(document, "Billing Period", period);
            WasacPdfBuilder.addKeyValueRow(document, "Amount Paid", payment.getAmount().toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Payment Method",
                    payment.getPaymentMethod().name().replace('_', ' '));
            WasacPdfBuilder.addKeyValueRow(document, "Payment Date", payment.getPaymentDate().toString());
            WasacPdfBuilder.addKeyValueRow(document, "Remaining Balance", bill.getBalance().toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Bill Status", bill.getStatus().name());

            WasacPdfBuilder.addParagraph(document,
                    "Thank you for your payment. This document serves as your official WASAC utility payment receipt.");
        });
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateBillReceipt(Long billId) {
        Bill bill = billRepository.findByIdWithDetails(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));
        assertCustomerOwnsBill(bill);

        String period = formatPeriod(bill.getBillingMonth(), bill.getBillingYear());
        return WasacPdfBuilder.build("Utility Bill Statement", document -> {
            WasacPdfBuilder.addHighlightBox(document,
                    "Bill Ref: " + bill.getReference() + "  |  Status: " + bill.getStatus());
            WasacPdfBuilder.addSectionTitle(document, "Customer Details");
            WasacPdfBuilder.addKeyValueRow(document, "Customer Name", bill.getCustomer().getFullName());
            WasacPdfBuilder.addKeyValueRow(document, "Meter Number", bill.getMeter().getMeterNumber());
            WasacPdfBuilder.addKeyValueRow(document, "Service Type", bill.getMeter().getType().name());
            WasacPdfBuilder.addKeyValueRow(document, "Billing Period", period);

            WasacPdfBuilder.addSectionTitle(document, "Charge Breakdown");
            WasacPdfBuilder.addKeyValueRow(document, "Consumption", bill.getConsumption().toPlainString() + " units");
            WasacPdfBuilder.addKeyValueRow(document, "Tariff Amount", bill.getTariffAmount().toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Fixed Charge", bill.getFixedChargeAmount().toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Tax (VAT)", bill.getTaxAmount().toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Penalty", bill.getPenaltyAmount().toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Total Amount", bill.getTotalAmount().toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Amount Paid", bill.getAmountPaid().toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Outstanding Balance", bill.getBalance().toPlainString() + " FRW");
        });
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateAdminBillingReport(int month, int year) {
        validatePeriod(month, year);
        List<Bill> bills = billRepository.findByPeriodWithDetails(month, year);
        List<Customer> customers = customerRepository.findAll();

        BigDecimal totalBilled = bills.stream().map(Bill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCollected = bills.stream().map(Bill::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutstanding = bills.stream().map(Bill::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        long paidCount = bills.stream().filter(b -> b.getStatus() == BillStatus.PAID).count();

        String period = formatPeriod(month, year);
        List<String[]> rows = new ArrayList<>();
        for (Bill bill : bills) {
            rows.add(new String[]{
                    bill.getReference(),
                    bill.getCustomer().getFullName(),
                    bill.getMeter().getMeterNumber(),
                    bill.getTotalAmount().toPlainString(),
                    bill.getBalance().toPlainString(),
                    bill.getStatus().name()
            });
        }

        return WasacPdfBuilder.build("Admin Billing Summary Report", document -> {
            WasacPdfBuilder.addHighlightBox(document, "Reporting Period: " + period);
            WasacPdfBuilder.addSectionTitle(document, "Executive Summary");
            WasacPdfBuilder.addKeyValueRow(document, "Total Registered Customers", String.valueOf(customers.size()));
            WasacPdfBuilder.addKeyValueRow(document, "Bills Generated", String.valueOf(bills.size()));
            WasacPdfBuilder.addKeyValueRow(document, "Fully Paid Bills", String.valueOf(paidCount));
            WasacPdfBuilder.addKeyValueRow(document, "Total Billed", totalBilled.toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Total Collected", totalCollected.toPlainString() + " FRW");
            WasacPdfBuilder.addKeyValueRow(document, "Total Outstanding", totalOutstanding.toPlainString() + " FRW");

            WasacPdfBuilder.addSectionTitle(document, "Bill Register");
            if (rows.isEmpty()) {
                WasacPdfBuilder.addParagraph(document, "No bills were generated for this period.");
            } else {
                WasacPdfBuilder.addDataTable(document,
                        new String[]{"Reference", "Customer", "Meter", "Total (FRW)", "Balance (FRW)", "Status"},
                        rows);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateInspectorMeterReport(int month, int year) {
        validatePeriod(month, year);
        List<MeterReading> readings = meterReadingRepository.findByPeriodWithDetails(month, year);

        String period = formatPeriod(month, year);
        List<String[]> rows = new ArrayList<>();
        BigDecimal totalConsumption = BigDecimal.ZERO;

        for (MeterReading reading : readings) {
            BigDecimal consumption = reading.getCurrentReading().subtract(reading.getPreviousReading());
            totalConsumption = totalConsumption.add(consumption);
            rows.add(new String[]{
                    reading.getMeter().getMeterNumber(),
                    reading.getMeter().getCustomer().getFullName(),
                    reading.getMeter().getType().name(),
                    reading.getPreviousReading().toPlainString(),
                    reading.getCurrentReading().toPlainString(),
                    consumption.toPlainString(),
                    reading.getReadingDate().toString()
            });
        }

        final BigDecimal totalConsumptionForReport = totalConsumption;
        return WasacPdfBuilder.build("Inspector Meter Reading Report", document -> {
            WasacPdfBuilder.addHighlightBox(document, "Inspection Period: " + period);
            WasacPdfBuilder.addSectionTitle(document, "Inspection Summary");
            WasacPdfBuilder.addKeyValueRow(document, "Total Readings Captured", String.valueOf(readings.size()));
            WasacPdfBuilder.addKeyValueRow(document, "Total Consumption Recorded",
                    totalConsumptionForReport.toPlainString() + " units");
            WasacPdfBuilder.addKeyValueRow(document, "Report Type", "Field Meter Inspection & Verification");

            WasacPdfBuilder.addSectionTitle(document, "Meter Reading Details");
            if (rows.isEmpty()) {
                WasacPdfBuilder.addParagraph(document, "No meter readings were recorded for this period.");
            } else {
                WasacPdfBuilder.addDataTable(document,
                        new String[]{"Meter No.", "Customer", "Type", "Previous", "Current", "Consumption", "Date"},
                        rows);
            }

            WasacPdfBuilder.addParagraph(document,
                    "This report certifies meter readings captured during the billing period for WASAC/REG verification.");
        });
    }

    private void assertCustomerOwnsBill(Bill bill) {
        if (!SecurityUtils.isCustomer()) {
            return;
        }
        User user = userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
        if (user.getCustomer() == null
                || !bill.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new AccessDeniedException("Customers may only download receipts for their own bills");
        }
    }

    private void validatePeriod(int month, int year) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new BadRequestException("Year must be between 2000 and 2100");
        }
    }

    private String formatPeriod(int month, int year) {
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return monthName + " " + year;
    }
}

/**
 * REST controller for PDF receipt and report generation (customer, admin, inspector).
 */
package rw.wasac.reg.billing.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.service.PdfReportService;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Reports & Receipts", description = "PDF receipts and operational reports")
public class ReportController {

    private final PdfReportService pdfReportService;

    @GetMapping("/receipts/payments/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Download payment receipt PDF")
    public ResponseEntity<byte[]> paymentReceipt(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long paymentId) {
        byte[] pdf = pdfReportService.generatePaymentReceipt(paymentId);
        return pdfResponse(pdf, "wasac-payment-receipt-" + paymentId + ".pdf");
    }

    @GetMapping("/receipts/bills/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Download bill statement PDF")
    public ResponseEntity<byte[]> billReceipt(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long billId) {
        byte[] pdf = pdfReportService.generateBillReceipt(billId);
        return pdfResponse(pdf, "wasac-bill-statement-" + billId + ".pdf");
    }

    @GetMapping("/admin/billing-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Download admin billing summary report PDF")
    public ResponseEntity<byte[]> adminBillingReport(
            @RequestParam @Min(value = 1, message = AppConstants.BILLING_MONTH_MESSAGE)
            @Max(value = 12, message = AppConstants.BILLING_MONTH_MESSAGE) int month,
            @RequestParam @Min(value = 2000, message = AppConstants.BILLING_YEAR_MESSAGE)
            @Max(value = 2100, message = AppConstants.BILLING_YEAR_MESSAGE) int year) {
        byte[] pdf = pdfReportService.generateAdminBillingReport(month, year);
        return pdfResponse(pdf, "wasac-admin-billing-" + month + "-" + year + ".pdf");
    }

    @GetMapping("/inspector/meter-readings")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSPECTOR', 'OPERATOR')")
    @Operation(summary = "Download inspector meter reading report PDF")
    public ResponseEntity<byte[]> inspectorMeterReport(
            @RequestParam @Min(value = 1, message = AppConstants.BILLING_MONTH_MESSAGE)
            @Max(value = 12, message = AppConstants.BILLING_MONTH_MESSAGE) int month,
            @RequestParam @Min(value = 2000, message = AppConstants.BILLING_YEAR_MESSAGE)
            @Max(value = 2100, message = AppConstants.BILLING_YEAR_MESSAGE) int year) {
        byte[] pdf = pdfReportService.generateInspectorMeterReport(month, year);
        return pdfResponse(pdf, "wasac-inspector-meters-" + month + "-" + year + ".pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

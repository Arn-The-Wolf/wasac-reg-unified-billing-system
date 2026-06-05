/**
 * REST controller for PDF receipt and report generation (customer, admin, inspector).
 */
package rw.wasac.reg.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.wasac.reg.billing.service.PdfReportService;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Reports & Receipts", description = "PDF receipts and operational reports")
public class ReportController {

    private final PdfReportService pdfReportService;

    @GetMapping("/receipts/payments/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Download payment receipt PDF")
    public ResponseEntity<byte[]> paymentReceipt(@PathVariable Long paymentId) {
        byte[] pdf = pdfReportService.generatePaymentReceipt(paymentId);
        return pdfResponse(pdf, "wasac-payment-receipt-" + paymentId + ".pdf");
    }

    @GetMapping("/receipts/bills/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Download bill statement PDF")
    public ResponseEntity<byte[]> billReceipt(@PathVariable Long billId) {
        byte[] pdf = pdfReportService.generateBillReceipt(billId);
        return pdfResponse(pdf, "wasac-bill-statement-" + billId + ".pdf");
    }

    @GetMapping("/admin/billing-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Download admin billing summary report PDF")
    public ResponseEntity<byte[]> adminBillingReport(
            @RequestParam int month,
            @RequestParam int year) {
        byte[] pdf = pdfReportService.generateAdminBillingReport(month, year);
        return pdfResponse(pdf, "wasac-admin-billing-" + month + "-" + year + ".pdf");
    }

    @GetMapping("/inspector/meter-readings")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSPECTOR', 'OPERATOR')")
    @Operation(summary = "Download inspector meter reading report PDF")
    public ResponseEntity<byte[]> inspectorMeterReport(
            @RequestParam int month,
            @RequestParam int year) {
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

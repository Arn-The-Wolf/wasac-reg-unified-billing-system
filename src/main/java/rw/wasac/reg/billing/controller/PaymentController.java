/**
 * REST controller exposing PaymentController endpoints for the WASAC/REG billing system.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.wasac.reg.billing.dto.request.PaymentRequest;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.dto.response.PaymentResponse;
import rw.wasac.reg.billing.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Payment", description = "Bill payment APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> record(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded", paymentService.recordPayment(request)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<PaymentResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment approved", paymentService.approvePayment(id)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<PaymentResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment rejected", paymentService.rejectPayment(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", paymentService.getAll()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success("Pending payments retrieved", paymentService.getPending()));
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByBill(@PathVariable Long billId) {
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", paymentService.getByBillId(billId)));
    }
}

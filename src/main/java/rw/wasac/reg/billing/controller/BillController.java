/**
 * REST controller exposing BillController endpoints for the WASAC/REG billing system.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.dto.request.BillGenerateRequest;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.dto.response.BillResponse;
import rw.wasac.reg.billing.service.BillService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Bill", description = "Utility bill management APIs")
public class BillController {

    private final BillService billService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<BillResponse>> generate(@Valid @RequestBody BillGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bill generated", billService.generateBill(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Bills retrieved", billService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<BillResponse>> getById(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved", billService.getById(id)));
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<BillResponse>> getByReference(
            @PathVariable
            @NotBlank(message = "Bill reference is required")
            @Pattern(regexp = AppConstants.BILL_REFERENCE_PATTERN, message = AppConstants.BILL_REFERENCE_MESSAGE)
            String reference) {
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved", billService.getByReference(reference)));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getByCustomer(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long customerId) {
        return ResponseEntity.ok(ApiResponse.success("Bills retrieved", billService.getByCustomerId(customerId)));
    }
}

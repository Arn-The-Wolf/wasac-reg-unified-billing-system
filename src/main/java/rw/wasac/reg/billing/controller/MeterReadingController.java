/**
 * REST controller exposing MeterReadingController endpoints for the WASAC/REG billing system.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.dto.request.MeterReadingRequest;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.dto.response.MeterReadingResponse;
import rw.wasac.reg.billing.service.MeterReadingService;

@RestController
@RequestMapping("/api/v1/meter-readings")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Meter Reading", description = "Meter reading APIs")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'INSPECTOR')")
    public ResponseEntity<ApiResponse<MeterReadingResponse>> create(@Valid @RequestBody MeterReadingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reading recorded", meterReadingService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'INSPECTOR', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<MeterReadingResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Readings retrieved", meterReadingService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<MeterReadingResponse>> getById(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long id) {
        return ResponseEntity.ok(ApiResponse.success("Reading retrieved", meterReadingService.getById(id)));
    }

    @GetMapping("/meter/{meterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<MeterReadingResponse>>> getByMeter(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long meterId) {
        return ResponseEntity.ok(ApiResponse.success("Readings retrieved", meterReadingService.getByMeterId(meterId)));
    }

    @GetMapping("/meter/{meterId}/suggested-previous")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'INSPECTOR')")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getSuggestedPrevious(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long meterId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Suggested previous reading retrieved", meterReadingService.getSuggestedPreviousReading(meterId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'INSPECTOR')")
    public ResponseEntity<ApiResponse<Void>> voidReading(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long id) {
        meterReadingService.voidReading(id);
        return ResponseEntity.ok(ApiResponse.success("Meter reading voided successfully"));
    }
}

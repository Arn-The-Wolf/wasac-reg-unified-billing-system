/**
 * REST controller exposing TariffConfigController endpoints for the WASAC/REG billing system.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.dto.request.*;
import rw.wasac.reg.billing.dto.response.*;
import rw.wasac.reg.billing.service.TariffConfigService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Tariff Configuration", description = "Admin-only tariff, tax, and penalty configuration")
public class TariffConfigController {

    private final TariffConfigService tariffConfigService;

    @PostMapping("/tariffs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> createTariff(@Valid @RequestBody TariffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tariff created", tariffConfigService.createTariff(request)));
    }

    @GetMapping("/tariffs")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<TariffResponse>>> getTariffs() {
        return ResponseEntity.ok(ApiResponse.success("Tariffs retrieved", tariffConfigService.getAllTariffs()));
    }

    @GetMapping("/tariffs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<TariffResponse>> getTariff(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tariff retrieved", tariffConfigService.getTariffById(id)));
    }

    @PostMapping("/fixed-charges")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FixedChargeResponse>> createFixedCharge(@Valid @RequestBody FixedChargeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fixed charge created", tariffConfigService.createFixedCharge(request)));
    }

    @GetMapping("/fixed-charges")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<FixedChargeResponse>>> getFixedCharges() {
        return ResponseEntity.ok(ApiResponse.success("Fixed charges retrieved", tariffConfigService.getAllFixedCharges()));
    }

    @PostMapping("/taxes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TaxResponse>> createTax(@Valid @RequestBody TaxRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tax created", tariffConfigService.createTax(request)));
    }

    @GetMapping("/taxes")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<TaxResponse>>> getTaxes() {
        return ResponseEntity.ok(ApiResponse.success("Taxes retrieved", tariffConfigService.getAllTaxes()));
    }

    @PostMapping("/penalties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PenaltyResponse>> createPenalty(@Valid @RequestBody PenaltyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Penalty created", tariffConfigService.createPenalty(request)));
    }

    @GetMapping("/penalties")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<PenaltyResponse>>> getPenalties() {
        return ResponseEntity.ok(ApiResponse.success("Penalties retrieved", tariffConfigService.getAllPenalties()));
    }
}

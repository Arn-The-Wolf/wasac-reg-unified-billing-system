/**
 * Request DTO for TariffRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.enums.MeterType;
import rw.wasac.reg.billing.enums.TariffType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TariffRequest {

    @NotBlank(message = AppConstants.CONFIG_NAME_REQUIRED_MESSAGE)
    @Size(min = 2, max = 100, message = AppConstants.CONFIG_NAME_SIZE_MESSAGE)
    private String name;

    @NotNull(message = AppConstants.TARIFF_TYPE_REQUIRED_MESSAGE)
    private TariffType tariffType;

    @NotNull(message = AppConstants.TARIFF_METER_TYPE_REQUIRED_MESSAGE)
    private MeterType meterType;

    @NotNull(message = AppConstants.EFFECTIVE_FROM_REQUIRED_MESSAGE)
    private LocalDate effectiveFrom;

    /** Optional end date; when set, tariff does not apply to billing periods after this date. */
    private LocalDate effectiveTo;

    @DecimalMin(value = "0.0", inclusive = true, message = AppConstants.FLAT_RATE_REQUIRED_MESSAGE)
    @Digits(integer = 10, fraction = 2, message = "Flat rate must have at most 10 integer digits and 2 decimal places")
    private BigDecimal flatRate;

    @Valid
    private List<TariffTierRequest> tiers = new ArrayList<>();

    @AssertTrue(message = AppConstants.EFFECTIVE_TO_AFTER_FROM_MESSAGE)
    public boolean isEffectiveToAfterFrom() {
        return effectiveTo == null || effectiveFrom == null || effectiveTo.isAfter(effectiveFrom);
    }

    @AssertTrue(message = AppConstants.FLAT_RATE_REQUIRED_MESSAGE)
    public boolean isFlatRateValidForFlatTariff() {
        if (tariffType != TariffType.FLAT) {
            return true;
        }
        return flatRate != null && flatRate.compareTo(BigDecimal.ZERO) > 0;
    }

    @AssertTrue(message = AppConstants.TARIFF_TIERS_REQUIRED_MESSAGE)
    public boolean isTiersValidForTierTariff() {
        if (tariffType != TariffType.TIER) {
            return true;
        }
        return tiers != null && !tiers.isEmpty();
    }
}

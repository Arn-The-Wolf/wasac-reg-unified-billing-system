/**
 * Request DTO for TariffTierRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;

import java.math.BigDecimal;

@Data
public class TariffTierRequest {

    @NotNull(message = AppConstants.TIER_FROM_UNITS_REQUIRED_MESSAGE)
    @DecimalMin(value = "0.0", inclusive = true, message = AppConstants.TIER_FROM_UNITS_REQUIRED_MESSAGE)
    @Digits(integer = 10, fraction = 3, message = "Tier from-units must have at most 10 integer digits and 3 decimal places")
    private BigDecimal fromUnits;

    @DecimalMin(value = "0.0", inclusive = true, message = AppConstants.TIER_TO_UNITS_MIN_MESSAGE)
    @Digits(integer = 10, fraction = 3, message = "Tier to-units must have at most 10 integer digits and 3 decimal places")
    private BigDecimal toUnits;

    @NotNull(message = AppConstants.TIER_RATE_REQUIRED_MESSAGE)
    @DecimalMin(value = "0.01", message = AppConstants.TIER_RATE_MIN_MESSAGE)
    @Digits(integer = 10, fraction = 2, message = "Tier rate must have at most 10 integer digits and 2 decimal places")
    private BigDecimal ratePerUnit;

    @AssertTrue(message = AppConstants.TIER_RANGE_MESSAGE)
    public boolean isUnitRangeValid() {
        if (fromUnits == null || toUnits == null) {
            return true;
        }
        return toUnits.compareTo(fromUnits) > 0;
    }
}

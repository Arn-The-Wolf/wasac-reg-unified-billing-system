/**
 * Request DTO for TaxRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TaxRequest {

    @NotBlank(message = AppConstants.CONFIG_NAME_REQUIRED_MESSAGE)
    @Size(min = 2, max = 100, message = AppConstants.CONFIG_NAME_SIZE_MESSAGE)
    private String name;

    @NotNull(message = AppConstants.PERCENTAGE_REQUIRED_MESSAGE)
    @DecimalMin(value = "0.01", message = AppConstants.PERCENTAGE_RANGE_MESSAGE)
    @DecimalMax(value = "100.0", message = AppConstants.PERCENTAGE_RANGE_MESSAGE)
    @Digits(integer = 3, fraction = 2, message = "Tax percentage must have at most 3 integer digits and 2 decimal places")
    private BigDecimal percentage;

    @NotNull(message = AppConstants.EFFECTIVE_FROM_REQUIRED_MESSAGE)
    private LocalDate effectiveFrom;
}

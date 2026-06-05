/**
 * Request DTO for TariffTierRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TariffTierRequest {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal fromUnits;

    @DecimalMin("0.0")
    private BigDecimal toUnits;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal ratePerUnit;
}

/**
 * Request DTO for PenaltyRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PenaltyRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal percentage;

    @NotNull
    private LocalDate effectiveFrom;
}

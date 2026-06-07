/**
 * Request DTO for MeterReadingRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MeterReadingRequest {

    @NotNull(message = AppConstants.METER_ID_REQUIRED_MESSAGE)
    @Positive(message = AppConstants.ID_POSITIVE_MESSAGE)
    private Long meterId;

    /** Optional — auto-filled from the meter's last reading when omitted. */
    @DecimalMin(value = "0.0", inclusive = true, message = AppConstants.READING_MIN_MESSAGE)
    @Digits(integer = 10, fraction = 3, message = "Previous reading must have at most 10 integer digits and 3 decimal places")
    private BigDecimal previousReading;

    @NotNull(message = AppConstants.READING_REQUIRED_MESSAGE)
    @DecimalMin(value = "0.0", inclusive = true, message = AppConstants.READING_MIN_MESSAGE)
    @Digits(integer = 10, fraction = 3, message = "Current reading must have at most 10 integer digits and 3 decimal places")
    private BigDecimal currentReading;

    @NotNull(message = AppConstants.DATE_REQUIRED_MESSAGE)
    @PastOrPresent(message = AppConstants.READING_DATE_MESSAGE)
    private LocalDate readingDate;
}

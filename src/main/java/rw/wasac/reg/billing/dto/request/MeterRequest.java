/**
 * Request DTO for MeterRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.enums.MeterStatus;
import rw.wasac.reg.billing.enums.MeterType;

import java.time.LocalDate;

@Data
public class MeterRequest {

    @NotBlank(message = AppConstants.METER_NUMBER_SIZE_MESSAGE)
    @Size(min = 3, max = 50, message = AppConstants.METER_NUMBER_SIZE_MESSAGE)
    @Pattern(regexp = AppConstants.METER_NUMBER_PATTERN, message = AppConstants.METER_NUMBER_MESSAGE)
    private String meterNumber;

    @NotNull(message = AppConstants.METER_TYPE_REQUIRED_MESSAGE)
    private MeterType type;

    @NotNull(message = AppConstants.DATE_REQUIRED_MESSAGE)
    @PastOrPresent(message = AppConstants.INSTALLATION_DATE_MESSAGE)
    private LocalDate installationDate;

    private MeterStatus status = MeterStatus.ACTIVE;

    @NotNull(message = AppConstants.CUSTOMER_ID_REQUIRED_MESSAGE)
    private Long customerId;
}

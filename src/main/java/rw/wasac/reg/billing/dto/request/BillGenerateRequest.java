/**
 * Request DTO for BillGenerateRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;

@Data
public class BillGenerateRequest {

    @NotNull(message = AppConstants.METER_READING_ID_REQUIRED_MESSAGE)
    @Positive(message = AppConstants.ID_POSITIVE_MESSAGE)
    private Long meterReadingId;
}

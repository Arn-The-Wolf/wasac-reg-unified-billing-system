/**
 * Request DTO for BillGenerateRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;

@Data
public class BillGenerateRequest {

    @NotNull(message = AppConstants.METER_READING_ID_REQUIRED_MESSAGE)
    private Long meterReadingId;
}

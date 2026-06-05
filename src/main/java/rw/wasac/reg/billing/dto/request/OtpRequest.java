/**
 * Request DTO for OtpRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;

@Data
public class OtpRequest {

    @NotBlank(message = AppConstants.EMAIL_REQUIRED_MESSAGE)
    @Email(message = AppConstants.EMAIL_MESSAGE)
    private String email;
}

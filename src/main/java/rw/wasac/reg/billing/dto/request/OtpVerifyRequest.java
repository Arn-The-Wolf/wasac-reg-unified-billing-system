/**
 * Request DTO for OtpVerifyRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;

@Data
public class OtpVerifyRequest {

    @NotBlank(message = AppConstants.EMAIL_REQUIRED_MESSAGE)
    @Email(message = AppConstants.EMAIL_MESSAGE)
    @Size(max = 150, message = AppConstants.EMAIL_SIZE_MESSAGE)
    private String email;

    @NotBlank(message = AppConstants.OTP_CODE_REQUIRED_MESSAGE)
    @Size(min = 6, max = 6, message = AppConstants.OTP_CODE_MESSAGE)
    @Pattern(regexp = AppConstants.OTP_CODE_PATTERN, message = AppConstants.OTP_CODE_MESSAGE)
    private String code;
}

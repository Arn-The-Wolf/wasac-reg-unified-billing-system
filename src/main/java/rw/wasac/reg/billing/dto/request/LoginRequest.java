/**
 * Request DTO for LoginRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;

@Data
public class LoginRequest {

    @NotBlank(message = AppConstants.EMAIL_REQUIRED_MESSAGE)
    @Email(message = AppConstants.EMAIL_MESSAGE)
    @Size(max = 150, message = AppConstants.EMAIL_SIZE_MESSAGE)
    private String email;

    @NotBlank(message = AppConstants.LOGIN_PASSWORD_REQUIRED_MESSAGE)
    private String password;
}

/**
 * Request DTO for SignupRequest with validation constraints.
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
import rw.wasac.reg.billing.enums.Role;

@Data
public class SignupRequest {

    @NotBlank(message = AppConstants.FULL_NAME_REQUIRED_MESSAGE)
    @Size(min = 2, max = 150, message = AppConstants.FULL_NAME_SIZE_MESSAGE)
    private String fullName;

    @NotBlank(message = AppConstants.EMAIL_REQUIRED_MESSAGE)
    @Email(message = AppConstants.EMAIL_MESSAGE)
    @Size(max = 150, message = AppConstants.EMAIL_MESSAGE)
    private String email;

    @Pattern(regexp = AppConstants.COUNTRY_CODE_PATTERN, message = AppConstants.COUNTRY_CODE_MESSAGE)
    private String countryCode = "+250";

    @NotBlank(message = AppConstants.PHONE_NUMBER_MESSAGE)
    @Pattern(regexp = AppConstants.PHONE_NUMBER_PATTERN, message = AppConstants.PHONE_NUMBER_MESSAGE)
    private String phoneNumber;

    @NotBlank(message = AppConstants.PASSWORD_MESSAGE)
    @Pattern(regexp = AppConstants.PASSWORD_PATTERN, message = AppConstants.PASSWORD_MESSAGE)
    private String password;

    private Role role = Role.ROLE_CUSTOMER;
}

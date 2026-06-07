/**
 * Request DTO for admin updates to an existing system user account.
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.enums.Role;
import rw.wasac.reg.billing.enums.UserStatus;

@Data
public class UserUpdateRequest {

    @NotBlank(message = AppConstants.FULL_NAME_REQUIRED_MESSAGE)
    @Size(min = 2, max = 150, message = AppConstants.FULL_NAME_SIZE_MESSAGE)
    private String fullName;

    @NotBlank(message = AppConstants.COUNTRY_CODE_REQUIRED_MESSAGE)
    @Pattern(regexp = AppConstants.COUNTRY_CODE_PATTERN, message = AppConstants.COUNTRY_CODE_MESSAGE)
    private String countryCode;

    @NotBlank(message = AppConstants.PHONE_NUMBER_MESSAGE)
    @Pattern(regexp = AppConstants.PHONE_NUMBER_PATTERN, message = AppConstants.PHONE_NUMBER_MESSAGE)
    private String phoneNumber;

    @NotNull(message = AppConstants.USER_STATUS_REQUIRED_MESSAGE)
    private UserStatus status;

    @NotNull(message = AppConstants.ROLE_REQUIRED_MESSAGE)
    private Role role;
}

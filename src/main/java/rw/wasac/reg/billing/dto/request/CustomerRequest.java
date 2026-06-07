/**
 * Request DTO for CustomerRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.enums.CustomerStatus;
import rw.wasac.reg.billing.validation.ValidRwandaNationalId;

@Data
public class CustomerRequest {

    @NotBlank(message = AppConstants.FULL_NAME_REQUIRED_MESSAGE)
    @Size(min = 2, max = 150, message = AppConstants.FULL_NAME_SIZE_MESSAGE)
    private String fullName;

    @NotBlank(message = AppConstants.NATIONAL_ID_REQUIRED_MESSAGE)
    @ValidRwandaNationalId
    private String nationalId;

    @NotBlank(message = AppConstants.EMAIL_REQUIRED_MESSAGE)
    @Email(message = AppConstants.EMAIL_MESSAGE)
    @Size(max = 150, message = AppConstants.EMAIL_SIZE_MESSAGE)
    private String email;

    @NotBlank(message = AppConstants.CUSTOMER_PHONE_MESSAGE)
    @Pattern(regexp = AppConstants.CUSTOMER_PHONE_PATTERN, message = AppConstants.CUSTOMER_PHONE_MESSAGE)
    private String phone;

    @NotBlank(message = AppConstants.ADDRESS_REQUIRED_MESSAGE)
    @Size(min = 5, max = 255, message = AppConstants.ADDRESS_SIZE_MESSAGE)
    private String address;

    @NotNull(message = AppConstants.CUSTOMER_STATUS_REQUIRED_MESSAGE)
    private CustomerStatus status = CustomerStatus.ACTIVE;
}

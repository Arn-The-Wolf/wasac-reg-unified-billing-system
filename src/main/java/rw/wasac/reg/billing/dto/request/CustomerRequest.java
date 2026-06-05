package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.enums.CustomerStatus;

@Data
public class CustomerRequest {

    @NotBlank
    @Size(min = 2, max = 150)
    private String fullName;

    @NotBlank
    @Pattern(regexp = AppConstants.NATIONAL_ID_PATTERN, message = AppConstants.NATIONAL_ID_MESSAGE)
    private String nationalId;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = AppConstants.CUSTOMER_PHONE_PATTERN, message = AppConstants.CUSTOMER_PHONE_MESSAGE)
    private String phone;

    @NotBlank
    @Size(min = 5, max = 255)
    private String address;

    private CustomerStatus status = CustomerStatus.ACTIVE;
}

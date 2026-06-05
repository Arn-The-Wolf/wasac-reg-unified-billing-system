/**
 * Response DTO for UserResponse API payloads.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.wasac.reg.billing.enums.Role;
import rw.wasac.reg.billing.enums.UserStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String countryCode;
    private String phoneNumber;
    private UserStatus status;
    private Role role;
    private Long customerId;
    private LocalDateTime createdAt;
}

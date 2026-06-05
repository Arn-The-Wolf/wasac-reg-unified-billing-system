/**
 * Response DTO for CustomerResponse API payloads.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.wasac.reg.billing.enums.CustomerStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String nationalId;
    private String email;
    private String phone;
    private String address;
    private CustomerStatus status;
    private LocalDateTime createdAt;
}

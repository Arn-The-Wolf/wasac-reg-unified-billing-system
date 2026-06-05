/**
 * Response DTO for FixedChargeResponse API payloads.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixedChargeResponse {
    private Long id;
    private String name;
    private BigDecimal amount;
    private LocalDate effectiveFrom;
    private LocalDateTime createdAt;
}

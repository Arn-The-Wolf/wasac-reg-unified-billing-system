/**
 * Response DTO for TaxResponse API payloads.
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
public class TaxResponse {
    private Long id;
    private String name;
    private BigDecimal percentage;
    private LocalDate effectiveFrom;
    private LocalDateTime createdAt;
}

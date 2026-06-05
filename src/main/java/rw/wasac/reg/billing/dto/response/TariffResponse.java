/**
 * Response DTO for TariffResponse API payloads.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.wasac.reg.billing.enums.MeterType;
import rw.wasac.reg.billing.enums.TariffType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffResponse {
    private Long id;
    private String name;
    private TariffType tariffType;
    private MeterType meterType;
    private Integer version;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private BigDecimal flatRate;
    private List<TariffTierResponse> tiers;
    private LocalDateTime createdAt;
}

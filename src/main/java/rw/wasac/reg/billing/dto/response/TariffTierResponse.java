package rw.wasac.reg.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffTierResponse {
    private Long id;
    private BigDecimal fromUnits;
    private BigDecimal toUnits;
    private BigDecimal ratePerUnit;
}

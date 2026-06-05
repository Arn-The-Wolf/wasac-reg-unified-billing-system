package rw.wasac.reg.billing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.enums.MeterType;
import rw.wasac.reg.billing.enums.TariffType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TariffRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotNull
    private TariffType tariffType;

    @NotNull
    private MeterType meterType;

    @NotNull
    private LocalDate effectiveFrom;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal flatRate;

    @Valid
    private List<TariffTierRequest> tiers = new ArrayList<>();
}

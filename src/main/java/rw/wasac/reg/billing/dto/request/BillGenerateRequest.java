package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillGenerateRequest {

    @NotNull
    private Long meterReadingId;
}

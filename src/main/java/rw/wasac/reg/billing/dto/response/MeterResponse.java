package rw.wasac.reg.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.wasac.reg.billing.enums.MeterStatus;
import rw.wasac.reg.billing.enums.MeterType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterResponse {
    private Long id;
    private String meterNumber;
    private MeterType type;
    private LocalDate installationDate;
    private MeterStatus status;
    private Long customerId;
    private String customerName;
    private LocalDateTime createdAt;
}

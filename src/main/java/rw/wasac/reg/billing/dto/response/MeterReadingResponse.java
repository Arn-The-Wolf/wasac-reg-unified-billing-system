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
public class MeterReadingResponse {
    private Long id;
    private Long meterId;
    private String meterNumber;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private LocalDate readingDate;
    private Integer billingMonth;
    private Integer billingYear;
    private LocalDateTime createdAt;
}

package rw.wasac.reg.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.wasac.reg.billing.enums.BillStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {
    private Long id;
    private String reference;
    private Long customerId;
    private String customerName;
    private Long meterId;
    private String meterNumber;
    private Integer billingMonth;
    private Integer billingYear;
    private BigDecimal consumption;
    private BigDecimal tariffAmount;
    private BigDecimal fixedChargeAmount;
    private BigDecimal taxAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    private BillStatus status;
    private LocalDateTime createdAt;
}

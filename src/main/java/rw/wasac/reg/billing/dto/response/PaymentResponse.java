/**
 * Response DTO for PaymentResponse API payloads.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rw.wasac.reg.billing.enums.PaymentMethod;
import rw.wasac.reg.billing.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long billId;
    private String billReference;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private LocalDate paymentDate;
    private PaymentStatus status;
    private String notes;
    private LocalDateTime createdAt;
}

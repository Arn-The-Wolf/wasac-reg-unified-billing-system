/**
 * Request DTO for PaymentRequest with validation constraints.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentRequest {

    @NotNull(message = AppConstants.BILL_ID_REQUIRED_MESSAGE)
    private Long billId;

    @NotNull(message = AppConstants.AMOUNT_REQUIRED_MESSAGE)
    @DecimalMin(value = "0.01", message = AppConstants.AMOUNT_MIN_MESSAGE)
    private BigDecimal amount;

    @NotNull(message = AppConstants.PAYMENT_METHOD_REQUIRED_MESSAGE)
    private PaymentMethod paymentMethod;

    @NotNull(message = AppConstants.DATE_REQUIRED_MESSAGE)
    @PastOrPresent(message = AppConstants.PAYMENT_DATE_MESSAGE)
    private LocalDate paymentDate;

    @Size(max = 500, message = AppConstants.NOTES_SIZE_MESSAGE)
    private String notes;
}

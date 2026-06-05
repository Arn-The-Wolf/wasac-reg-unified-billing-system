/**
 * Response DTO for NotificationResponse API payloads.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String message;
    private Integer month;
    private Integer year;
    private String monthYear;
    private LocalDateTime sentAt;
}

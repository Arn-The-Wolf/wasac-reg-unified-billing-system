package rw.wasac.reg.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import rw.wasac.reg.billing.enums.MeterStatus;
import rw.wasac.reg.billing.enums.MeterType;

import java.time.LocalDate;

@Data
public class MeterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Meter number must be uppercase letters, digits, or hyphens")
    private String meterNumber;

    @NotNull
    private MeterType type;

    @NotNull
    @PastOrPresent(message = "Installation date cannot be in the future")
    private LocalDate installationDate;

    private MeterStatus status = MeterStatus.ACTIVE;

    @NotNull
    private Long customerId;
}

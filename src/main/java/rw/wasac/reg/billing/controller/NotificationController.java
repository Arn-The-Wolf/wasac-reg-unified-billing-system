/**
 * REST controller exposing NotificationController endpoints for the WASAC/REG billing system.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.dto.response.NotificationResponse;
import rw.wasac.reg.billing.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Notification", description = "Customer notification APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notificationService.getAll()));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved",
                notificationService.getByCustomerId(customerId)));
    }
}

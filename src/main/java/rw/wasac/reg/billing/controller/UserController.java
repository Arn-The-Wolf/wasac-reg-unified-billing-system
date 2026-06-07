/**
 * REST controller for admin user management (Task 1 — ROLE_ADMIN manages users).
 */
package rw.wasac.reg.billing.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.dto.request.UserUpdateRequest;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.dto.response.UserResponse;
import rw.wasac.reg.billing.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "User Management", description = "Admin-only user account management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", userService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long id) {
        return ResponseEntity.ok(ApiResponse.success("User retrieved", userService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated", userService.update(id, request)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activate(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long id) {
        return ResponseEntity.ok(ApiResponse.success("User activated", userService.activate(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivate(
            @PathVariable @Positive(message = AppConstants.ID_POSITIVE_MESSAGE) Long id) {
        return ResponseEntity.ok(ApiResponse.success("User deactivated", userService.deactivate(id)));
    }
}

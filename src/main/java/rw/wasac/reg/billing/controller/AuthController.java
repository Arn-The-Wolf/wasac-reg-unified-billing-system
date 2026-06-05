/**
 * REST controller exposing AuthController endpoints for the WASAC/REG billing system.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.wasac.reg.billing.dto.request.LoginRequest;
import rw.wasac.reg.billing.dto.request.OtpRequest;
import rw.wasac.reg.billing.dto.request.OtpVerifyRequest;
import rw.wasac.reg.billing.dto.request.SignupRequest;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.dto.response.AuthResponse;
import rw.wasac.reg.billing.dto.response.OtpPendingResponse;
import rw.wasac.reg.billing.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Signup", description = "Customer self-registration only (ROLE_CUSTOMER). Staff accounts are created by an administrator.")
    public ResponseEntity<ApiResponse<OtpPendingResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/signup/verify-otp")
    @Operation(summary = "Verify signup OTP", description = "Verify OTP and activate account")
    public ResponseEntity<ApiResponse<AuthResponse>> verifySignupOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Account activated", authService.verifySignupOtp(request)));
    }

    @PostMapping("/signup/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendSignupOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.resendSignupOtp(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Validate credentials and send login OTP")
    public ResponseEntity<ApiResponse<OtpPendingResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/login/verify-otp")
    @Operation(summary = "Verify login OTP", description = "Complete login and receive JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLoginOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.verifyLoginOtp(request)));
    }

    @PostMapping("/login/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendLoginOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.resendLoginOtp(request));
    }
}

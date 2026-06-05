/**
 * Service contract defining AuthService operations.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.request.LoginRequest;
import rw.wasac.reg.billing.dto.request.OtpRequest;
import rw.wasac.reg.billing.dto.request.OtpVerifyRequest;
import rw.wasac.reg.billing.dto.request.SignupRequest;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.dto.response.AuthResponse;
import rw.wasac.reg.billing.dto.response.OtpPendingResponse;

public interface AuthService {
    ApiResponse<OtpPendingResponse> signup(SignupRequest request);
    AuthResponse verifySignupOtp(OtpVerifyRequest request);
    ApiResponse<String> resendSignupOtp(OtpRequest request);
    ApiResponse<OtpPendingResponse> login(LoginRequest request);
    AuthResponse verifyLoginOtp(OtpVerifyRequest request);
    ApiResponse<String> resendLoginOtp(OtpRequest request);
}

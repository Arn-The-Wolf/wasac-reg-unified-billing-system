package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.request.OtpRequest;
import rw.wasac.reg.billing.dto.request.OtpVerifyRequest;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.enums.OtpPurpose;

public interface OtpService {
    ApiResponse<String> sendOtp(String email, OtpPurpose purpose, String recipientName);
    void verifyOtpCode(OtpVerifyRequest request, OtpPurpose purpose);
    void consumeVerifiedOtp(String email, OtpPurpose purpose, String code);
    ApiResponse<String> resendOtp(OtpRequest request, OtpPurpose purpose);
    void cleanupExpiredOtps();
}

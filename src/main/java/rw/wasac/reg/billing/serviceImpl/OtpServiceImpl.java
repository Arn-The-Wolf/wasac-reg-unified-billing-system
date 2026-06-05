/**
 * Service implementation providing Otp business logic.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.OtpRequest;
import rw.wasac.reg.billing.dto.request.OtpVerifyRequest;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.entity.Otp;
import rw.wasac.reg.billing.enums.OtpPurpose;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.repository.OtpRepository;
import rw.wasac.reg.billing.service.EmailService;
import rw.wasac.reg.billing.service.OtpService;
import rw.wasac.reg.billing.utils.EmailTemplateBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public OtpServiceImpl(OtpRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public ApiResponse<String> sendOtp(String email, OtpPurpose purpose, String recipientName) {
        otpRepository.deleteByEmailAndPurpose(email, purpose);

        String otpCode = generateOtpCode();
        Otp otp = Otp.builder()
                .email(email)
                .purpose(purpose)
                .code(otpCode)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .attempts(0)
                .build();
        otpRepository.save(otp);

        String subject = "WASAC — Your Verification Code";
        String plainBody = String.format(
                "Dear %s, Your verification code is: %s. Expires in %d minutes.",
                recipientName, otpCode, OTP_EXPIRY_MINUTES);
        String htmlBody = EmailTemplateBuilder.buildOtpEmail(recipientName, otpCode, OTP_EXPIRY_MINUTES);
        emailService.sendHtmlEmail(email, subject, htmlBody, plainBody);

        return ApiResponse.<String>builder()
                .success(true)
                .message("OTP sent successfully to " + maskEmail(email))
                .data("OTP expires in " + OTP_EXPIRY_MINUTES + " minutes")
                .build();
    }

    @Override
    @Transactional
    public void verifyOtpCode(OtpVerifyRequest request, OtpPurpose purpose) {
        Otp otp = otpRepository.findByEmailAndPurposeAndVerifiedFalse(request.getEmail(), purpose)
                .orElseThrow(() -> new BadRequestException("No active OTP found for this email"));

        if (otp.isExpired()) {
            otpRepository.delete(otp);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        if (otp.isMaxAttemptsReached()) {
            otpRepository.delete(otp);
            throw new BadRequestException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        if (!otp.getCode().equals(request.getCode())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            int remaining = MAX_ATTEMPTS - otp.getAttempts();
            if (remaining > 0) {
                throw new BadRequestException("Invalid OTP code. " + remaining + " attempt(s) remaining.");
            }
            otpRepository.delete(otp);
            throw new BadRequestException("Invalid OTP code. Maximum attempts exceeded. Please request a new OTP.");
        }

        otp.setVerified(true);
        otpRepository.save(otp);
    }

    @Override
    @Transactional
    public ApiResponse<String> resendOtp(OtpRequest request, OtpPurpose purpose) {
        return sendOtp(request.getEmail(), purpose, request.getEmail());
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void consumeVerifiedOtp(String email, OtpPurpose purpose, String code) {
        Otp otp = requireVerifiedOtp(email, purpose, code);
        otpRepository.delete(otp);
    }

    private Otp requireVerifiedOtp(String email, OtpPurpose purpose, String code) {
        Otp otp = otpRepository.findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new BadRequestException("No OTP found. Complete verification first."));

        if (!Boolean.TRUE.equals(otp.getVerified())) {
            throw new BadRequestException("OTP not verified. Verify the code first.");
        }

        if (otp.isExpired()) {
            otpRepository.delete(otp);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        if (!otp.getCode().equals(code)) {
            throw new BadRequestException("Invalid OTP code.");
        }

        return otp;
    }

    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String local = parts[0];
        if (local.length() <= 2) {
            return "**@" + parts[1];
        }
        StringBuilder masked = new StringBuilder();
        masked.append(local.charAt(0));
        for (int i = 0; i < local.length() - 2; i++) {
            masked.append('*');
        }
        masked.append(local.charAt(local.length() - 1)).append('@').append(parts[1]);
        return masked.toString();
    }
}

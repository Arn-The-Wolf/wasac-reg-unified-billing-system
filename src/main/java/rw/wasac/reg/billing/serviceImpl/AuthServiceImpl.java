/**
 * Service implementation providing Auth business logic.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.LoginRequest;
import rw.wasac.reg.billing.dto.request.OtpRequest;
import rw.wasac.reg.billing.dto.request.OtpVerifyRequest;
import rw.wasac.reg.billing.dto.request.SignupRequest;
import rw.wasac.reg.billing.dto.response.ApiResponse;
import rw.wasac.reg.billing.dto.response.AuthResponse;
import rw.wasac.reg.billing.dto.response.OtpPendingResponse;
import rw.wasac.reg.billing.entity.User;
import rw.wasac.reg.billing.enums.OtpPurpose;
import rw.wasac.reg.billing.enums.Role;
import rw.wasac.reg.billing.enums.UserStatus;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.exception.DuplicateResourceException;
import rw.wasac.reg.billing.exception.ResourceNotFoundException;
import rw.wasac.reg.billing.repository.UserRepository;
import rw.wasac.reg.billing.security.JwtTokenProvider;
import rw.wasac.reg.billing.service.AuthService;
import rw.wasac.reg.billing.service.EmailService;
import rw.wasac.reg.billing.service.OtpService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;
    private final EmailService emailService;

    @Override
    @Transactional
    public ApiResponse<OtpPendingResponse> signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_CUSTOMER;
        if (role == Role.ROLE_ADMIN) {
            throw new BadRequestException("Cannot self-register as ADMIN");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .countryCode(request.getCountryCode() != null ? request.getCountryCode() : "+250")
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.PENDING_VERIFICATION)
                .role(role)
                .build();
        userRepository.save(user);

        otpService.sendOtp(user.getEmail(), OtpPurpose.SIGNUP, user.getFullName());
        log.info("Signup pending OTP verification: {}", user.getEmail());

        return ApiResponse.success("Signup initiated. Verify OTP sent to your email.", OtpPendingResponse.builder()
                .requiresOtp(true)
                .email(user.getEmail())
                .message("Check your email for the verification code")
                .build());
    }

    @Override
    @Transactional
    public AuthResponse verifySignupOtp(OtpVerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("Account is already active");
        }

        otpService.verifyOtpCode(request, OtpPurpose.SIGNUP);
        otpService.consumeVerifiedOtp(request.getEmail(), OtpPurpose.SIGNUP, request.getCode());

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        String plainWelcome = String.format(
                "Dear %s, your WASAC/REG billing account has been activated.", user.getFullName());
        String htmlWelcome = rw.wasac.reg.billing.utils.EmailTemplateBuilder.buildWelcomeEmail(user.getFullName());
        emailService.sendHtmlEmail(user.getEmail(), "WASAC — Welcome to Utility Billing", htmlWelcome, plainWelcome);

        return buildAuthResponse(user);
    }

    @Override
    public ApiResponse<String> resendSignupOtp(OtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new BadRequestException("No pending signup for this email");
        }
        return otpService.resendOtp(request, OtpPurpose.SIGNUP);
    }

    @Override
    @Transactional
    public ApiResponse<OtpPendingResponse> login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        otpService.sendOtp(user.getEmail(), OtpPurpose.LOGIN, user.getFullName());

        return ApiResponse.success("Credentials valid. OTP sent to your email.", OtpPendingResponse.builder()
                .requiresOtp(true)
                .email(user.getEmail())
                .message("Enter the OTP sent to your email to complete login")
                .build());
    }

    @Override
    @Transactional
    public AuthResponse verifyLoginOtp(OtpVerifyRequest request) {
        otpService.verifyOtpCode(request, OtpPurpose.LOGIN);
        otpService.consumeVerifiedOtp(request.getEmail(), OtpPurpose.LOGIN, request.getCode());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return buildAuthResponse(user);
    }

    @Override
    public ApiResponse<String> resendLoginOtp(OtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }
        return otpService.resendOtp(request, OtpPurpose.LOGIN);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(user.getRole().name()))
                .build();

        return AuthResponse.builder()
                .token(jwtTokenProvider.generateToken(userDetails))
                .type("Bearer")
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}

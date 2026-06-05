package rw.wasac.reg.billing.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import rw.wasac.reg.billing.dto.request.LoginRequest;
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
import rw.wasac.reg.billing.repository.UserRepository;
import rw.wasac.reg.billing.security.JwtTokenProvider;
import rw.wasac.reg.billing.serviceImpl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private OtpService otpService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void signup_createsPendingUserAndSendsOtp() {
        SignupRequest request = new SignupRequest();
        request.setFullName("Test User");
        request.setEmail("test@wasac.rw");
        request.setPhoneNumber("788123456");
        request.setPassword("Test@1234");

        when(userRepository.existsByEmail("test@wasac.rw")).thenReturn(false);
        when(passwordEncoder.encode("Test@1234")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        ApiResponse<OtpPendingResponse> response = authService.signup(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().isRequiresOtp()).isTrue();
        verify(otpService).sendOtp(eq("test@wasac.rw"), eq(OtpPurpose.SIGNUP), eq("Test User"));
    }

    @Test
    void signup_rejectsDuplicateEmail() {
        SignupRequest request = new SignupRequest();
        request.setEmail("dup@wasac.rw");
        when(userRepository.existsByEmail("dup@wasac.rw")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void signup_rejectsAdminSelfRegistration() {
        SignupRequest request = new SignupRequest();
        request.setEmail("admin@wasac.rw");
        request.setRole(Role.ROLE_ADMIN);
        when(userRepository.existsByEmail("admin@wasac.rw")).thenReturn(false);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void verifySignupOtp_activatesUserAndReturnsToken() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setEmail("test@wasac.rw");
        request.setCode("123456");

        User user = User.builder()
                .id(1L)
                .email("test@wasac.rw")
                .fullName("Test User")
                .password("encoded")
                .status(UserStatus.PENDING_VERIFICATION)
                .role(Role.ROLE_CUSTOMER)
                .build();

        when(userRepository.findByEmail("test@wasac.rw")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.verifySignupOtp(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(otpService).verifyOtpCode(request, OtpPurpose.SIGNUP);
        verify(otpService).consumeVerifiedOtp("test@wasac.rw", OtpPurpose.SIGNUP, "123456");
        verify(emailService).sendHtmlEmail(eq("test@wasac.rw"), eq("WASAC — Welcome to Utility Billing"), any(String.class), any(String.class));
    }

    @Test
    void login_sendsOtpAfterAuthentication() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@wasac.rw");
        request.setPassword("Test@1234");

        User user = User.builder()
                .email("user@wasac.rw")
                .fullName("Active User")
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_CUSTOMER)
                .password("encoded")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("user@wasac.rw")).thenReturn(Optional.of(user));

        ApiResponse<OtpPendingResponse> response = authService.login(request);

        assertThat(response.getData().isRequiresOtp()).isTrue();
        verify(otpService).sendOtp("user@wasac.rw", OtpPurpose.LOGIN, "Active User");
    }
}

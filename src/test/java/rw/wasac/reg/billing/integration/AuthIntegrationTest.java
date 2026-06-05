package rw.wasac.reg.billing.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.OtpVerifyRequest;
import rw.wasac.reg.billing.dto.request.SignupRequest;
import rw.wasac.reg.billing.enums.OtpPurpose;
import rw.wasac.reg.billing.repository.OtpRepository;
import rw.wasac.reg.billing.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void signupAndVerifyOtp_activatesAccount() throws Exception {
        SignupRequest signup = new SignupRequest();
        signup.setFullName("Integration User");
        signup.setEmail("integration@wasac.rw");
        signup.setCountryCode("+250");
        signup.setPhoneNumber("788999888");
        signup.setPassword("Integr@123");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requiresOtp").value(true));

        String code = otpRepository.findByEmailAndPurposeAndVerifiedFalse(
                "integration@wasac.rw", OtpPurpose.SIGNUP).orElseThrow().getCode();

        OtpVerifyRequest verify = new OtpVerifyRequest();
        verify.setEmail("integration@wasac.rw");
        verify.setCode(code);

        mockMvc.perform(post("/api/v1/auth/signup/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verify)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());

        assertThat(userRepository.findByEmail("integration@wasac.rw").orElseThrow().getStatus().name())
                .isEqualTo("ACTIVE");
    }

    @Test
    void signupValidation_rejectsWeakPassword() throws Exception {
        SignupRequest signup = new SignupRequest();
        signup.setFullName("Bad Password");
        signup.setEmail("weak@wasac.rw");
        signup.setPhoneNumber("788111222");
        signup.setPassword("short");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

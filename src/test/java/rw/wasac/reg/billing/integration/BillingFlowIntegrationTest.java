package rw.wasac.reg.billing.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.BillGenerateRequest;
import rw.wasac.reg.billing.dto.request.CustomerRequest;
import rw.wasac.reg.billing.dto.request.MeterReadingRequest;
import rw.wasac.reg.billing.dto.request.MeterRequest;
import rw.wasac.reg.billing.dto.request.PaymentRequest;
import rw.wasac.reg.billing.entity.*;
import rw.wasac.reg.billing.enums.*;
import rw.wasac.reg.billing.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BillingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TariffRepository tariffRepository;
    @Autowired
    private FixedChargeRepository fixedChargeRepository;
    @Autowired
    private TaxRepository taxRepository;
    @Autowired
    private PenaltyRepository penaltyRepository;
    @Autowired
    private MeterReadingRepository meterReadingRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    private Long meterReadingId;
    private Long billId;

    @BeforeEach
    void seedConfig() {
        tariffRepository.save(Tariff.builder()
                .name("Water Test Flat")
                .tariffType(TariffType.FLAT)
                .meterType(MeterType.WATER)
                .version(1)
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .flatRate(new BigDecimal("100.00"))
                .build());
        fixedChargeRepository.save(FixedCharge.builder()
                .name("Service Fee")
                .amount(new BigDecimal("50.00"))
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .build());
        taxRepository.save(Tax.builder()
                .name("VAT")
                .percentage(new BigDecimal("18.00"))
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .build());
        penaltyRepository.save(Penalty.builder()
                .name("Late Fee")
                .percentage(new BigDecimal("5.00"))
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .build());

        userRepository.save(User.builder()
                .fullName("Operator")
                .email("operator-test@wasac.rw")
                .countryCode("+250")
                .phoneNumber("788000001")
                .password(passwordEncoder.encode("Operator@123"))
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_OPERATOR)
                .build());
    }

    @Test
    @WithMockUser(username = "operator-test@wasac.rw", roles = "OPERATOR")
    void endToEnd_billGenerationAndPayment() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setFullName("Flow Customer");
        customerRequest.setNationalId("1199880077665500");
        customerRequest.setEmail("flow.customer@wasac.rw");
        customerRequest.setPhone("+250788123456");
        customerRequest.setAddress("Kigali");

        String customerJson = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long customerId = objectMapper.readTree(customerJson).path("data").path("id").asLong();

        MeterRequest meterRequest = new MeterRequest();
        meterRequest.setMeterNumber("WTR-FLOW-001");
        meterRequest.setType(MeterType.WATER);
        meterRequest.setInstallationDate(LocalDate.of(2024, 1, 1));
        meterRequest.setCustomerId(customerId);

        String meterJson = mockMvc.perform(post("/api/v1/meters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meterRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long meterId = objectMapper.readTree(meterJson).path("data").path("id").asLong();

        MeterReadingRequest readingRequest = new MeterReadingRequest();
        readingRequest.setMeterId(meterId);
        readingRequest.setPreviousReading(new BigDecimal("0"));
        readingRequest.setCurrentReading(new BigDecimal("10"));
        readingRequest.setReadingDate(LocalDate.of(2025, 6, 1));

        String readingJson = mockMvc.perform(post("/api/v1/meter-readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(readingRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        meterReadingId = objectMapper.readTree(readingJson).path("data").path("id").asLong();

        BillGenerateRequest billRequest = new BillGenerateRequest();
        billRequest.setMeterReadingId(meterReadingId);

        String billJson = mockMvc.perform(post("/api/v1/bills/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.totalAmount").exists())
                .andReturn().getResponse().getContentAsString();

        billId = objectMapper.readTree(billJson).path("data").path("id").asLong();

        assertThat(objectMapper.readTree(billJson).path("data").path("status").asText()).isEqualTo("PENDING");

        BigDecimal billBalance = new BigDecimal(
                objectMapper.readTree(billJson).path("data").path("balance").asText());

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setBillId(billId);
        paymentRequest.setAmount(billBalance);
        paymentRequest.setPaymentMethod(rw.wasac.reg.billing.enums.PaymentMethod.MOBILE_MONEY);
        paymentRequest.setPaymentDate(java.time.LocalDate.of(2025, 6, 5));

        String paymentJson = mockMvc.perform(post("/api/v1/payments")
                        .with(user("customer@wasac.rw").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long paymentId = objectMapper.readTree(paymentJson).path("data").path("id").asLong();

        mockMvc.perform(patch("/api/v1/payments/" + paymentId + "/approve")
                        .with(user("finance@wasac.rw").roles("FINANCE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
}

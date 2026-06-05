/**
 * Spring configuration component: DataLoader.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import rw.wasac.reg.billing.entity.*;
import rw.wasac.reg.billing.enums.*;
import rw.wasac.reg.billing.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final MeterRepository meterRepository;
    private final TariffRepository tariffRepository;
    private final FixedChargeRepository fixedChargeRepository;
    private final TaxRepository taxRepository;
    private final PenaltyRepository penaltyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        loadCustomersAndMeters();
        loadUsers();
        loadTariffConfig();
    }

    private void loadCustomersAndMeters() {
        if (customerRepository.count() > 0) {
            return;
        }

        log.info("Seeding sample customer and meters...");

        Customer customer = customerRepository.save(Customer.builder()
                .fullName("Jean Uwimana")
                .nationalId("1199880077665544")
                .email("customer.demo@wasac.rw")
                .phone("0788123456")
                .address("Kigali, Gasabo, Remera")
                .status(CustomerStatus.ACTIVE)
                .build());

        meterRepository.save(Meter.builder()
                .meterNumber("WTR-001-2024")
                .type(MeterType.WATER)
                .installationDate(LocalDate.of(2024, 1, 15))
                .status(MeterStatus.ACTIVE)
                .customer(customer)
                .build());

        meterRepository.save(Meter.builder()
                .meterNumber("ELC-001-2024")
                .type(MeterType.ELECTRICITY)
                .installationDate(LocalDate.of(2024, 2, 1))
                .status(MeterStatus.ACTIVE)
                .customer(customer)
                .build());
    }

    private void loadUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Seeding default users...");

        Customer customer = customerRepository.findByEmail("customer.demo@wasac.rw").orElse(null);

        userRepository.save(User.builder()
                .fullName("System Admin")
                .email("ruyangearnold@gmail.com")
                .countryCode("+250")
                .phoneNumber("0781000001")
                .password(passwordEncoder.encode("Admin@123"))
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_ADMIN)
                .build());

        userRepository.save(User.builder()
                .fullName("Field Operator")
                .email("operator@wasac.rw")
                .countryCode("+250")
                .phoneNumber("0781000002")
                .password(passwordEncoder.encode("Operator@123"))
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_OPERATOR)
                .build());

        userRepository.save(User.builder()
                .fullName("Field Inspector")
                .email("inspector@wasac.rw")
                .countryCode("+250")
                .phoneNumber("0781000005")
                .password(passwordEncoder.encode("Inspector@123"))
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_INSPECTOR)
                .build());

        userRepository.save(User.builder()
                .fullName("Finance Officer")
                .email("finance@wasac.rw")
                .countryCode("+250")
                .phoneNumber("0781000003")
                .password(passwordEncoder.encode("Finance@123"))
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_FINANCE)
                .build());

        userRepository.save(User.builder()
                .fullName("Jean Uwimana")
                .email("customer@wasac.rw")
                .countryCode("+250")
                .phoneNumber("0781000004")
                .password(passwordEncoder.encode("Customer@123"))
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_CUSTOMER)
                .customer(customer)
                .build());

        log.info("Default credentials: ruyangearnold@gmail.com/Admin@123, operator@wasac.rw/Operator@123, "
                + "inspector@wasac.rw/Inspector@123, finance@wasac.rw/Finance@123, customer@wasac.rw/Customer@123");
    }

    private void loadTariffConfig() {
        if (tariffRepository.count() > 0) {
            return;
        }

        log.info("Seeding tariff configuration...");

        tariffRepository.save(Tariff.builder()
                .name("Water Flat Rate v1")
                .tariffType(TariffType.FLAT)
                .meterType(MeterType.WATER)
                .version(1)
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .flatRate(new BigDecimal("350.00"))
                .build());

        tariffRepository.save(Tariff.builder()
                .name("Electricity Flat Rate v1")
                .tariffType(TariffType.FLAT)
                .meterType(MeterType.ELECTRICITY)
                .version(1)
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .flatRate(new BigDecimal("120.00"))
                .build());
        Tariff waterTiered = Tariff.builder()
                .name("Water Tiered Rate v1")
                .tariffType(TariffType.TIER)
                .meterType(MeterType.WATER)
                .version(2)
                .effectiveFrom(LocalDate.of(2025, 1, 1))
                .build();
        waterTiered.getTiers().add(TariffTier.builder()
                .tariff(waterTiered)
                .fromUnits(new BigDecimal("0"))
                .toUnits(new BigDecimal("10"))
                .ratePerUnit(new BigDecimal("280.00"))
                .build());
        waterTiered.getTiers().add(TariffTier.builder()
                .tariff(waterTiered)
                .fromUnits(new BigDecimal("10"))
                .toUnits(null)
                .ratePerUnit(new BigDecimal("420.00"))
                .build());
        tariffRepository.save(waterTiered);


        fixedChargeRepository.save(FixedCharge.builder()
                .name("Monthly Service Fee")
                .amount(new BigDecimal("500.00"))
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .build());

        taxRepository.save(Tax.builder()
                .name("VAT")
                .percentage(new BigDecimal("18.00"))
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .build());

        penaltyRepository.save(Penalty.builder()
                .name("Late Payment Penalty")
                .percentage(new BigDecimal("5.00"))
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .build());
    }
}

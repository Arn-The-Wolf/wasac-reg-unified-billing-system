package rw.wasac.reg.billing.utils;

import org.junit.jupiter.api.Test;
import rw.wasac.reg.billing.entity.Tariff;
import rw.wasac.reg.billing.enums.MeterType;
import rw.wasac.reg.billing.enums.TariffType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BillingCalculatorTest {

    private final BillingCalculator calculator = new BillingCalculator();

    @Test
    void calculateTariffAmount_flatRate() {
        Tariff tariff = Tariff.builder()
                .name("Flat")
                .tariffType(TariffType.FLAT)
                .meterType(MeterType.WATER)
                .flatRate(new BigDecimal("350.00"))
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .build();

        BigDecimal amount = calculator.calculateTariffAmount(tariff, new BigDecimal("10"));
        assertThat(amount).isEqualByComparingTo("3500.00");
    }

    @Test
    void calculateTax_appliesPercentage() {
        BigDecimal tax = calculator.calculateTax(new BigDecimal("1000"), new BigDecimal("18"));
        assertThat(tax).isEqualByComparingTo("180.00");
    }
}

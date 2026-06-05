package rw.wasac.reg.billing.utils;

import org.springframework.stereotype.Component;
import rw.wasac.reg.billing.entity.Tariff;
import rw.wasac.reg.billing.entity.TariffTier;
import rw.wasac.reg.billing.enums.TariffType;
import rw.wasac.reg.billing.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BillingCalculator {

    public BigDecimal calculateTariffAmount(Tariff tariff, BigDecimal consumption) {
        if (tariff.getTariffType() == TariffType.FLAT) {
            if (tariff.getFlatRate() == null) {
                throw new BadRequestException("Flat tariff requires flatRate");
            }
            return consumption.multiply(tariff.getFlatRate()).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal remaining = consumption;
        BigDecimal total = BigDecimal.ZERO;

        for (TariffTier tier : tariff.getTiers()) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal tierCapacity = tier.getToUnits() != null
                    ? tier.getToUnits().subtract(tier.getFromUnits())
                    : remaining;
            if (tierCapacity.compareTo(BigDecimal.ZERO) <= 0) {
                tierCapacity = remaining;
            }
            BigDecimal unitsInTier = remaining.min(tierCapacity);
            total = total.add(unitsInTier.multiply(tier.getRatePerUnit()));
            remaining = remaining.subtract(unitsInTier);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0 && !tariff.getTiers().isEmpty()) {
            TariffTier lastTier = tariff.getTiers().get(tariff.getTiers().size() - 1);
            total = total.add(remaining.multiply(lastTier.getRatePerUnit()));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTax(BigDecimal baseAmount, BigDecimal taxPercentage) {
        return baseAmount.multiply(taxPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculatePenalty(BigDecimal baseAmount, BigDecimal penaltyPercentage) {
        return baseAmount.multiply(penaltyPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}

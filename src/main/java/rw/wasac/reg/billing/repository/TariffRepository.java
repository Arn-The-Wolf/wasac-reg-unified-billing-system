/**
 * Spring Data JPA repository for Tariff persistence.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.wasac.reg.billing.entity.Tariff;
import rw.wasac.reg.billing.enums.MeterType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {

    @Query("SELECT t FROM Tariff t WHERE t.meterType = :meterType AND t.effectiveFrom <= :periodStart "
            + "AND (t.effectiveTo IS NULL OR t.effectiveTo >= :periodStart) "
            + "ORDER BY t.effectiveFrom DESC, t.version DESC")
    List<Tariff> findApplicableTariffs(@Param("meterType") MeterType meterType, @Param("periodStart") LocalDate periodStart);

    default Optional<Tariff> findActiveTariffForPeriod(MeterType meterType, LocalDate periodStart) {
        return findApplicableTariffs(meterType, periodStart).stream().findFirst();
    }

    Optional<Tariff> findTopByMeterTypeOrderByVersionDesc(MeterType meterType);

    @Query("SELECT DISTINCT t FROM Tariff t LEFT JOIN FETCH t.tiers")
    List<Tariff> findAllWithTiers();

    @Query("SELECT t FROM Tariff t LEFT JOIN FETCH t.tiers WHERE t.id = :id")
    Optional<Tariff> findByIdWithTiers(@Param("id") Long id);
}

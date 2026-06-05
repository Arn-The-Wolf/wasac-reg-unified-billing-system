/**
 * Spring Data JPA repository for FixedCharge persistence.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.wasac.reg.billing.entity.FixedCharge;

import java.time.LocalDate;
import java.util.Optional;

public interface FixedChargeRepository extends JpaRepository<FixedCharge, Long> {

    @Query("SELECT f FROM FixedCharge f WHERE f.effectiveFrom <= :periodStart ORDER BY f.effectiveFrom DESC")
    java.util.List<FixedCharge> findApplicable(@Param("periodStart") LocalDate periodStart);

    default Optional<FixedCharge> findActiveForPeriod(LocalDate periodStart) {
        return findApplicable(periodStart).stream().findFirst();
    }
}

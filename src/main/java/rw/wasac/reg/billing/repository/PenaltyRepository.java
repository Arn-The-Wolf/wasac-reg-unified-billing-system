/**
 * Spring Data JPA repository for Penalty persistence.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.wasac.reg.billing.entity.Penalty;

import java.time.LocalDate;
import java.util.Optional;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    @Query("SELECT p FROM Penalty p WHERE p.effectiveFrom <= :periodStart ORDER BY p.effectiveFrom DESC")
    java.util.List<Penalty> findApplicable(@Param("periodStart") LocalDate periodStart);

    default Optional<Penalty> findActiveForPeriod(LocalDate periodStart) {
        return findApplicable(periodStart).stream().findFirst();
    }
}

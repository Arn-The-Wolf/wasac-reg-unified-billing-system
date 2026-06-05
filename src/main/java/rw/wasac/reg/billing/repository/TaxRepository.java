package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.wasac.reg.billing.entity.Tax;

import java.time.LocalDate;
import java.util.Optional;

public interface TaxRepository extends JpaRepository<Tax, Long> {

    @Query("SELECT t FROM Tax t WHERE t.effectiveFrom <= :periodStart ORDER BY t.effectiveFrom DESC")
    java.util.List<Tax> findApplicable(@Param("periodStart") LocalDate periodStart);

    default Optional<Tax> findActiveForPeriod(LocalDate periodStart) {
        return findApplicable(periodStart).stream().findFirst();
    }
}

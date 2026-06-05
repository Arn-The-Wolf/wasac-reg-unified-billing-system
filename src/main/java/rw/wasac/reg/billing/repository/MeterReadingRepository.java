/**
 * Spring Data JPA repository for MeterReading persistence.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.wasac.reg.billing.entity.MeterReading;

import java.util.List;
import java.util.Optional;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    Optional<MeterReading> findByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer month, Integer year);
    List<MeterReading> findByMeterIdOrderByReadingDateDesc(Long meterId);
    boolean existsByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer month, Integer year);

    java.util.Optional<MeterReading> findFirstByMeterIdOrderByReadingDateDesc(Long meterId);
}

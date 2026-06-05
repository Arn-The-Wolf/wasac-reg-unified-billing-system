/**
 * Spring Data JPA repository for Bill persistence.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.wasac.reg.billing.entity.Bill;
import rw.wasac.reg.billing.enums.BillStatus;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByReference(String reference);
    Optional<Bill> findByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer month, Integer year);
    List<Bill> findByCustomerId(Long customerId);
    List<Bill> findByStatus(BillStatus status);
    boolean existsByMeterReadingId(Long meterReadingId);

    @Query("SELECT b FROM Bill b JOIN FETCH b.customer JOIN FETCH b.meter WHERE b.id = :id")
    Optional<Bill> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b FROM Bill b JOIN FETCH b.customer JOIN FETCH b.meter "
            + "WHERE b.billingMonth = :month AND b.billingYear = :year ORDER BY b.reference")
    List<Bill> findByPeriodWithDetails(@Param("month") int month, @Param("year") int year);
}

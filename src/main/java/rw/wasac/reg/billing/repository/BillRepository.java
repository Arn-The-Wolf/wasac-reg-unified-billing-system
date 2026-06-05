package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.wasac.reg.billing.entity.Bill;
import rw.wasac.reg.billing.enums.BillStatus;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByReference(String reference);
    Optional<Bill> findByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer month, Integer year);
    List<Bill> findByCustomerId(Long customerId);
    List<Bill> findByStatus(BillStatus status);
}

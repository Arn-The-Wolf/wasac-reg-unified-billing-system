package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.wasac.reg.billing.entity.Payment;
import rw.wasac.reg.billing.enums.PaymentStatus;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBillId(Long billId);
    List<Payment> findByStatus(PaymentStatus status);
}

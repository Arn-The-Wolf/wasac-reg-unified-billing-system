package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.wasac.reg.billing.entity.Meter;
import rw.wasac.reg.billing.enums.MeterType;

import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    Optional<Meter> findByMeterNumber(String meterNumber);
    boolean existsByMeterNumber(String meterNumber);
    List<Meter> findByCustomerId(Long customerId);
    List<Meter> findByCustomerIdAndType(Long customerId, MeterType type);

    @Query("SELECT DISTINCT m FROM Meter m LEFT JOIN FETCH m.customer")
    List<Meter> findAllWithCustomer();

    @Query("SELECT m FROM Meter m LEFT JOIN FETCH m.customer WHERE m.id = :id")
    Optional<Meter> findByIdWithCustomer(@Param("id") Long id);

    @Query("SELECT DISTINCT m FROM Meter m LEFT JOIN FETCH m.customer WHERE m.customer.id = :customerId")
    List<Meter> findByCustomerIdWithCustomer(@Param("customerId") Long customerId);
}

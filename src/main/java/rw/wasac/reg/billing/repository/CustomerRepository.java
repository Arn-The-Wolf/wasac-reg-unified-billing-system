package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.wasac.reg.billing.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByNationalId(String nationalId);
    Optional<Customer> findByEmail(String email);
    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
}

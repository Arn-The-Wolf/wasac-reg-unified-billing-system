/**
 * Spring Data JPA repository for Notification persistence.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.wasac.reg.billing.entity.CustomerNotification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<CustomerNotification, Long> {

    List<CustomerNotification> findByCustomerIdOrderBySentAtDesc(Long customerId);

    @Query("SELECT DISTINCT n FROM CustomerNotification n LEFT JOIN FETCH n.customer")
    List<CustomerNotification> findAllWithCustomer();

    @Query("SELECT DISTINCT n FROM CustomerNotification n LEFT JOIN FETCH n.customer WHERE n.customer.id = :customerId ORDER BY n.sentAt DESC")
    List<CustomerNotification> findByCustomerIdWithCustomer(@Param("customerId") Long customerId);
}

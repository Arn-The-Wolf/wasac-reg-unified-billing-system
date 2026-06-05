/**
 * Service implementation providing Notification business logic.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.response.NotificationResponse;
import rw.wasac.reg.billing.entity.CustomerNotification;
import rw.wasac.reg.billing.repository.NotificationRepository;
import rw.wasac.reg.billing.service.NotificationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAllWithCustomer().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByCustomerId(Long customerId) {
        return notificationRepository.findByCustomerIdWithCustomer(customerId)
                .stream().map(this::toResponse).toList();
    }

    private NotificationResponse toResponse(CustomerNotification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomer().getId())
                .customerName(notification.getCustomer().getFullName())
                .message(notification.getMessage())
                .month(notification.getBillingMonth())
                .year(notification.getBillingYear())
                .monthYear(notification.getMonthYear())
                .sentAt(notification.getSentAt())
                .build();
    }
}

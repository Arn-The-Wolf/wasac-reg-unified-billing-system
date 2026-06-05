package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getAll();
    List<NotificationResponse> getByCustomerId(Long customerId);
}

/**
 * Admin user management: list, update, activate, and deactivate system accounts.
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.UserUpdateRequest;
import rw.wasac.reg.billing.dto.response.UserResponse;
import rw.wasac.reg.billing.entity.User;
import rw.wasac.reg.billing.enums.Role;
import rw.wasac.reg.billing.enums.UserStatus;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.exception.ResourceNotFoundException;
import rw.wasac.reg.billing.repository.UserRepository;
import rw.wasac.reg.billing.service.StaffNotificationService;
import rw.wasac.reg.billing.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StaffNotificationService staffNotificationService;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findEntity(id);
        if (request.getRole() == Role.ROLE_ADMIN && user.getRole() != Role.ROLE_ADMIN) {
            throw new BadRequestException("Cannot promote users to ADMIN via API. Create admin accounts manually.");
        }
        user.setFullName(request.getFullName());
        if (request.getCountryCode() != null) {
            user.setCountryCode(request.getCountryCode());
        }
        user.setPhoneNumber(request.getPhoneNumber());
        user.setStatus(request.getStatus());
        user.setRole(request.getRole());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse activate(Long id) {
        User user = findEntity(id);
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new BadRequestException("User must verify email OTP before activation");
        }
        user.setStatus(UserStatus.ACTIVE);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse deactivate(Long id) {
        User user = findEntity(id);
        user.setStatus(UserStatus.INACTIVE);
        User saved = userRepository.save(user);
        staffNotificationService.notifyUserDeactivated(saved);
        return toResponse(saved);
    }

    private User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .countryCode(user.getCountryCode())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .role(user.getRole())
                .customerId(user.getCustomer() != null ? user.getCustomer().getId() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}

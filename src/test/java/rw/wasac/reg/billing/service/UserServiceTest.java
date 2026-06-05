package rw.wasac.reg.billing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.wasac.reg.billing.dto.request.UserUpdateRequest;
import rw.wasac.reg.billing.entity.User;
import rw.wasac.reg.billing.enums.Role;
import rw.wasac.reg.billing.enums.UserStatus;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.repository.UserRepository;
import rw.wasac.reg.billing.serviceImpl.UserServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StaffNotificationService staffNotificationService;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void deactivate_setsInactiveStatus() {
        User user = User.builder().id(2L).email("op@wasac.rw").status(UserStatus.ACTIVE)
                .role(Role.ROLE_OPERATOR).fullName("Operator").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var response = userService.deactivate(2L);
        assertThat(response.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void update_rejectsAdminPromotion() {
        User user = User.builder().id(3L).role(Role.ROLE_CUSTOMER).fullName("Cust").build();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFullName("Cust");
        request.setPhoneNumber("0788123456");
        request.setStatus(UserStatus.ACTIVE);
        request.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.update(3L, request))
                .isInstanceOf(BadRequestException.class);
    }
}

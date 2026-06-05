/**
 * Service contract for admin user management operations.
 */
package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.request.UserUpdateRequest;
import rw.wasac.reg.billing.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAll();
    UserResponse getById(Long id);
    UserResponse update(Long id, UserUpdateRequest request);
    UserResponse activate(Long id);
    UserResponse deactivate(Long id);
}

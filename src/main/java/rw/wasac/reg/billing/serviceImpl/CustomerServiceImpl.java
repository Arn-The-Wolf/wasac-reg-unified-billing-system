/**
 * Service implementation providing Customer business logic.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.CustomerRequest;
import rw.wasac.reg.billing.dto.response.CustomerResponse;
import rw.wasac.reg.billing.entity.Customer;
import rw.wasac.reg.billing.enums.CustomerStatus;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.exception.DuplicateResourceException;
import rw.wasac.reg.billing.exception.ResourceNotFoundException;
import rw.wasac.reg.billing.repository.CustomerRepository;
import rw.wasac.reg.billing.service.CustomerService;
import rw.wasac.reg.billing.service.StaffNotificationService;

import java.util.List;

/**
 * Inactive customer rules:
 * - Cannot register new meters for inactive customers
 * - Cannot record meter readings on meters belonging to inactive customers
 * - Cannot generate bills for inactive customers
 * - Existing bills remain payable but no new billing cycles are started
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    private final StaffNotificationService staffNotificationService;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        validateUnique(request.getNationalId(), request.getEmail(), null);

        Customer customer = modelMapper.map(request, Customer.class);
        customer.setStatus(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE);
        return toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findEntity(id);
        validateUnique(request.getNationalId(), request.getEmail(), id);

        customer.setFullName(request.getFullName());
        customer.setNationalId(request.getNationalId());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }

        return toResponse(customerRepository.save(customer));
    }

    @Override
    public CustomerResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CustomerResponse activate(Long id) {
        Customer customer = findEntity(id);
        customer.setStatus(CustomerStatus.ACTIVE);
        Customer saved = customerRepository.save(customer);
        staffNotificationService.notifyCustomerActivated(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse deactivate(Long id) {
        Customer customer = findEntity(id);
        customer.setStatus(CustomerStatus.INACTIVE);
        Customer saved = customerRepository.save(customer);
        staffNotificationService.notifyCustomerDeactivated(saved);
        return toResponse(saved);
    }

    public void assertCustomerActive(Customer customer) {
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BadRequestException("Customer is inactive: " + customer.getFullName());
        }
    }

    private Customer findEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private void validateUnique(String nationalId, String email, Long excludeId) {
        customerRepository.findByNationalId(nationalId).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new DuplicateResourceException("National ID already exists: " + nationalId);
            }
        });
        customerRepository.findByEmail(email).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new DuplicateResourceException("Email already exists: " + email);
            }
        });
    }

    private CustomerResponse toResponse(Customer customer) {
        return modelMapper.map(customer, CustomerResponse.class);
    }
}

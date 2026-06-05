package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.request.CustomerRequest;
import rw.wasac.reg.billing.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);
    CustomerResponse update(Long id, CustomerRequest request);
    CustomerResponse getById(Long id);
    List<CustomerResponse> getAll();
    void delete(Long id);
    CustomerResponse activate(Long id);
    CustomerResponse deactivate(Long id);
}

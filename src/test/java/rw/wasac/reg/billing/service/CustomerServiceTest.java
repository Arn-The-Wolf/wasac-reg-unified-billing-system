package rw.wasac.reg.billing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import rw.wasac.reg.billing.dto.request.CustomerRequest;
import rw.wasac.reg.billing.entity.Customer;
import rw.wasac.reg.billing.enums.CustomerStatus;
import rw.wasac.reg.billing.exception.DuplicateResourceException;
import rw.wasac.reg.billing.repository.CustomerRepository;
import rw.wasac.reg.billing.serviceImpl.CustomerServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void create_rejectsDuplicateNationalId() {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("Test");
        request.setNationalId("1199880077665544");
        request.setEmail("new@test.com");
        request.setPhone("0788123456");
        request.setAddress("Kigali");
        request.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findByNationalId("1199880077665544"))
                .thenReturn(Optional.of(Customer.builder().id(99L).build()));

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("National ID");
    }
}

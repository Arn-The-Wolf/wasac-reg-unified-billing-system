package rw.wasac.reg.billing.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rw.wasac.reg.billing.constant.AppConstants;
import rw.wasac.reg.billing.dto.request.CustomerRequest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RwandaNationalIdConstraintValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void customerRequest_surfacesSpecificNidErrorMessage() {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("Jean Uwimana");
        request.setNationalId("0199880077665544");
        request.setEmail("jean@example.com");
        request.setPhone("0788123456");
        request.setAddress("Kigali, Rwanda");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(AppConstants.NATIONAL_ID_STATUS_MESSAGE);
    }
}

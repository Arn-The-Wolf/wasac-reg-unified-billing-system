/**
 * Jakarta Bean Validation adapter for {@link rw.wasac.reg.billing.utils.RwandaNationalIdValidator}.
 */
package rw.wasac.reg.billing.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import rw.wasac.reg.billing.utils.RwandaNationalIdValidator;

public class RwandaNationalIdConstraintValidator implements ConstraintValidator<ValidRwandaNationalId, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return RwandaNationalIdValidator.validate(value)
                .map(error -> {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(error).addConstraintViolation();
                    return false;
                })
                .orElse(true);
    }
}

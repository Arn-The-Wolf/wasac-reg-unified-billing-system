/**
 * Bean Validation constraint for Rwanda National ID (NIN) numbers.
 */
package rw.wasac.reg.billing.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import rw.wasac.reg.billing.constant.AppConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = RwandaNationalIdConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRwandaNationalId {

    String message() default AppConstants.NATIONAL_ID_INVALID_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

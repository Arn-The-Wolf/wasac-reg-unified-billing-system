/**
 * Exception type: ResourceNotFoundException for API error handling.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

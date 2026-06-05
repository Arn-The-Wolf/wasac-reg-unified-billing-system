/**
 * Exception type: DuplicateResourceException for API error handling.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

/**
 * Exception type: BadRequestException for API error handling.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

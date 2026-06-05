/**
 * Application-wide constants used across the billing system.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.constant;

public final class AppConstants {

    public static final String PASSWORD_PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
    public static final String PASSWORD_MESSAGE =
            "Password must be at least 8 characters and include letters, digits, and symbols";

    public static final String COUNTRY_CODE_PATTERN = "^\\+\\d{1,4}$";
    public static final String COUNTRY_CODE_MESSAGE = "Country code must start with + followed by 1-4 digits";

    public static final String PHONE_NUMBER_PATTERN = "^[0-9]{9,12}$";
    public static final String PHONE_NUMBER_MESSAGE = "Phone number must be 9-12 digits without country code";

    public static final String CUSTOMER_PHONE_PATTERN = "^(\\+250[0-9]{9}|0?7[0-9]{8})$";
    public static final String CUSTOMER_PHONE_MESSAGE =
            "Phone must be a valid Rwanda number (+250XXXXXXXXX or 07XXXXXXXX)";

    public static final String NATIONAL_ID_PATTERN = "^[0-9]{16}$";
    public static final String NATIONAL_ID_MESSAGE = "National ID must be exactly 16 digits";

    public static final String EMAIL_MESSAGE = "Email must be a valid email address";
    public static final String EMAIL_REQUIRED_MESSAGE = "Email is required";

    public static final String FULL_NAME_REQUIRED_MESSAGE = "Full name is required";
    public static final String FULL_NAME_SIZE_MESSAGE = "Full name must be between 2 and 150 characters";

    public static final String ADDRESS_REQUIRED_MESSAGE = "Address is required";
    public static final String ADDRESS_SIZE_MESSAGE = "Address must be between 5 and 255 characters";

    public static final String METER_NUMBER_PATTERN = "^[A-Z0-9-]+$";
    public static final String METER_NUMBER_MESSAGE =
            "Meter number must contain only uppercase letters, digits, or hyphens";
    public static final String METER_NUMBER_SIZE_MESSAGE = "Meter number must be between 3 and 50 characters";

    public static final String OTP_CODE_PATTERN = "\\d{6}";
    public static final String OTP_CODE_MESSAGE = "OTP must be exactly 6 digits";
    public static final String OTP_CODE_REQUIRED_MESSAGE = "OTP code is required";

    public static final String DATE_REQUIRED_MESSAGE = "Date is required";
    public static final String DATE_PAST_OR_PRESENT_MESSAGE = "Date cannot be in the future";
    public static final String INSTALLATION_DATE_MESSAGE = "Installation date cannot be in the future";
    public static final String READING_DATE_MESSAGE = "Reading date cannot be in the future";
    public static final String PAYMENT_DATE_MESSAGE = "Payment date cannot be in the future";

    public static final String AMOUNT_REQUIRED_MESSAGE = "Amount is required";
    public static final String AMOUNT_MIN_MESSAGE = "Amount must be greater than zero";
    public static final String READING_REQUIRED_MESSAGE = "Reading value is required";
    public static final String READING_MIN_MESSAGE = "Reading value cannot be negative";

    public static final String PAYMENT_METHOD_REQUIRED_MESSAGE = "Payment method is required";
    public static final String BILL_ID_REQUIRED_MESSAGE = "Bill ID is required";
    public static final String METER_READING_ID_REQUIRED_MESSAGE = "Meter reading ID is required";
    public static final String METER_ID_REQUIRED_MESSAGE = "Meter ID is required";
    public static final String CUSTOMER_ID_REQUIRED_MESSAGE = "Customer ID is required";
    public static final String METER_TYPE_REQUIRED_MESSAGE = "Meter type is required";

    public static final String NOTES_SIZE_MESSAGE = "Notes cannot exceed 500 characters";

    private AppConstants() {
    }
}

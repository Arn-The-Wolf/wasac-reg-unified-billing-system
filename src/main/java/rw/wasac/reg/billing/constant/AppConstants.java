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

    public static final String PHONE_NUMBER_PATTERN = "^7[235789][0-9]{7}$";
    public static final String PHONE_NUMBER_MESSAGE =
            "Phone number must be a valid 9-digit Rwanda mobile number without country code (e.g. 788123456)";

    public static final String COUNTRY_CODE_REQUIRED_MESSAGE = "Country code is required";

    public static final String CUSTOMER_PHONE_PATTERN = "^(\\+250[0-9]{9}|0?7[0-9]{8})$";
    public static final String CUSTOMER_PHONE_MESSAGE =
            "Phone must be a valid Rwanda number (+250XXXXXXXXX or 07XXXXXXXX)";

    /** Full NIDA structure: status + 19|20YY + gender(7|8) + birth-order(7) + issue(1) + checksum(2). */
    public static final String NATIONAL_ID_PATTERN =
            "^[1-3](19|20)\\d{2}[78]\\d{7}\\d\\d{2}$";

    public static final String NATIONAL_ID_REQUIRED_MESSAGE =
            "National ID (NIN) is required";

    public static final String NATIONAL_ID_NON_NUMERIC_MESSAGE =
            "National ID must contain digits only; remove spaces, letters, hyphens, or other characters";

    public static final String NATIONAL_ID_LENGTH_MESSAGE =
            "National ID must be exactly %2$d digits (NIDA format); you provided %1$d digit(s)";

    public static final String NATIONAL_ID_STATUS_MESSAGE =
            "Invalid holder status at digit 1: use 1 (Rwandan citizen), 2 (refugee with ID), or 3 (foreigner)";

    public static final String NATIONAL_ID_BIRTH_YEAR_FORMAT_MESSAGE =
            "Invalid birth year at digits 2–5: year must start with 19 or 20 (e.g. 1996, 2004)";

    public static final String NATIONAL_ID_BIRTH_YEAR_FUTURE_MESSAGE =
            "Birth year %1$d in the National ID cannot be after the current year (%2$d)";

    public static final String NATIONAL_ID_MIN_AGE_MESSAGE =
            "National ID indicates the holder is %2$d years old (born %3$d). "
                    + "Rwanda NIDs are issued from age %1$d; the birth year in digits 2–5 must reflect that";

    public static final String NATIONAL_ID_GENDER_MESSAGE =
            "Invalid gender digit at position 6: use 7 (female) or 8 (male)";

    public static final String NATIONAL_ID_STRUCTURE_MESSAGE =
            "National ID does not match the official NIDA 16-digit structure. "
                    + "Expected: [status 1–3][birth year YYYY][gender 7|8][7-digit order][issue 0–9][2-digit security code]";

    public static final String NATIONAL_ID_INVALID_MESSAGE =
            "National ID is not a valid Rwanda NIN (NIDA format)";

    /** @deprecated Use {@link #NATIONAL_ID_REQUIRED_MESSAGE} or specific NIDA validation messages. */
    @Deprecated
    public static final String NATIONAL_ID_MESSAGE = NATIONAL_ID_INVALID_MESSAGE;

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

    public static final String LOGIN_PASSWORD_REQUIRED_MESSAGE = "Password is required";

    public static final String ROLE_REQUIRED_MESSAGE = "User role is required";

    public static final String USER_STATUS_REQUIRED_MESSAGE = "User status is required (ACTIVE or INACTIVE)";

    public static final String CUSTOMER_STATUS_REQUIRED_MESSAGE = "Customer status is required (ACTIVE or INACTIVE)";

    public static final String METER_STATUS_REQUIRED_MESSAGE = "Meter status is required (ACTIVE or INACTIVE)";

    public static final String ID_POSITIVE_MESSAGE = "ID must be a positive number greater than zero";

    public static final String CONFIG_NAME_REQUIRED_MESSAGE = "Name is required";
    public static final String CONFIG_NAME_SIZE_MESSAGE = "Name must be between 2 and 100 characters";

    public static final String TARIFF_TYPE_REQUIRED_MESSAGE = "Tariff type is required (FLAT or TIER)";
    public static final String TARIFF_METER_TYPE_REQUIRED_MESSAGE = "Meter type is required (WATER or ELECTRICITY)";
    public static final String EFFECTIVE_FROM_REQUIRED_MESSAGE = "Effective from date is required";
    public static final String EFFECTIVE_TO_AFTER_FROM_MESSAGE =
            "Effective end date must be after the effective start date";
    public static final String FLAT_RATE_REQUIRED_MESSAGE =
            "Flat tariff requires a rate greater than zero";
    public static final String TARIFF_TIERS_REQUIRED_MESSAGE =
            "Tier-based tariff requires at least one consumption tier";

    public static final String TIER_FROM_UNITS_REQUIRED_MESSAGE = "Tier from-units value is required";
    public static final String TIER_TO_UNITS_MIN_MESSAGE = "Tier to-units cannot be negative";
    public static final String TIER_RATE_REQUIRED_MESSAGE = "Tier rate per unit is required";
    public static final String TIER_RATE_MIN_MESSAGE = "Tier rate per unit must be greater than zero";
    public static final String TIER_RANGE_MESSAGE = "Tier to-units must be greater than from-units";

    public static final String FIXED_CHARGE_AMOUNT_MIN_MESSAGE = "Fixed charge amount must be greater than zero";

    public static final String PERCENTAGE_REQUIRED_MESSAGE = "Percentage is required";
    public static final String PERCENTAGE_RANGE_MESSAGE = "Percentage must be between 0.01 and 100";

    public static final String BILLING_MONTH_MESSAGE = "Billing month must be between 1 and 12";
    public static final String BILLING_YEAR_MESSAGE = "Billing year must be between 2000 and 2100";

    public static final String EMAIL_SIZE_MESSAGE = "Email must not exceed 150 characters";

    public static final String BILL_REFERENCE_PATTERN = "^BILL-[A-F0-9]{8}$";
    public static final String BILL_REFERENCE_MESSAGE =
            "Bill reference must match format BILL-XXXXXXXX (8 uppercase hex characters)";

    private AppConstants() {
    }
}

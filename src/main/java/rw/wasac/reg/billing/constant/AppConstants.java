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

    private AppConstants() {
    }
}

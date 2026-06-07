/**
 * Validates Rwanda National Identification Numbers (NIN) issued by NIDA.
 *
 * <p>Official 16-digit structure (NIDA):
 * <ul>
 *   <li>Digit 1 — holder status: 1 (Rwandan citizen), 2 (refugee), 3 (foreigner)</li>
 *   <li>Digits 2–5 — year of birth (e.g. 1996, 2004)</li>
 *   <li>Digit 6 — gender: 7 (female), 8 (male)</li>
 *   <li>Digits 7–13 — birth-order sequence for that year and gender</li>
 *   <li>Digit 14 — issuance count: 0 (first card), 1–9 (reissues)</li>
 *   <li>Digits 15–16 — security checksum (format only; algorithm is NIDA-internal)</li>
 * </ul>
 *
 * @see <a href="https://www.ktpress.rw/">KT Press — Rwanda NIN structure</a>
 */
package rw.wasac.reg.billing.utils;

import rw.wasac.reg.billing.constant.AppConstants;

import java.time.Year;
import java.util.Optional;
import java.util.regex.Pattern;

public final class RwandaNationalIdValidator {

    private static final int NID_LENGTH = 16;
    private static final int MIN_HOLDER_AGE = 16;
    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");
    private static final Pattern NIDA_STRUCTURE = Pattern.compile("^[1-3](19|20)\\d{2}[78]\\d{7}\\d\\d{2}$");

    private RwandaNationalIdValidator() {
    }

    /**
     * @return empty if valid; otherwise a specific human-readable error message
     */
    public static Optional<String> validate(String nationalId) {
        if (nationalId == null || nationalId.isBlank()) {
            return Optional.of(AppConstants.NATIONAL_ID_REQUIRED_MESSAGE);
        }

        String nid = nationalId.trim();

        if (!DIGITS_ONLY.matcher(nid).matches()) {
            return Optional.of(AppConstants.NATIONAL_ID_NON_NUMERIC_MESSAGE);
        }

        if (nid.length() != NID_LENGTH) {
            return Optional.of(String.format(
                    AppConstants.NATIONAL_ID_LENGTH_MESSAGE, nid.length(), NID_LENGTH));
        }

        char statusDigit = nid.charAt(0);
        if (statusDigit != '1' && statusDigit != '2' && statusDigit != '3') {
            return Optional.of(AppConstants.NATIONAL_ID_STATUS_MESSAGE);
        }

        String birthYearPart = nid.substring(1, 5);
        if (!birthYearPart.startsWith("19") && !birthYearPart.startsWith("20")) {
            return Optional.of(AppConstants.NATIONAL_ID_BIRTH_YEAR_FORMAT_MESSAGE);
        }

        int birthYear;
        try {
            birthYear = Integer.parseInt(birthYearPart);
        } catch (NumberFormatException ex) {
            return Optional.of(AppConstants.NATIONAL_ID_BIRTH_YEAR_FORMAT_MESSAGE);
        }

        int currentYear = Year.now().getValue();
        if (birthYear > currentYear) {
            return Optional.of(String.format(
                    AppConstants.NATIONAL_ID_BIRTH_YEAR_FUTURE_MESSAGE, birthYear, currentYear));
        }

        int holderAge = currentYear - birthYear;
        if (holderAge < MIN_HOLDER_AGE) {
            return Optional.of(String.format(
                    AppConstants.NATIONAL_ID_MIN_AGE_MESSAGE, MIN_HOLDER_AGE, holderAge, birthYear));
        }

        char genderDigit = nid.charAt(5);
        if (genderDigit != '7' && genderDigit != '8') {
            return Optional.of(AppConstants.NATIONAL_ID_GENDER_MESSAGE);
        }

        if (!NIDA_STRUCTURE.matcher(nid).matches()) {
            return Optional.of(AppConstants.NATIONAL_ID_STRUCTURE_MESSAGE);
        }

        return Optional.empty();
    }

    public static boolean isValid(String nationalId) {
        return validate(nationalId).isEmpty();
    }
}

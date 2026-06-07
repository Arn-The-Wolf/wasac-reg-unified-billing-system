package rw.wasac.reg.billing.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rw.wasac.reg.billing.constant.AppConstants;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

class RwandaNationalIdValidatorTest {

    @Test
    void validate_acceptsKnownValidNids() {
        assertThat(RwandaNationalIdValidator.isValid("1199880077665544")).isTrue();
        assertThat(RwandaNationalIdValidator.isValid("1199887766554433")).isTrue();
        assertThat(RwandaNationalIdValidator.isValid("1199672222000040")).isTrue();
        assertThat(RwandaNationalIdValidator.isValid("2199672222000040")).isTrue();
        assertThat(RwandaNationalIdValidator.isValid("3199672222000040")).isTrue();
    }

    @Test
    void validate_rejectsNullBlankAndWhitespace() {
        assertThat(RwandaNationalIdValidator.validate(null))
                .contains(AppConstants.NATIONAL_ID_REQUIRED_MESSAGE);
        assertThat(RwandaNationalIdValidator.validate(""))
                .contains(AppConstants.NATIONAL_ID_REQUIRED_MESSAGE);
        assertThat(RwandaNationalIdValidator.validate("   "))
                .contains(AppConstants.NATIONAL_ID_REQUIRED_MESSAGE);
    }

    @Test
    void validate_rejectsNonNumericCharacters() {
        assertThat(RwandaNationalIdValidator.validate("119988007766554A"))
                .contains(AppConstants.NATIONAL_ID_NON_NUMERIC_MESSAGE);
        assertThat(RwandaNationalIdValidator.validate("1199 880077665544"))
                .contains(AppConstants.NATIONAL_ID_NON_NUMERIC_MESSAGE);
        assertThat(RwandaNationalIdValidator.validate("119988007766554-"))
                .contains(AppConstants.NATIONAL_ID_NON_NUMERIC_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "119988007766554", "11998800776655444"})
    void validate_rejectsIncorrectLength(String nid) {
        assertThat(RwandaNationalIdValidator.validate(nid))
                .hasValueSatisfying(message -> {
                    assertThat(message).contains("16");
                    assertThat(message).contains(String.valueOf(nid.length()));
                });
    }

    @Test
    void validate_rejectsInvalidStatusDigit() {
        assertThat(RwandaNationalIdValidator.validate("0199880077665544"))
                .contains(AppConstants.NATIONAL_ID_STATUS_MESSAGE);
        assertThat(RwandaNationalIdValidator.validate("9199880077665544"))
                .contains(AppConstants.NATIONAL_ID_STATUS_MESSAGE);
    }

    @Test
    void validate_rejectsInvalidBirthYearPrefix() {
        assertThat(RwandaNationalIdValidator.validate("1188880077665544"))
                .contains(AppConstants.NATIONAL_ID_BIRTH_YEAR_FORMAT_MESSAGE);
        assertThat(RwandaNationalIdValidator.validate("1210080077665544"))
                .contains(AppConstants.NATIONAL_ID_BIRTH_YEAR_FORMAT_MESSAGE);
    }

    @Test
    void validate_rejectsFutureBirthYear() {
        int futureYear = Year.now().getValue() + 1;
        String nid = "1" + futureYear + "80077665544";

        assertThat(RwandaNationalIdValidator.validate(nid))
                .hasValueSatisfying(message -> {
                    assertThat(message).contains(String.valueOf(futureYear));
                    assertThat(message).contains("cannot be after the current year");
                });
    }

    @Test
    void validate_rejectsHolderYoungerThanSixteen() {
        int birthYear = Year.now().getValue() - 10;
        String nid = "1" + birthYear + "78007766544";

        assertThat(nid).hasSize(16);
        assertThat(RwandaNationalIdValidator.validate(nid))
                .hasValueSatisfying(message -> {
                    assertThat(message).contains("16");
                    assertThat(message).contains(String.valueOf(birthYear));
                });
    }

    @Test
    void validate_rejectsInvalidGenderDigit() {
        assertThat(RwandaNationalIdValidator.isValid("1199880077665544")).isTrue();

        assertThat(RwandaNationalIdValidator.validate("1199810077665544"))
                .contains(AppConstants.NATIONAL_ID_GENDER_MESSAGE);
        assertThat(RwandaNationalIdValidator.validate("1199890077665544"))
                .contains(AppConstants.NATIONAL_ID_GENDER_MESSAGE);
    }

    @Test
    void validate_rejectsIdsUsedInCommunityValidatorsAsInvalid() {
        assertThat(RwandaNationalIdValidator.isValid("1201772222000040")).isFalse();
    }

    @Test
    void validate_trimsSurroundingWhitespaceBeforeChecking() {
        assertThat(RwandaNationalIdValidator.isValid(" 1199880077665544 ")).isTrue();
    }
}

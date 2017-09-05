package uk.nhs.careconnect.ri.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import uk.org.hl7.fhir.validation.NhsCodeValidator;

public final class NhsCodeValidatorTest {

    @Test
    public void nhsCodeValidatorTest() {
        assertThat(NhsCodeValidator.nhsNumberValid("0123456789")).isTrue();
        assertThat(NhsCodeValidator.nhsNumberValid(null)).isFalse();
        assertThat(NhsCodeValidator.nhsNumberValid("")).isFalse();
        assertThat(NhsCodeValidator.nhsNumberValid("012345678")).isFalse();
        assertThat(NhsCodeValidator.nhsNumberValid("01234567890")).isFalse();
        assertThat(NhsCodeValidator.nhsNumberValid("012345678A")).isFalse();
    }
}

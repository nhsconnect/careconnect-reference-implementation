package uk.org.hl7.fhir.validation;

public final class NhsCodeValidator {
    private NhsCodeValidator() {}

    public static boolean nhsNumberValid(String nhsNumber) {

        // NHS numbers should only contain 10 numeric values
        if (null == nhsNumber || !nhsNumber.matches("[0-9]{10}")) {
            return false;
        }

        // Modulus 11 Checked
        String[] nhsNumberDigits = nhsNumber.split("(?!^)");

        int result = Integer.parseInt(nhsNumberDigits[0]) * 10
                + Integer.parseInt(nhsNumberDigits[1]) * 9
                + Integer.parseInt(nhsNumberDigits[2]) * 8
                + Integer.parseInt(nhsNumberDigits[3]) * 7
                + Integer.parseInt(nhsNumberDigits[4]) * 6
                + Integer.parseInt(nhsNumberDigits[5]) * 5
                + Integer.parseInt(nhsNumberDigits[6]) * 4
                + Integer.parseInt(nhsNumberDigits[7]) * 3
                + Integer.parseInt(nhsNumberDigits[8]) * 2;
        result = (11 - (result % 11)) % 11;

        return result == Integer.parseInt(nhsNumberDigits[9]);
    }
}

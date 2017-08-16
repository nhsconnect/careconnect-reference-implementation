package uk.org.hl7.fhir.validation.dstu2;

import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

public class Validate {

    public static void validate(FhirValidator validator, String resource)
    {
        ValidationResult result = validator.validateWithResult(resource);

        // System.out.println(result.isSuccessful()); // false

        // Show the issues
        for (SingleValidationMessage next : result.getMessages()) {
            switch (next.getSeverity())
            {
                case FATAL:
                    System.out.println(" Next issue " + (char)27 + "[31mFATAL" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                case ERROR:
                    System.out.println(" Next issue " + (char)27 + "[31mERROR" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                case WARNING:
                    System.out.println(" Next issue " + (char)27 + "[33mWARNING" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                case INFORMATION:
                    System.out.println(" Next issue " + (char)27 + "[34mINFORMATION" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                default:
                    System.out.println(" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
            }
        }
    }
}

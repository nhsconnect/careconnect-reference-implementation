package uk.nhs.careconnect.ri.stu3.dao;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Identifier;
import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

public final class daoutils {

    // KGM 3/1/2018 Limiting the number of results returned from 'wildcard' searches.
    public static final int MAXROWS = 1000;
    public static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    public static String removeSpace(String s) { return s.replaceAll("\\s+",""); }

    public static Enumerations.AdministrativeGender getGender(String gender) {

            switch (gender)
            {
                case "MALE":
                   return Enumerations.AdministrativeGender.MALE;
                case "FEMALE":
                    return Enumerations.AdministrativeGender.FEMALE;
                case "OTHER":
                    return Enumerations.AdministrativeGender.OTHER;
                case "UNKNOWN":
                    return Enumerations.AdministrativeGender.UNKNOWN;
            }

        return null;
    }

    public static Identifier getIdentifier(BaseIdentifier baseIdentifier, Identifier identifier) {
        if (baseIdentifier.getSystem() != null) identifier.setSystem(baseIdentifier.getSystem().getUri());
        if (baseIdentifier.getValue() != null) identifier.setValue(baseIdentifier.getValue());
        if (baseIdentifier.getUse() != null) {
            identifier.setUse(baseIdentifier.getIdentifierUse());
        }
        if (baseIdentifier.getIdentifierType() != null) {
            identifier.getType()
                    .addCoding()
                        .setCode(baseIdentifier.getIdentifierType().getCode())
                        .setDisplay(baseIdentifier.getIdentifierType().getDisplay())
                        .setSystem(baseIdentifier.getIdentifierType().getSystem());
        }
        return identifier;
    }

}

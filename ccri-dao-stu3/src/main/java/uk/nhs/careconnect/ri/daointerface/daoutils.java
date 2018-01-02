package uk.nhs.careconnect.ri.daointerface;

import org.hl7.fhir.dstu3.model.Enumerations;

public final class daoutils {

    public static final int MAXROWS = 500;
    public static boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

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

}

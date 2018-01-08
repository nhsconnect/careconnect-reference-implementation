package uk.org.hl7.fhir.core.Stu3;

/**
 * Created by kevinmayfield on 26/05/2017.
 */
public class CareConnectSystem {
    public static final String NHSNumber = "https://fhir.nhs.uk/Id/nhs-number";

    public static final String ODSOrganisationCode ="https://fhir.nhs.uk/Id/ods-organization-code";
    public static final String SDSUserId="https://fhir.nhs.uk/Id/sds-user-id";
    public static final String ODSSiteCode="https://fhir.nhs.uk/Id/ods-site-code";



    public static final String NHSNumberVerificationStatus = "https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-NHSNumberVerificationStatus-1";
    // KGM 8/1/2018 altered to hl7.org.uk from fhir.nhs.uk
    public static final String EthnicCategory ="https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-EthnicCategory-1";
    public static final String SDSJobRoleName="https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-SDSJobRoleName-1";

    // Core FHIR Systems
    public static final String UnitOfMeasure = "http://unitsofmeasure.org";
    public static final String SNOMEDCT = "http://snomed.info/sct";
    public static final String LOINC = "http://loinc.org";
    public static final String OrganisationType ="http://hl7.org/fhir/organization-type";

    public static final String FHIRObservationCategory = "http://hl7.org/fhir/observation-category";
    public static final String HL7v2Table0078 = "http://hl7.org/fhir/v2/0078";

    public static final String HL7v3MaritalStatus = "http://hl7.org/fhir/v3/MaritalStatus";
}

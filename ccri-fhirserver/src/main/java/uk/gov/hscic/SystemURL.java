package uk.gov.hscic;

public final class SystemURL {

    private SystemURL() { }

    // Base Constants
    public static final String AUTHORIZATION_TOKEN = "https://authorize.fhir.nhs.net/token";
    public static final String SDS_USER_ID = "http://fhir.nhs.net/sds-user-id";
    public static final String SNOMED = "http://snomed.info/sct";

    // HL7 Constants
    public static final String HL7_BASIC_RESOURCE_TYPE = "http://hl7.org/fhir/basic-resource-type";
    public static final String HL7_VS_C80_PRACTICE_CODES = "http://hl7.org/fhir/ValueSet/c80-practice-codes";

    // FHIR ID Constants
    public static final String ID_GPC_APPOINTMENT_IDENTIFIER = "http://fhir.nhs.net/Id/gpconnect-appointment-identifier";
    public static final String ID_GPC_SCHEDULE_IDENTIFIER = "http://fhir.nhs.net/Id/gpconnect-schedule-identifier";
    //public static final String ID_NHS_NUMBER = "http://fhir.nhs.net/Id/nhs-number";
    //public static final String ID_ODS_ORGANIZATION_CODE = "http://fhir.nhs.net/Id/ods-organization-code";
    //public static final String ID_ODS_SITE_CODE = "http://fhir.nhs.net/Id/ods-site-code";
    //public static final String ID_SDS_ROLE_PROFILE_ID = "http://fhir.nhs.net/Id/sds-role-profile-id";
    //public static final String ID_SDS_USER_ID = "http://fhir.nhs.net/Id/sds-user-id";

    // FHIR StructureDefinition Constants
    public static final String SD_EXTENSION_GPC_APPOINTMENT_CANCELLATION_REASON = "http://fhir.nhs.net/StructureDefinition/extension-gpconnect-appointment-cancellation-reason-1";
    public static final String SD_EXTENSION_GPC_PRACTITIONER = "http://fhir.nhs.net/StructureDefinition/extension-gpconnect-practitioner-1";
    public static final String SD_EXTENSION_MEDICATION_QUANTITY_TEXT = "http://fhir.nhs.net/StructureDefinition/extension-medication-quantity-text-1-0";
    public static final String SD_EXTENSION_PERSCRIPTION_REPEAT_REVIEW_DATE = "http://fhir.nhs.net/StructureDefinition/extension-prescription-repeat-review-date-1-0";
    public static final String SD_EXTENSION_REGISTRATION_PERIOD = "http://fhir.nhs.net/StructureDefinition/extension-registration-period-1";
    public static final String SD_EXTENSION_REGISTRATION_STATUS = "http://fhir.nhs.net/StructureDefinition/extension-registration-status-1";
    public static final String SD_EXTENSION_REGISTRATION_TYPE = "http://fhir.nhs.net/StructureDefinition/extension-registration-type-1";
    public static final String SD_GPC_CARERECORD_COMPOSITION = "http://fhir.nhs.net/StructureDefinition/gpconnect-carerecord-composition-1";
    public static final String SD_GPC_OPERATIONOUTCOME = "http://fhir.nhs.net/StructureDefinition/gpconnect-operationoutcome-1";
    //public static final String SD_GPC_ORGANIZATION = "http://fhir.nhs.net/StructureDefinition/gpconnect-organization-1";
    //public static final String SD_GPC_PATIENT = "http://fhir.nhs.net/StructureDefinition/gpconnect-patient-1";
    //public static final String SD_GPC_PRACTITIONER = "http://fhir.nhs.net/StructureDefinition/gpconnect-practitioner-1";
    public static final String SD_GPC_APPOINTMENT = "http://fhir.nhs.net/StructureDefinition/gpconnect-appointment-1";
    public static final String SD_GPC_GET_SCHEDULE_BUNDLE = "http://fhir.nhs.net/StructureDefinition/gpconnect-getschedule-bundle-1";
    //public static final String SD_GPC_LOCATION = "http://fhir.nhs.net/StructureDefinition/gpconnect-location-1";
    public static final String SD_GPC_SLOT = "http://fhir.nhs.net/StructureDefinition/gpconnect-slot-1";
    
    // FHIR ValueSet Constants
    public static final String VS_GPC_ERROR_WARNING_CODE = "http://fhir.nhs.net/ValueSet/gpconnect-error-or-warning-code-1";
    public static final String VS_GPC_REASON_TYPE = "http://fhir.nhs.net/ValueSet/gpconnect-reason-type-1-0";
    public static final String VS_GPC_RECORD_SECTION = "http://fhir.nhs.net/ValueSet/gpconnect-record-section-1";
    public static final String VS_HUMAN_LANGUAGE = "http://fhir.nhs.net/ValueSet/human-language-1";
    public static final String VS_REGISTRATION_STATUS = "http://fhir.nhs.net/ValueSet/registration-status-1";
    public static final String VS_REGISTRATION_TYPE = "http://fhir.nhs.net/ValueSet/registration-type-1";
    public static final String VS_SDS_JOB_ROLE_NAME = "http://fhir.nhs.net/ValueSet/sds-job-role-name-1";
}

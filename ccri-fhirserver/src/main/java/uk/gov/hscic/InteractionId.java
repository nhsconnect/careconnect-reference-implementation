package uk.gov.hscic;

import java.util.Arrays;
import java.util.List;

public final class InteractionId {

    private InteractionId() { }

    private static final String BASE = "urn:nhs:names:services:gpconnect:fhir:";

    public static final String CLAIM_PATIENT_ALLERGY_INTOLERANCE       = BASE + "claim:patient/AllergyIntolerance.read";
    public static final String CLAIM_PATIENT_APPOINTMENT               = BASE + "claim:patient/Appointment.read";
    public static final String CLAIM_PATIENT_CONDITION                 = BASE + "claim:patient/Condition.read";
    public static final String CLAIM_PATIENT_DIAGNOSTIC_ORDER          = BASE + "claim:patient/DiagnosticOrder.read";
    public static final String CLAIM_PATIENT_DIAGNOSTIC_REPORT         = BASE + "claim:patient/DiagnosticReport.read";
    public static final String CLAIM_PATIENT_ENCOUNTER                 = BASE + "claim:patient/Encounter.read";
    public static final String CLAIM_PATIENT_FLAG                      = BASE + "claim:patient/Flag.read";
    public static final String CLAIM_PATIENT_IMMUNIZATION              = BASE + "claim:patient/Immunization.read";
    public static final String CLAIM_PATIENT_MEDICATION_ADMINISTRATION = BASE + "claim:patient/MedicationAdministration.read";
    public static final String CLAIM_PATIENT_MEDICATION_DISPENSE       = BASE + "claim:patient/MedicationDispense.read";
    public static final String CLAIM_PATIENT_MEDICATION_ORDER          = BASE + "claim:patient/MedicationOrder.read";
    public static final String CLAIM_PATIENT_OBSERVATION               = BASE + "claim:patient/Observation.read";
    public static final String CLAIM_PATIENT_PROBLEM                   = BASE + "claim:patient/Problem.read";
    public static final String CLAIM_PATIENT_PROCEDURES                = BASE + "claim:patient/Procedures.read";
    public static final String CLAIM_PATIENT_REFERRAL                  = BASE + "claim:patient/Referral.read";
    public static final String OPERATION_GPC_GET_CARE_RECORD           = BASE + "operation:gpc.getcarerecord";
    public static final String OPERATION_GPC_GET_SCHEDULE              = BASE + "operation:gpc.getschedule";
    public static final String OPERATION_GPC_REGISTER_PATIENT          = BASE + "operation:gpc.registerpatient";
    public static final String REST_CREATE_APPOINTMENT                 = BASE + "rest:create:appointment";
    public static final String REST_CREATE_ORDER                       = BASE + "rest:create:order";
    public static final String REST_READ_APPOINTMENT                   = BASE + "rest:read:appointment";
    public static final String REST_READ_LOCATION                      = BASE + "rest:read:location";
    public static final String REST_READ_METADATA                      = BASE + "rest:read:metadata";
    public static final String REST_READ_ORGANIZATION                  = BASE + "rest:read:organization";
    public static final String REST_READ_PATIENT                       = BASE + "rest:read:patient";
    public static final String REST_READ_PRACTITIONER                  = BASE + "rest:read:practitioner";
    public static final String REST_SEARCH_LOCATION                    = BASE + "rest:search:location";
    public static final String REST_SEARCH_ORGANIZATION                = BASE + "rest:search:organization";
    public static final String REST_SEARCH_PATIENT                     = BASE + "rest:search:patient";
    public static final String REST_SEARCH_PATIENT_APPOINTMENTS        = BASE + "rest:search:patient_appointments";
    public static final String REST_SEARCH_PRACTITIONER                = BASE + "rest:search:practitioner";
    public static final String REST_UPDATE_APPOINTMENT                 = BASE + "rest:update:appointment";

    public static final List<String> IDENTIFIER_INTERACTIONS = Arrays.asList(
            REST_SEARCH_LOCATION,
            REST_SEARCH_ORGANIZATION,
            REST_SEARCH_PATIENT,
            REST_SEARCH_PRACTITIONER);
}

package uk.nhs.careconnect.ri.provider.patient;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.SystemCode;
import uk.nhs.careconnect.ri.entity.patient.PatientSearch;
import uk.nhs.careconnect.ri.entity.patient.PatientStore;
import uk.nhs.careconnect.ri.model.patient.PatientDetails;
import uk.nhs.careconnect.ri.provider.organization.OrganizationResourceProvider;
import uk.nhs.careconnect.ri.provider.practitioner.PractitionerResourceProvider;
import uk.org.hl7.fhir.core.dstu2.CareConnectProfile;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class PatientResourceProvider implements IResourceProvider {
    private static final String TEMPORARY_RESIDENT_REGISTRATION_TYPE = "T";
    private static final String ACTIVE_REGISTRATION_STATUS = "A";
    private static final int ENCOUNTERS_SUMMARY_LIMIT = 3;

    private static final List<String> MANDATORY_PARAM_NAMES = Arrays.asList("patientNHSNumber", "recordSection");
    private static final List<String> PERMITTED_PARAM_NAMES = new ArrayList<String>(MANDATORY_PARAM_NAMES) {{
        add("timePeriod");
    }};

    @Autowired
    private PractitionerResourceProvider practitionerResourceProvider;

    @Autowired
    private OrganizationResourceProvider organizationResourceProvider;

    @Autowired
    private PatientStore patientStore;

    @Autowired
    private PatientSearch patientSearch;


    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }


    @Create()
    public MethodOutcome createPatient(HttpServletRequest theRequest, @ResourceParam Patient thePatient) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        return method;
    }

    @Read
    public Patient getPatientById(@IdParam IdType internalId) {
        PatientDetails patientDetails = patientSearch.findPatientByInternalID(internalId.getIdPart());

        if (patientDetails == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No patient details found for patient ID: " + internalId.getIdPart()),
                    SystemCode.PATIENT_NOT_FOUND, OperationOutcome.IssueType.NOTFOUND);
        }

        return patientDetailsToPatientResourceConverter(patientDetails);
    }

    @Search
    public List<Patient> getPatientsByPatientId(@RequiredParam(name = Patient.SP_IDENTIFIER) TokenParam tokenParam) {
        /* Not required... is this valid anyway???
        if (!CareConnectSystem.NHSNumber.equals(tokenParam.getSystem())) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                new InvalidRequestException("Invalid system code"),
                SystemCode.INVALID_PARAMETER, IssueTypeEnum.INVALID_CONTENT);
        }
        */
        Patient patient = getPatientByPatientId(tokenParam.getValue());

        return null == patient
                ? Collections.emptyList()
                : Collections.singletonList(patient);
    }

    private Patient getPatientByPatientId(String patientId) {
        PatientDetails patientDetails = patientSearch.findPatient(patientId);

        return null == patientDetails
                ? null
                : patientDetailsToPatientResourceConverter(patientDetails);
    }


    private PatientDetails registerPatientResourceConverterToPatientDetail(Patient patientResource) {
        PatientDetails patientDetails = new PatientDetails();
        HumanName name = patientResource.getName().get(0);
        patientDetails.setForename(name.getGiven().toString());
        patientDetails.setSurname(name.getFamily().toString());
        patientDetails.setDateOfBirth(patientResource.getBirthDate());
        patientDetails.setGender(patientResource.getGender().toCode());
        // TODO this won't work with next refactor
        patientDetails.setNhsNumber(patientResource.getIdentifier().get(0).getValue());

        /* TODO remove or replace when viable
        List<ExtensionDt> registrationPeriodExtensions = patientResource
                .getUndeclaredExtensionsByUrl(SystemURL.SD_EXTENSION_REGISTRATION_PERIOD);
        ExtensionDt registrationPeriodExtension = registrationPeriodExtensions.get(0);
        PeriodDt registrationPeriod = (PeriodDt) registrationPeriodExtension.getValue();

        Date registrationStart = registrationPeriod.getStart();

        if (registrationStart.compareTo(new Date()) <= 1) {
            patientDetails.setRegistrationStartDateTime(registrationStart);
        } else {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new UnprocessableEntityException("Patient record not found"),
                    SystemCode.INVALID_PARAMETER, IssueTypeEnum.NOT_FOUND);
        }

        Date registrationEnd = registrationPeriod.getEnd();

        if (registrationEnd != null) {
            throw new IllegalArgumentException(String.format(
                    "The given registration end (%c) is not valid. The registration end should be left blank to indicate an open-ended registration period.",
                    registrationStart));

        }
         */
/* TODO KGM removed extensions for refactor
        List<ExtensionDt> registrationStatusExtensions = patientResource
                .getUndeclaredExtensionsByUrl(SystemURL.SD_EXTENSION_REGISTRATION_STATUS);
        ExtensionDt registrationStatusExtension = registrationStatusExtensions.get(0);
        CodeableConcept registrationStatusCode = (CodeableConcept) registrationStatusExtension.getValue();
        String registrationStatus = registrationStatusCode.getCodingFirstRep().getCode();

        if (ACTIVE_REGISTRATION_STATUS.equals(registrationStatus)) {
            patientDetails.setRegistrationStatus(registrationStatus);
        } else {
            throw new IllegalArgumentException(String.format(
                    "The given registration status is not valid. Expected - A. Actual - %s", registrationStatus));
        }


        List<ExtensionDt> registrationTypeExtensions = patientResource
                .getUndeclaredExtensionsByUrl(SystemURL.SD_EXTENSION_REGISTRATION_TYPE);
        ExtensionDt registrationTypeExtension = registrationTypeExtensions.get(0);
        CodeableConcept registrationTypeCode = (CodeableConcept) registrationTypeExtension.getValue();
        String registrationType = registrationTypeCode.getCodingFirstRep().getCode();

        if (TEMPORARY_RESIDENT_REGISTRATION_TYPE.equals(registrationType)) {
            patientDetails.setRegistrationType(registrationType);
        } else {
            throw new IllegalArgumentException(String
                    .format("The given registration type is not valid. Expected - T. Actual - %s", registrationType));
        }
        */
        return patientDetails;
    }

    // a cut-down Patient
    private Patient patientDetailsToRegisterPatientResourceConverter(PatientDetails patientDetails) {
        Patient patient = new Patient()
                .addIdentifier(new Identifier().setSystem(CareConnectSystem.NHSNumber).setValue(patientDetails.getNhsNumber()))
                .setBirthDate(patientDetails.getDateOfBirth());
        try {

                patient.setGender(Enumerations.AdministrativeGender.fromCode(patientDetails.getGender().toLowerCase(Locale.UK)));
        } catch (Exception ex) {

        }
        patient.setId(patientDetails.getId());
        patient.addName().addFamily(patientDetails.getSurname()).addGiven(patientDetails.getForename()).setUse(HumanName.NameUse.USUAL);

        Period registrationPeriod = new Period()
                .setStart(patientDetails.getRegistrationStartDateTime())
                .setEnd(patientDetails.getRegistrationEndDateTime());
        /*

        TODO KGM Removed

        patient.addExtension(new Extension().setUrl(
                SystemURL.SD_EXTENSION_REGISTRATION_PERIOD).setValue(registrationPeriod));

        patient.addExtension(new Extension().setUrl(SystemURL.SD_EXTENSION_REGISTRATION_STATUS).setValue(new CodeableConcept(
                SystemURL.VS_REGISTRATION_STATUS, patientDetails.getRegistrationStatus()));

        patient.addExtension(new Extension().setUrl(SystemURL.SD_EXTENSION_REGISTRATION_TYPE, new CodeableConcept(
                SystemURL.VS_REGISTRATION_TYPE, patientDetails.getRegistrationType()));
        */
        return patient;
    }

    private Patient patientDetailsToPatientResourceConverter(PatientDetails patientDetails) {
        Patient patient = new Patient();
        patient.addIdentifier(new Identifier().setSystem(CareConnectSystem.NHSNumber).setValue(patientDetails.getNhsNumber()));

        Date lastUpdated = patientDetails.getLastUpdated();

        if (lastUpdated == null) {
            patient.setId(patientDetails.getId());
        } else {
            patient.setId(patientDetails.getId());
            // TODO , String.valueOf(lastUpdated.getTime() should be in meta?
            patient.getMeta()
                    .setLastUpdated(lastUpdated)
                    .setVersionId(String.valueOf(lastUpdated.getTime()));
        }

        patient.addName()
                .setText(patientDetails.getName())
                .addFamily(patientDetails.getSurname())
                .addGiven(patientDetails.getForename())
                .addPrefix(patientDetails.getTitle())
                .setUse(HumanName.NameUse.USUAL);

        patient.setBirthDate(patientDetails.getDateOfBirth());
        patient.getMeta().addProfile(CareConnectProfile.Patient_1);

        String addressLines = patientDetails.getAddress();

        if (addressLines != null) {
            patient.addAddress()
                    .setUse(Address.AddressUse.HOME)
                    .setType(Address.AddressType.PHYSICAL)
                    .setText(addressLines);
        }

        Long gpId = patientDetails.getGpId();

        if (gpId != null) {
            HumanName practitionerName = practitionerResourceProvider.getPractitionerById(new IdType(gpId)).getName();

            Reference practitionerReference = new Reference("Practitioner/" + gpId)
                    .setDisplay(practitionerName.getPrefix() + " " + practitionerName.getGiven() + " " + practitionerName.getFamily());

            patient.getCareProvider().add(practitionerReference);
        }

        String gender = patientDetails.getGender();
        if (gender != null) {
            // TODO BIG TIME
            switch (patientDetails.getGender())
            {
                case "female" :
                    patient.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                case "male" :
                    patient.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                default:
                    patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);

            }

        }

        String telephoneNumber = patientDetails.getTelephone();
        if (telephoneNumber != null) {
            ContactPoint telephone = new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(telephoneNumber)
                    .setUse(ContactPoint.ContactPointUse.HOME);

            patient.addTelecom(telephone);
        }
        // TODO
        /*
        Date registrationStartDateTime = patientDetails.getRegistrationStartDateTime();
        if (registrationStartDateTime != null) {
            Period registrationPeriod = new Period()
                    .setStart(registrationStartDateTime)
                    .setEnd(patientDetails.getRegistrationEndDateTime());

           patient.addUndeclaredExtension(false, SystemURL.SD_EXTENSION_REGISTRATION_PERIOD, registrationPeriod);
        }

        String registrationStatusValue = patientDetails.getRegistrationStatus();
        if (registrationStatusValue != null) {
            patient.addUndeclaredExtension(false, SystemURL.SD_EXTENSION_REGISTRATION_STATUS, new CodeableConceptDt(
                    SystemURL.VS_REGISTRATION_STATUS, registrationStatusValue));
        }

        String registrationTypeValue = patientDetails.getRegistrationType();
        if (registrationTypeValue != null) {
            patient.addUndeclaredExtension(false, SystemURL.SD_EXTENSION_REGISTRATION_TYPE, new CodeableConceptDt(
                    SystemURL.VS_REGISTRATION_TYPE, registrationTypeValue));
        }
        */
        return patient;
    }
}

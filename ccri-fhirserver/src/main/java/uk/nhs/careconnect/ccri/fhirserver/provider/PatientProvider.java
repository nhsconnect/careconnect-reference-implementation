package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.PatientRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@Component
public class PatientProvider implements ICCResourceProvider {


    @Autowired
    private PractitionerProvider practitionerResourceProvider;

    @Autowired
    private OrganizationProvider organizationResourceProvider;

    @Autowired
    private PatientRepository patientDao;

    @Autowired
    FhirContext ctx;

    @Override
    public Long count() {
        return patientDao.count();
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);


    @Read
    public Patient getPatientById(@IdParam IdType internalId) {
        Patient patient = patientDao.read(ctx,internalId);

        if (patient == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No patient details found for patient ID: " + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return patient;
    }

    @Update
    public MethodOutcome updatePatient(HttpServletRequest theRequest, @ResourceParam Patient patient, @IdParam IdType theId,@ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

        log.debug("Update Patient Provider called");

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        Patient newPatient = null;
        try {
            newPatient = patientDao.update(ctx, patient, theId, theConditional);
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }
        method.setId(newPatient.getIdElement());
        method.setResource(newPatient);


        log.debug("called update Patient method");

        return method;
    }

    @Create
    public MethodOutcome createPatient(HttpServletRequest theRequest, @ResourceParam Patient patient) {

        log.debug("Update Patient Provider called");

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        Patient newPatient = null;
        try {
            newPatient = patientDao.update(ctx, patient, null,null);
            method.setId(newPatient.getIdElement());
            method.setResource(newPatient);
        } catch (Exception ex) {

            if (ex instanceof OperationOutcomeException) {
                OperationOutcomeException outcomeException = (OperationOutcomeException) ex;
                method.setOperationOutcome(outcomeException.getOutcome());
                method.setCreated(false);
            } else {
                log.error(ex.getMessage());
                method.setCreated(false);
                method.setOperationOutcome(OperationOutcomeFactory.createOperationOutcome(ex.getMessage()));
            }
        }

        log.debug("called create Patient method");

        return method;
    }

    @Search
    public List<Resource> searchPatient(HttpServletRequest theRequest,

           @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
           @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
           @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
           @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
           @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
           @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
           @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
           @OptionalParam(name= Patient.SP_NAME) StringParam name,
           @OptionalParam(name= Patient.SP_PHONE) StringParam phone
            , @OptionalParam(name = Patient.SP_RES_ID) StringParam resid
            ,@IncludeParam(reverse=true, allow = {"*"}) Set<Include> reverseIncludes
            ,@IncludeParam(allow= {
                                        "Patient:general-practitioner"
                                        ,"Patient:organization"
                                        , "*"}) Set<Include> includes
                                       ) {



        return patientDao.search(ctx,addressPostcode, birthDate, email, familyName, gender,givenName, identifier, name, phone,resid,reverseIncludes,includes);

    }



}

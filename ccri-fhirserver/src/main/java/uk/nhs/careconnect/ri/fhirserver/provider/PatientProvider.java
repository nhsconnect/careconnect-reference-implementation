package uk.nhs.careconnect.ri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.daointerface.PatientRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        Patient newPatient = patientDao.update(ctx, patient, theId, theConditional);
        method.setId(newPatient.getIdElement());
        method.setResource(newPatient);


        return method;
    }

    @Search
    public List<Patient> searchPatient(HttpServletRequest theRequest,

                                       @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
                                       @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
                                       @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
                                       @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                                       @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                                       @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                                       @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                                       @OptionalParam(name= Patient.SP_NAME) StringParam name,
                                       @OptionalParam(name= Patient.SP_PHONE) StringParam phone
                                       ) {



        return patientDao.search(ctx,addressPostcode, birthDate, email, familyName, gender,givenName, identifier, name, phone);

    }



}

package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.PractitionerRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class PractitionerProvider implements ICCResourceProvider {

    @Autowired
    private PractitionerRepository practitionerDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<Practitioner> getResourceType() {
        return Practitioner.class;
    }

    @Override
    public Long count() {
        return practitionerDao.count();
    }

    @Update
    public MethodOutcome updatePractitioner(HttpServletRequest theRequest, @ResourceParam Practitioner practitioner, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
        Practitioner newPractitioner = practitionerDao.create(ctx,practitioner, theId, theConditional);
        method.setId(newPractitioner.getIdElement());
        method.setResource(newPractitioner);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }



        return method;
    }

    @Create
    public MethodOutcome createPractitioner(HttpServletRequest theRequest, @ResourceParam Practitioner practitioner) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
        Practitioner newPractitioner = practitionerDao.create(ctx,practitioner, null, null);
        method.setId(newPractitioner.getIdElement());
        method.setResource(newPractitioner);
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


        return method;
    }

    @Read
    public Practitioner getPractitioner
            (@IdParam IdType internalId) {
        Practitioner practitioner = practitionerDao.read(ctx, internalId);

        if ( practitioner == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Patient/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return practitioner;
    }

    @Search
    public List<Practitioner> searchPractitioner(HttpServletRequest theRequest,
                                                                  @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
                                                                  @OptionalParam(name = Practitioner.SP_NAME) StringParam name,
                                                 @OptionalParam(name = Practitioner.SP_ADDRESS_POSTALCODE) StringParam postCode
            , @OptionalParam(name = Procedure.SP_RES_ID) StringParam resid
    ) {
        return practitionerDao.searchPractitioner(ctx, identifier, name ,postCode,resid);
    }


}

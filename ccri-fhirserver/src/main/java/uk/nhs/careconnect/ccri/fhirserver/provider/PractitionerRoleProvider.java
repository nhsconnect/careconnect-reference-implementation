package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.PractitionerRoleRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class PractitionerRoleProvider implements ICCResourceProvider {

    @Autowired
    private PractitionerRoleRepository practitionerRoleDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return practitionerRoleDao.count();
    }

    @Override
    public Class<PractitionerRole> getResourceType() {
        return PractitionerRole.class;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam PractitionerRole practitionerRole) {

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        PractitionerRole newPractitioner = practitionerRoleDao.create(ctx, practitionerRole,null,null);
        method.setId(newPractitioner.getIdElement());
        method.setResource(newPractitioner);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }
    @Update
    public MethodOutcome updatePractitioner(HttpServletRequest theRequest, @ResourceParam PractitionerRole practitionerRole, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            PractitionerRole newPractitioner = practitionerRoleDao.create(ctx, practitionerRole, theId, theConditional);
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
    public PractitionerRole getPractitionerRole
            (@IdParam IdType internalId) {
        PractitionerRole practitionerRole = practitionerRoleDao.read(ctx, internalId);

        if ( practitionerRole == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No PractitionerRole/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return practitionerRole;
    }

    @Search
    public List<PractitionerRole> searchPractitioner(HttpServletRequest theRequest,
                                                     @OptionalParam(name = PractitionerRole.SP_IDENTIFIER) TokenParam identifier,
                                                     @OptionalParam(name = PractitionerRole.SP_PRACTITIONER) ReferenceParam practitioner,
                                                     @OptionalParam(name = PractitionerRole.SP_ORGANIZATION) ReferenceParam organisation
            , @OptionalParam(name = PractitionerRole.SP_RES_ID) StringParam resid) {

        return practitionerRoleDao.search(ctx,
                identifier
                ,practitioner
                ,organisation
                ,resid
        );
    }


}

package uk.nhs.careconnect.ri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.daointerface.PractitionerRoleRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class PractitionerRoleProvider implements IResourceProvider {

    @Autowired
    private PractitionerRoleRepository practitionerRoleDao;

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


        PractitionerRole newPractitioner = practitionerRoleDao.create(practitionerRole,null,null);
        method.setId(newPractitioner.getIdElement());
        method.setResource(newPractitioner);

        return method;
    }
    @Update
    public MethodOutcome updatePractitioner(HttpServletRequest theRequest, @ResourceParam PractitionerRole practitionerRole, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        PractitionerRole newPractitioner = practitionerRoleDao.create(practitionerRole, theId, theConditional);
        method.setId(newPractitioner.getIdElement());
        method.setResource(newPractitioner);



        return method;
    }
    @Read
    public PractitionerRole getPractitionerRole
            (@IdParam IdType internalId) {
        PractitionerRole practitionerRole = practitionerRoleDao.read(internalId);

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
                                                     @OptionalParam(name = PractitionerRole.SP_ORGANIZATION) ReferenceParam organisation) {
        return practitionerRoleDao.search(identifier,practitioner,organisation);
    }


}

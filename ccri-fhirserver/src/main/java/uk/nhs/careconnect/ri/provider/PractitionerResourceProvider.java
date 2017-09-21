package uk.nhs.careconnect.ri.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.dao.Practitioner.PractitionerRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class PractitionerResourceProvider  implements IResourceProvider {

    @Autowired
    private PractitionerRepository practitionerDao;

    @Override
    public Class<Practitioner> getResourceType() {
        return Practitioner.class;
    }


    @Update
    public MethodOutcome updatePractitioner(HttpServletRequest theRequest, @ResourceParam Practitioner practitioner, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        Practitioner newPractitioner = practitionerDao.create(practitioner, theId, theConditional);
        method.setId(newPractitioner.getIdElement());
        method.setResource(newPractitioner);



        return method;
    }
    @Read
    public Practitioner getPractitioner
            (@IdParam IdType internalId) {
        Practitioner practitioner = practitionerDao.read(internalId);

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
                                                                  @OptionalParam(name = Practitioner.SP_NAME) StringParam name) {
        return practitionerDao.searchPractitioner(identifier,name);
    }


}

package uk.nhs.careconnect.ri.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
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

import java.util.List;

@Component
public class PractitionerResourceProvider  implements IResourceProvider {

    @Autowired
    private PractitionerRepository practitionerDao;

    @Override
    public Class<Practitioner> getResourceType() {
        return Practitioner.class;
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
    public List<Practitioner> getPractitionerByPractitionerUserId(@RequiredParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier) {
        return practitionerDao.searchPractitioner(identifier);
    }


}

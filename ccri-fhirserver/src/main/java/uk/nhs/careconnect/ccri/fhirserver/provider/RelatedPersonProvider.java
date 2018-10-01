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
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.RelatedPersonRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class RelatedPersonProvider implements ICCResourceProvider {


    @Autowired
    private RelatedPersonRepository personDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return RelatedPerson.class;
    }

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return personDao.count();
    }

    @Update
    public MethodOutcome updateRelatedPerson(HttpServletRequest theRequest, @ResourceParam RelatedPerson person, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

    try {
        RelatedPerson newRelatedPerson = personDao.update(ctx, person, theId, theConditional);
        method.setId(newRelatedPerson.getIdElement());
        method.setResource(newRelatedPerson);

    } catch (Exception ex) {

        ProviderResponseLibrary.handleException(method,ex);
    }


        return method;
    }

    @Create
    public MethodOutcome createRelatedPerson(HttpServletRequest theRequest, @ResourceParam RelatedPerson person) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
        RelatedPerson newRelatedPerson = personDao.update(ctx, person, null,null);
        method.setId(newRelatedPerson.getIdElement());
        method.setResource(newRelatedPerson);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

   

    @Read()
    public RelatedPerson getRelatedPerson(@IdParam IdType personId) {

        RelatedPerson person = personDao.read(ctx,personId);

        if ( person == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No RelatedPerson/ " + personId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return person;
    }

    @Search
    public List<Resource> searchRelatedPerson(HttpServletRequest theRequest,
                                              @OptionalParam(name = RelatedPerson.SP_IDENTIFIER) TokenParam identifier,
                                              @OptionalParam(name = RelatedPerson.SP_PATIENT) ReferenceParam patient,
                                              @OptionalParam(name = RelatedPerson.SP_RES_ID) StringParam resid
    ) {
        return personDao.search(ctx, identifier,patient,resid);
    }

}

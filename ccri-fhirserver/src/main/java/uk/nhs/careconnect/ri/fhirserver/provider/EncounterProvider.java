package uk.nhs.careconnect.ri.fhirserver.provider;


import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.common.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.daointerface.EncounterRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class EncounterProvider implements IResourceProvider {


    @Autowired
    private EncounterRepository encounterDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Encounter.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Encounter encounter, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);


        Encounter newEncounter = encounterDao.create(encounter, theId, theConditional);
        method.setId(newEncounter.getIdElement());
        method.setResource(newEncounter);



        return method;
    }

    @Search
    public List<Encounter> search(HttpServletRequest theRequest,
                                           @OptionalParam(name = Encounter.SP_PATIENT) ReferenceParam patient
            ,@OptionalParam(name = Encounter.SP_DATE) DateRangeParam date
            ,@OptionalParam(name = Encounter.SP_EPISODEOFCARE) ReferenceParam episode) {
        return encounterDao.search(patient,date,episode);
    }

    @Read()
    public Encounter get(@IdParam IdType encounterId) {

        Encounter encounter = encounterDao.read(encounterId);

        if ( encounter == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Encounter/ " + encounterId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return encounter;
    }


}

package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.EncounterRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@Component
public class EncounterProvider implements ICCResourceProvider {


    @Autowired
    @Lazy
    private EncounterRepository encounterDao;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    private static final Logger log = LoggerFactory.getLogger(EncounterProvider.class);
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Encounter.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Encounter encounter, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {

    	resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Encounter newEncounter = encounterDao.create(ctx, encounter, theId, theConditional);
            method.setId(newEncounter.getIdElement());
            method.setResource(newEncounter);
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Encounter encounter) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Encounter newEncounter = encounterDao.create(ctx, encounter, null, null);
            method.setId(newEncounter.getIdElement());
            method.setResource(newEncounter);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<Resource> search(HttpServletRequest theRequest,
                                 @OptionalParam(name = Encounter.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Encounter.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Encounter.SP_EPISODEOFCARE) ReferenceParam episode
            , @OptionalParam(name = Encounter.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Encounter.SP_RES_ID) StringParam resid
            , @IncludeParam(reverse=true, allow = {
            "Observation:context",
            "Encounter:part-of",
            "Procedure:context",
            "Condition:context",
            "MedicationRequest:context",
            "Immunization:encounter" ,
            "DocumentReference:context",
            "Composition:encounter",
            "ReferralRequest:encounter",
            "*"
    }) Set<Include> reverseIncludes
            , @IncludeParam(allow = { "Encounter:participant" , "Encounter:patient" ,"Encounter:service-provider", "Encounter:location", "*"
    }) Set<Include> includes
            , @OptionalParam(name = Encounter.SP_TYPE) TokenParam type
            , @OptionalParam(name = Encounter.SP_STATUS) TokenParam status
    ) {
        return encounterDao.search(ctx,patient,date,episode,identifier,resid,reverseIncludes, includes, type, status);
    }

    @Read()
    public Encounter get(@IdParam IdType encounterId) {
    	resourcePermissionProvider.checkPermission("read");
        Encounter encounter = encounterDao.read(ctx,encounterId);

        if ( encounter == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Encounter/ " + encounterId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return encounter;
    }


    @Override
    public Long count() {
        return encounterDao.count();
    }
    
    
    @Validate
    public MethodOutcome testResource(@ResourceParam Encounter resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}

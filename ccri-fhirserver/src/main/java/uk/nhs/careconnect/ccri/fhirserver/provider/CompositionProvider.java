package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.CompositionRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class CompositionProvider implements ICCResourceProvider {

	@Autowired
    private CompositionRepository compositionDao;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    @Autowired
    private ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return compositionDao.count();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Composition.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Composition composition, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {
    	resourcePermissionProvider.checkPermission("update");
    	
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        try {
            Composition newComposition = compositionDao.create(ctx,composition, theId, theConditional);
            method.setId(newComposition.getIdElement());
            method.setResource(newComposition);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Composition composition) {

    	resourcePermissionProvider.checkPermission("create");
    	
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();
        method.setOperationOutcome(opOutcome);
        try {
            Composition newComposition = compositionDao.create(ctx,composition, null,null);
            method.setId(newComposition.getIdElement());
            method.setResource(newComposition);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Search
    public List<Composition> search(HttpServletRequest theRequest,
                                  @OptionalParam(name = Composition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Composition.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Composition.SP_RES_ID) StringParam resid
            , @OptionalParam(name = Composition.SP_TYPE) TokenParam type
            , @OptionalParam(name = Composition.SP_PERIOD) DateRangeParam date
                                  ) {
        return compositionDao.search(ctx,patient,identifier,resid,type,date);
    }

    @Read()
    public Composition get(@IdParam IdType compositionId) {
    	resourcePermissionProvider.checkPermission("read");
    	
        Composition composition = compositionDao.read(ctx,compositionId);

        if ( composition == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Composition/ " + compositionId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return composition;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam Composition resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}

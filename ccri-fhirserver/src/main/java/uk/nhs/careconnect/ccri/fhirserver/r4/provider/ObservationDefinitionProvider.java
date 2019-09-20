package uk.nhs.careconnect.ccri.fhirserver.r4.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ObservationDefinition;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.ICCResourceProvider;
import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.ResourcePermissionProvider;
import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.ResourceTestProvider;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.ObservationDefinitionRepository;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ObservationDefinitionProvider implements ICCResourceProvider {

	@Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
   
	@Autowired
    private ResourceTestProvider resourceTestProvider;

    @Override
    public Class<ObservationDefinition> getResourceType() {
        return ObservationDefinition.class;
    }

    @Autowired
    private ObservationDefinitionRepository observationDefinitionDao;

    @Qualifier("r4ctx")
    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(ObservationDefinitionProvider.class);


    @Override
    public Long count() {
        return observationDefinitionDao.count();
    }

    @Search
    public List<ObservationDefinition> search(HttpServletRequest theRequest,
                                              @OptionalParam(name = "category") TokenParam category,
                                              @OptionalParam(name = "code") TokenOrListParam code
            , @OptionalParam(name = "identifier") TokenParam identifier
            , @OptionalParam(name = "name") StringParam name
            , @OptionalParam(name = ObservationDefinition.SP_RES_ID) StringParam id
    ) {
        return observationDefinitionDao.search(ctx,category,code,identifier,name, id);
    }

    @Read
    public ObservationDefinition get
            (@IdParam IdType internalId) {
        resourcePermissionProvider.checkPermission("read");
        ObservationDefinition observationDefinition = observationDefinitionDao.read( ctx, internalId);

        if ( observationDefinition == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No ObservationDefinition/" + internalId.getIdPart()),
                     OperationOutcome.IssueType.NOTFOUND);
        }

        return observationDefinition;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam ObservationDefinition observationDefinition)

    {
        log.info("create method is called");
        resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();

        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            ObservationDefinition newObservationDefinition = observationDefinitionDao.create(ctx,observationDefinition, null);
            method.setCreated(true);
            method.setId(newObservationDefinition.getIdElement());
            method.setResource(newObservationDefinition);
        } catch (Exception ex) {
            log.info(ex.getMessage());
            ProviderResponseLibrary.handleException(method,ex);
        }
        return method;
    }

    @Update()
    public MethodOutcome update(HttpServletRequest theRequest,@ResourceParam  ObservationDefinition observationDefinition) throws OperationOutcomeException {
        log.info("update method is called");
        resourcePermissionProvider.checkPermission("update");
        MethodOutcome method = new MethodOutcome();

        try {
            ObservationDefinition newObservationDefinition = observationDefinitionDao.create(ctx,observationDefinition, null);
            method.setCreated(false);
            method.setId(newObservationDefinition.getIdElement());
            method.setResource(newObservationDefinition);
        } catch (Exception ex) {
            log.info(ex.getMessage());
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Delete
    public MethodOutcome delete
            (@IdParam IdType internalId) {
        resourcePermissionProvider.checkPermission("delete");
        MethodOutcome method = new MethodOutcome();

        try {
            OperationOutcome outcome = observationDefinitionDao.delete(ctx,internalId);
            method.setCreated(false);

            method.setResource(outcome);
        } catch (Exception ex) {
            log.info(ex.getMessage());
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam ObservationDefinition resource,
                                      @Validate.Mode ValidationModeEnum theMode,
                                      @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }



}

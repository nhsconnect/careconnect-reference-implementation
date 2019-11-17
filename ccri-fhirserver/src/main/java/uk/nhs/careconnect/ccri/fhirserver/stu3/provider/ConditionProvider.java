package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.ConditionRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class ConditionProvider implements ICCResourceProvider {


    @Autowired
    private ConditionRepository conditionDao;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;

    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;
    
    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return conditionDao.count();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Condition.class;
    }


    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Condition condition, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {
    	resourcePermissionProvider.checkPermission("update");

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Condition newCondition = conditionDao.create(ctx, condition, theId, theConditional);
            method.setId(newCondition.getIdElement());
            method.setResource(newCondition);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }
    /*
    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Condition condition, @IdParam IdType theId) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        Condition newCondition = conditionDao.create(ctx,condition, theId, null);
        method.setId(newCondition.getIdElement());
        method.setResource(newCondition);



        return method;
    }
    */

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Condition condition) {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
        Condition newCondition = conditionDao.create(ctx,condition, null,null);
        method.setId(newCondition.getIdElement());
        method.setResource(newCondition);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Search
    public List<Condition> search(HttpServletRequest theRequest,
                                  @OptionalParam(name = Condition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Condition.SP_CATEGORY) TokenParam category
            , @OptionalParam(name = Condition.SP_CLINICAL_STATUS) TokenParam clinicalstatus
            , @OptionalParam(name = Condition.SP_ASSERTED_DATE) DateRangeParam asserted
            , @OptionalParam(name = Condition.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Condition.SP_RES_ID) StringParam resid
                                  ) {
        return conditionDao.search(ctx,patient, category, clinicalstatus, asserted, identifier,resid);
    }

    @Read()
    public Condition get(@IdParam IdType conditionId) {

    	resourcePermissionProvider.checkPermission("read");
        Condition condition = conditionDao.read(ctx,conditionId);

        if ( condition == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Condition/ " + conditionId.getIdPart()),
                     OperationOutcome.IssueType.NOTFOUND);
        }

        return condition;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam Condition resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @OptionalParam(name = "profile") @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}

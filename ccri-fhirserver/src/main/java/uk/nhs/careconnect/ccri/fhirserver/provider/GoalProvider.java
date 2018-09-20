package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Goal;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.daointerface.GoalRepository;
import uk.nhs.careconnect.ri.lib.server.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.lib.server.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class GoalProvider implements ICCResourceProvider {

    @Autowired
    private GoalRepository goalDao;

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Goal.class;
    }

        @Override
        public Long count() {
        return goalDao.count();
    }
    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Goal goal, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Goal newGoal = goalDao.create(ctx,goal, theId, theConditional);
            method.setId(newGoal.getIdElement());
            method.setResource(newGoal);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }




        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Goal goal) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Goal newGoal = goalDao.create(ctx,goal, null,null);
            method.setId(newGoal.getIdElement());
            method.setResource(newGoal);
        } catch (Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }


        return method;
    }

    @Search
    public List<Goal> search(HttpServletRequest theRequest,
                                 @OptionalParam(name = Goal.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Goal.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Goal.SP_RES_ID) StringParam id
    ) {
        return goalDao.search(ctx,patient, identifier,id);
    }

    @Read()
    public Goal get(@IdParam IdType goalId) {

        Goal goal = goalDao.read(ctx,goalId);

        if ( goal == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Goal/ " + goalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return goal;
    }


}

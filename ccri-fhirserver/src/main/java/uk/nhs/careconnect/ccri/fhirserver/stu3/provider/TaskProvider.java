package uk.nhs.careconnect.ccri.fhirserver.stu3.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.Task;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ccri.fhirserver.support.OperationOutcomeFactory;
import uk.nhs.careconnect.ccri.fhirserver.support.ProviderResponseLibrary;
import uk.nhs.careconnect.ri.database.daointerface.TaskRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class TaskProvider implements ICCResourceProvider {

    @Autowired
    private TaskRepository taskDao;
    
    @Autowired
    private ResourcePermissionProvider resourcePermissionProvider;

    @Autowired
    FhirContext ctx;
    
    @Autowired
    private ResourceTestProvider resourceTestProvider;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Task.class;
    }

        @Override
        public Long count() {
        return taskDao.count();
    }
    @Update
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam Task task, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {
    	
    	resourcePermissionProvider.checkPermission("update");
    	
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Task newTask = taskDao.create(ctx,task, theId, theConditional);
            method.setId(newTask.getIdElement());
            method.setResource(newTask);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Task task)  {

    	resourcePermissionProvider.checkPermission("create");
        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Task newTask = taskDao.create(ctx,task, null,null);
            method.setId(newTask.getIdElement());
            method.setResource(newTask);
        } catch (BaseServerResponseException srv) {
            // HAPI Exceptions pass through
            throw srv;
        } catch(Exception ex) {
            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Search
    public List<Resource> search(HttpServletRequest theRequest,
                                 @OptionalParam(name = Task.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Task.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Task.SP_RES_ID) StringParam id
            , @OptionalParam(name = Task.SP_OWNER) ReferenceParam owner
            , @OptionalParam(name = Task.SP_REQUESTER) ReferenceParam requester
            , @OptionalParam(name = Task.SP_STATUS) TokenParam status
            , @OptionalParam(name = Task.SP_CODE) TokenParam code
    ) {
        return taskDao.search(ctx,patient, identifier,id, owner, requester,status, code);
    }

    @Read()
    public Task get(@IdParam IdType taskId) {

    	resourcePermissionProvider.checkPermission("read");
        Task task = taskDao.read(ctx,taskId);

        if ( task == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Task/ " + taskId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return task;
    }

    @Validate
    public MethodOutcome testResource(@ResourceParam Task resource,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        return resourceTestProvider.testResource(resource,theMode,theProfile);
    }
}

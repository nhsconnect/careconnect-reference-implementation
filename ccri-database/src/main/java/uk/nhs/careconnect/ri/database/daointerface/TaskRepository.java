package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Task;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.task.TaskEntity;

import java.util.List;


public interface TaskRepository extends BaseRepository<TaskEntity,Task> {
    void save(FhirContext ctx, TaskEntity task) throws OperationOutcomeException;

    Task read(FhirContext ctx, IdType theId);

    TaskEntity readEntity(FhirContext ctx, IdType theId);

    Task create(FhirContext ctx, Task task, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;


    List<Resource> search(
            FhirContext ctx,
            @OptionalParam(name = Task.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Task.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Task.SP_RES_ID) StringParam id
            , @OptionalParam(name = Task.SP_OWNER) ReferenceParam owner
            , @OptionalParam(name = Task.SP_REQUESTER) ReferenceParam requester
            , @OptionalParam(name = Task.SP_STATUS) TokenParam status

    );

    List<TaskEntity> searchEntity(FhirContext ctx,
                                   @OptionalParam(name = Task.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Task.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Task.SP_RES_ID) StringParam id
            , @OptionalParam(name = Task.SP_OWNER) ReferenceParam owner
            , @OptionalParam(name = Task.SP_REQUESTER) ReferenceParam requester
            , @OptionalParam(name = Task.SP_STATUS) TokenParam status
    );
}

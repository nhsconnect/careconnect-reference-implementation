package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Task;

import java.util.List;

public interface ITask {

    Task read(FhirContext ctx, IdType internalId);

    Task create(FhirContext ctx, Task task);

    List<Task> search(FhirContext ctx, ReferenceParam patient) throws Exception;

}

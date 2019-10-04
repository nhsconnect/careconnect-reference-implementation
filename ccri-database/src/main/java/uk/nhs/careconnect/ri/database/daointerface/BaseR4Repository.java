package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.IBaseResource;


public interface BaseR4Repository<R extends IBaseResource,F extends DomainResource> {

    Long count();

     R readEntity(FhirContext ctx, IdType theId);

    void save(FhirContext ctx, R resource) throws OperationOutcomeException;

     F read(FhirContext ctx, IdType theId);
}

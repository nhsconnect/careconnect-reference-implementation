package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.IBaseResource;


public interface BaseDao<R extends IBaseResource,F extends DomainResource> {

    Long count();

     R readEntity(FhirContext ctx, IdType theId);

    void save(FhirContext ctx, R resource);

     F read(FhirContext ctx, IdType theId);
}

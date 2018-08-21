package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.hl7.fhir.dstu3.model.Resource;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.entity.relatedPerson.RelatedPersonEntity;

import java.util.List;
import java.util.Set;


public interface RelatedPersonRepository extends BaseRepository<RelatedPersonEntity,RelatedPerson> {

    void save(FhirContext ctx, RelatedPersonEntity person) throws OperationOutcomeException;

    RelatedPerson read(FhirContext ctx, IdType theId);

    RelatedPersonEntity readEntity(FhirContext ctx, IdType theId);

    RelatedPerson update(FhirContext ctx, RelatedPerson person, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<Resource> search(FhirContext ctx,
                          @OptionalParam(name = RelatedPerson.SP_IDENTIFIER) TokenParam identifier,
                          @OptionalParam(name = RelatedPerson.SP_PATIENT) ReferenceParam patient,
                          @OptionalParam(name = RelatedPerson.SP_RES_ID) TokenParam id
            );

    List<RelatedPersonEntity> searchEntity(FhirContext ctx,
                                           @OptionalParam(name = RelatedPerson.SP_IDENTIFIER) TokenParam identifier,
                                           @OptionalParam(name = RelatedPerson.SP_PATIENT) ReferenceParam patient,
                                           @OptionalParam(name = RelatedPerson.SP_RES_ID) TokenParam id);

}

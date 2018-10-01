package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ListResource;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.list.ListEntity;

import java.util.List;

public interface ListRepository extends BaseRepository<ListEntity,ListResource> {

    void save(FhirContext ctx, ListEntity list) throws OperationOutcomeException;

    ListResource read(FhirContext ctx, IdType theId);

    ListEntity readEntity(FhirContext ctx, IdType theId);

    ListResource create(FhirContext ctx, ListResource questionnaire, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;
    List<ListResource> searchListResource(FhirContext ctx,

                                                            @OptionalParam(name = ListResource.SP_IDENTIFIER) TokenParam identifier,
                                                            @OptionalParam(name = ListResource.SP_RES_ID) StringParam id,
                                                            @OptionalParam(name = ListResource.SP_PATIENT) ReferenceParam patient

    );

    List<ListEntity> searchListEntity(FhirContext ctx,
                                                                        @OptionalParam(name = ListResource.SP_IDENTIFIER) TokenParam identifier,
                                                                        @OptionalParam(name = ListResource.SP_RES_ID) StringParam id,
                                                                        @OptionalParam(name = ListResource.SP_PATIENT) ReferenceParam patient
    );
}

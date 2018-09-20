package uk.nhs.careconnect.ri.database.daointerface;

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
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;

import java.util.List;
import java.util.Set;

public interface EncounterRepository  extends BaseRepository<EncounterEntity,Encounter> {
    void save(FhirContext ctx,EncounterEntity encounter) throws OperationOutcomeException;

    Encounter read(FhirContext ctx, IdType theId);

    EncounterEntity readEntity(FhirContext ctx, IdType theId);

    Encounter create(FhirContext ctx,Encounter encounter, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<Resource> search(FhirContext ctx,

                          @OptionalParam(name = Encounter.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Encounter.SP_DATE) DateRangeParam date
            , @OptionalParam(name = Encounter.SP_EPISODEOFCARE) ReferenceParam episode
            , @OptionalParam(name = Encounter.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name= Encounter.SP_RES_ID) StringParam id
            , @IncludeParam(reverse=true, allow = {"*"}) Set<Include> reverseIncludes
            , @IncludeParam(allow = { "Encounter.participant" , "Encounter.service-provider", "Encounter.location", "*"
    }) Set<Include> includes

    );

    List<EncounterEntity> searchEntity(FhirContext ctx,
            @OptionalParam(name = Encounter.SP_PATIENT) ReferenceParam patient
            ,@OptionalParam(name = Encounter.SP_DATE) DateRangeParam date
            ,@OptionalParam(name = Encounter.SP_EPISODEOFCARE) ReferenceParam episode
            , @OptionalParam(name = Encounter.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= Encounter.SP_RES_ID) StringParam id
            , @IncludeParam(reverse=true, allow = {"*"}) Set<Include> reverseIncludes
            , @IncludeParam(allow = { "Encounter.participant" , "Encounter.subject", "Encounter.service-provider", "Encounter.location", "*"
    }) Set<Include> includes

    );
}

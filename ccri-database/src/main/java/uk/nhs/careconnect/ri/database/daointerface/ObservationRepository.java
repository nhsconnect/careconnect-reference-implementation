package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;

import java.util.List;

public interface ObservationRepository extends BaseRepository<ObservationEntity,Observation> {

    Observation save(FhirContext ctx, Observation observation, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    Observation read(FhirContext ctx, IdType theId);

    ObservationEntity readEntity(FhirContext ctx, IdType theId );


    List<Observation> search (FhirContext ctx,
            @OptionalParam(name= Observation.SP_CATEGORY) TokenParam category,
            @OptionalParam(name= Observation.SP_CODE) TokenOrListParam codes,
            @OptionalParam(name= Observation.SP_DATE) DateRangeParam effectiveDate,
            @OptionalParam(name = Observation.SP_PATIENT) ReferenceParam patient
            ,@OptionalParam(name = Observation.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= Observation.SP_RES_ID) StringParam id
            ,@OptionalParam(name = Observation.SP_SUBJECT) ReferenceParam subject
            );

    List<ObservationEntity> searchEntity (FhirContext ctx,
                              @OptionalParam(name= Observation.SP_CATEGORY) TokenParam category,
                              @OptionalParam(name= Observation.SP_CODE) TokenOrListParam codes,
                              @OptionalParam(name= Observation.SP_DATE) DateRangeParam effectiveDate,
                              @OptionalParam(name = Observation.SP_PATIENT) ReferenceParam patient

            ,@OptionalParam(name = Observation.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= Observation.SP_RES_ID) StringParam id
            ,@OptionalParam(name = Observation.SP_SUBJECT) ReferenceParam subject
    );
}

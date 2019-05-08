package uk.nhs.careconnect.ri.database.r4interface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ObservationDefinition;
import org.hl7.fhir.r4.model.Resource;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.observationDefinition.ObservationDefinitionEntity;
import java.util.List;

public interface ObservationDefinitionRepository extends BaseRepository<ObservationDefinitionEntity, ObservationDefinition> {

    ObservationDefinition create(FhirContext ctx, ObservationDefinition observation, @IdParam IdType theId) throws OperationOutcomeException;

    ObservationDefinition read(FhirContext ctx, IdType theId);

    ObservationDefinitionEntity readEntity(FhirContext ctx, IdType theId);

    OperationOutcome delete(FhirContext ctx, IdType theId) ;

    List<ObservationDefinition> search(FhirContext ctx,
                          @OptionalParam(name = "category") TokenParam category,
                          @OptionalParam(name = "code") TokenOrListParam codes
            , @OptionalParam(name = "identifier") TokenParam identifier
            , @OptionalParam(name = ObservationDefinition.SP_RES_ID) StringParam id

    );

    List<ObservationDefinitionEntity> searchEntity(FhirContext ctx,
                                         @OptionalParam(name = "category") TokenParam category,
                                         @OptionalParam(name = "code") TokenOrListParam codes
            , @OptionalParam(name = "identifier") TokenParam identifier
            , @OptionalParam(name = ObservationDefinition.SP_RES_ID) StringParam id
    );
}

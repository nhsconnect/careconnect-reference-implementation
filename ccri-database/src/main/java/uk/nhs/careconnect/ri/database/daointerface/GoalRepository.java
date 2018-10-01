package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Goal;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;

import uk.nhs.careconnect.ri.database.entity.goal.GoalEntity;

import java.util.List;

public interface GoalRepository extends BaseRepository<GoalEntity,Goal> {
    void save(FhirContext ctx, GoalEntity team) throws OperationOutcomeException;

    Goal read(FhirContext ctx, IdType theId);

    Goal create(FhirContext ctx, Goal goal, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    GoalEntity readEntity(FhirContext ctx, IdType theId);

    List<Goal> search(FhirContext ctx,
                          @OptionalParam(name = Goal.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Goal.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Goal.SP_RES_ID) StringParam id
    );

    List<GoalEntity> searchEntity(FhirContext ctx,
                                      @OptionalParam(name = Goal.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Goal.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Goal.SP_RES_ID) StringParam id
    );
}

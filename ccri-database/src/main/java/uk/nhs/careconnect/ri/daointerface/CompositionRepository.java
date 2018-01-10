package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.ri.entity.composition.CompositionEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;

import java.util.List;

public interface CompositionRepository extends BaseDao<CompositionEntity,Composition> {
    void save(FhirContext ctx, CompositionEntity composition);

    Composition read(FhirContext ctx, IdType theId);

    CompositionEntity readEntity(FhirContext ctx, IdType theId);

    Composition create(FhirContext ctx, Composition composition, @IdParam IdType theId, @ConditionalUrlParam String theConditional);

    List<Composition> search(FhirContext ctx,

              @OptionalParam(name = Condition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Composition.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Composition.SP_RES_ID) TokenParam id

    );

    List<CompositionEntity> searchEntity(FhirContext ctx,
              @OptionalParam(name = Composition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Condition.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Condition.SP_RES_ID) TokenParam id
    );
}

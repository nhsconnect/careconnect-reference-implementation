package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;

import java.util.List;

public interface ConditionRepository extends BaseRepository<ConditionEntity,Condition> {
    void save(FhirContext ctx,ConditionEntity condition) throws OperationOutcomeException;

    Condition read(FhirContext ctx, IdType theId);

    ConditionEntity readEntity(FhirContext ctx, IdType theId);

    Condition create(FhirContext ctx,Condition condition, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<Condition> search(FhirContext ctx,

            @OptionalParam(name = Condition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Condition.SP_CATEGORY) TokenParam category
            , @OptionalParam(name = Condition.SP_CLINICAL_STATUS) TokenParam clinicalstatus
            , @OptionalParam(name = Condition.SP_ASSERTED_DATE) DateRangeParam asserted
            , @OptionalParam(name = Condition.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= Condition.SP_RES_ID) StringParam id

    );

    List<ConditionEntity> searchEntity(FhirContext ctx,
            @OptionalParam(name = Condition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Condition.SP_CATEGORY) TokenParam category
            , @OptionalParam(name = Condition.SP_CLINICAL_STATUS) TokenParam clinicalstatus
            , @OptionalParam(name = Condition.SP_ASSERTED_DATE) DateRangeParam asserted
            , @OptionalParam(name = Condition.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= Condition.SP_RES_ID) StringParam id
    );
}

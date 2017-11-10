package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;

import java.util.List;

public interface ConditionRepository {
    void save(ConditionEntity condition);

    Condition read(IdType theId);

    Condition create(Condition condition, @IdParam IdType theId, @ConditionalUrlParam String theConditional);


    List<Condition> search(

            @OptionalParam(name = Condition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Condition.SP_CATEGORY) TokenParam category
            , @OptionalParam(name = Condition.SP_CLINICAL_STATUS) TokenParam clinicalstatus
            , @OptionalParam(name = Condition.SP_ASSERTED_DATE) DateRangeParam asserted
    );

    List<ConditionEntity> searchEntity(
            @OptionalParam(name = Condition.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Condition.SP_CATEGORY) TokenParam category
            , @OptionalParam(name = Condition.SP_CLINICAL_STATUS) TokenParam clinicalstatus
            , @OptionalParam(name = Condition.SP_ASSERTED_DATE) DateRangeParam asserted
    );
}

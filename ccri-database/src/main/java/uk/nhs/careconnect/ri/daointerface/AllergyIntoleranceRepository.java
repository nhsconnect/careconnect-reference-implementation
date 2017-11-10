package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.ri.entity.allergy.AllergyIntoleranceEntity;

import java.util.List;

public interface AllergyIntoleranceRepository {
    void save(AllergyIntoleranceEntity allergy);

    AllergyIntolerance read(IdType theId);

    AllergyIntolerance create(AllergyIntolerance allergy, @IdParam IdType theId, @ConditionalUrlParam String theConditional);


    List<AllergyIntolerance> search(

            @OptionalParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = AllergyIntolerance.SP_DATE) DateRangeParam date
            , @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenParam clinicalStatus

    );

    List<AllergyIntoleranceEntity> searchEntity(
            @OptionalParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = AllergyIntolerance.SP_DATE) DateRangeParam date
            , @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenParam clinicalStatus
    );
}

package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.allergy.AllergyIntoleranceEntity;

import java.util.List;

public interface AllergyIntoleranceRepository extends BaseRepository<AllergyIntoleranceEntity,AllergyIntolerance> {
    void save(FhirContext ctx,AllergyIntoleranceEntity allergy) throws OperationOutcomeException;

    AllergyIntolerance read(FhirContext ctx, IdType theId);

    AllergyIntoleranceEntity readEntity(FhirContext ctx, IdType theId);

    AllergyIntolerance create(FhirContext ctx,
                              AllergyIntolerance allergy,
                              @IdParam IdType theId,
                              @ConditionalUrlParam String theConditional) throws OperationOutcomeException;



    List<AllergyIntolerance> search(
            FhirContext ctx,
            @OptionalParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = AllergyIntolerance.SP_DATE) DateRangeParam date
            , @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenParam clinicalStatus
            , @OptionalParam(name = AllergyIntolerance.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= AllergyIntolerance.SP_RES_ID) StringParam id

    );

    List<AllergyIntoleranceEntity> searchEntity(FhirContext ctx,
            @OptionalParam(name = AllergyIntolerance.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = AllergyIntolerance.SP_DATE) DateRangeParam date
            , @OptionalParam(name = AllergyIntolerance.SP_CLINICAL_STATUS) TokenParam clinicalStatus
            , @OptionalParam(name = AllergyIntolerance.SP_IDENTIFIER) TokenParam identifier
            ,@OptionalParam(name= AllergyIntolerance.SP_RES_ID) StringParam id
    );
}

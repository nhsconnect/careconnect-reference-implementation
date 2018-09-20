package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.carePlan.CarePlanEntity;

import java.util.List;
import java.util.Set;

public interface CarePlanRepository extends BaseRepository<CarePlanEntity,CarePlan> {
    void save(FhirContext ctx, CarePlanEntity allergy) throws OperationOutcomeException;

    CarePlan read(FhirContext ctx, IdType theId);

    CarePlanEntity readEntity(FhirContext ctx, IdType theId);

    CarePlan create(FhirContext ctx, CarePlan allergy, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;


    List<Resource> search(
            FhirContext ctx,
            @OptionalParam(name = CarePlan.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = CarePlan.SP_DATE) DateRangeParam date
            , @OptionalParam(name = CarePlan.SP_CATEGORY) TokenOrListParam categories
            , @OptionalParam(name = CarePlan.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = CarePlan.SP_RES_ID) StringParam id
            , @IncludeParam(allow= {
                "CarePlan:subject"
                ,"CarePlan:supportingInformation"
                , "*"}) Set<Include> includes

    );

    List<CarePlanEntity> searchEntity(FhirContext ctx,
                                      @OptionalParam(name = CarePlan.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = CarePlan.SP_DATE) DateRangeParam date
            , @OptionalParam(name = CarePlan.SP_CATEGORY) TokenOrListParam categories
            , @OptionalParam(name = CarePlan.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = CarePlan.SP_RES_ID) StringParam id
            , @IncludeParam(allow= {
                "CarePlan:subject"
                ,"CarePlan:supportingInformation"
                , "*"}) Set<Include> includes
    );
}

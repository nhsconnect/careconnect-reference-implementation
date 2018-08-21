package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.entity.healthcareService.HealthcareServiceEntity;

import java.util.List;

public interface HealthcareServiceRepository extends BaseRepository<HealthcareServiceEntity,HealthcareService> {
    void save(FhirContext ctx, HealthcareServiceEntity location) throws OperationOutcomeException;

    HealthcareService read(FhirContext ctx, IdType theId);

    HealthcareServiceEntity readEntity(FhirContext ctx, IdType theId);

    HealthcareService create(FhirContext ctx, HealthcareService location, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;



    List<HealthcareService> searchHealthcareService(FhirContext ctx,

          @OptionalParam(name = HealthcareService.SP_IDENTIFIER) TokenParam identifier,
          @OptionalParam(name = HealthcareService.SP_NAME) StringParam name,
          @OptionalParam(name= HealthcareService.SP_TYPE) TokenOrListParam codes,
          @OptionalParam(name = HealthcareService.SP_RES_ID) TokenParam id,
          @OptionalParam(name = HealthcareService.SP_ORGANIZATION) ReferenceParam organisation

    );

    List<HealthcareServiceEntity> searchHealthcareServiceEntity(FhirContext ctx,

          @OptionalParam(name = HealthcareService.SP_IDENTIFIER) TokenParam identifier,
          @OptionalParam(name = HealthcareService.SP_NAME) StringParam name,
          @OptionalParam(name= HealthcareService.SP_TYPE) TokenOrListParam codes,
          @OptionalParam(name = HealthcareService.SP_RES_ID) TokenParam id,
          @OptionalParam(name = HealthcareService.SP_ORGANIZATION) ReferenceParam organisation

    );
}

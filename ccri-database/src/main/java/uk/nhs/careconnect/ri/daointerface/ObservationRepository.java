package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;

import java.util.List;

public interface ObservationRepository extends BaseInterface {

    Observation save(FhirContext ctx, Observation observation) throws IllegalArgumentException;

    Observation read(FhirContext ctx, IdType theId);

    ObservationEntity readEntity(FhirContext ctx, IdType theId );


    List<Observation> search (FhirContext ctx,
            @OptionalParam(name= Observation.SP_CATEGORY) TokenParam category,
            @OptionalParam(name= Observation.SP_CODE) TokenParam code,
            @OptionalParam(name= Observation.SP_DATE) DateRangeParam effectiveDate,
            @OptionalParam(name = Observation.SP_PATIENT) ReferenceParam patient
            );

}

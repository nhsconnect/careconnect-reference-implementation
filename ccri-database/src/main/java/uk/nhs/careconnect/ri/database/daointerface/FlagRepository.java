package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Flag;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.flag.FlagEntity;
import java.util.List;

public interface FlagRepository extends BaseRepository<FlagEntity, Flag> {

    void save(FhirContext ctx, FlagEntity list) throws OperationOutcomeException;

    Flag read(FhirContext ctx, IdType theId);

    FlagEntity readEntity(FhirContext ctx, IdType theId);

    Flag create(FhirContext ctx, Flag questionnaire, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    List<Flag> searchFlag(FhirContext ctx,

                                          @OptionalParam(name = Flag.SP_IDENTIFIER) TokenParam identifier,
                                          @OptionalParam(name = Flag.SP_RES_ID) StringParam id,
                                          @OptionalParam(name = Flag.SP_PATIENT) ReferenceParam patient

    );

    List<FlagEntity> searchFlagEntity(FhirContext ctx,
                                      @OptionalParam(name = Flag.SP_IDENTIFIER) TokenParam identifier,
                                      @OptionalParam(name = Flag.SP_RES_ID) StringParam id,
                                      @OptionalParam(name = Flag.SP_PATIENT) ReferenceParam patient
    );
}

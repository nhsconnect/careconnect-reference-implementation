package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.consent.ConsentEntity;


import java.util.List;

public interface ConsentRepository extends BaseRepository<ConsentEntity,Consent> {
    void save(FhirContext ctx, ConsentEntity consent) throws OperationOutcomeException;

    Consent read(FhirContext ctx, IdType theId);

    Consent create(FhirContext ctx, Consent impression, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    ConsentEntity readEntity(FhirContext ctx, IdType theId);

    List<Consent> search(FhirContext ctx,
                                    @OptionalParam(name = Consent.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Consent.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Consent.SP_RES_ID) StringParam id
    );

    List<ConsentEntity> searchEntity(FhirContext ctx,
                                                @OptionalParam(name = Consent.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Consent.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Consent.SP_RES_ID) StringParam id
    );
}

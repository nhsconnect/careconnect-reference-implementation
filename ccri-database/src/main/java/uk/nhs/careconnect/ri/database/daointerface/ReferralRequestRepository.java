package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.referral.ReferralRequestEntity;

import java.util.List;

public interface ReferralRequestRepository extends BaseRepository<ReferralRequestEntity,ReferralRequest> {
    void save(FhirContext ctx, ReferralRequestEntity referral) throws OperationOutcomeException;

    ReferralRequest read(FhirContext ctx, IdType theId);

    ReferralRequestEntity readEntity(FhirContext ctx, IdType theId);

    ReferralRequest create(FhirContext ctx, ReferralRequest referral, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;



    List<ReferralRequest> searchReferralRequest(FhirContext ctx,

        @OptionalParam(name = ReferralRequest.SP_IDENTIFIER) TokenParam identifier,
        @OptionalParam(name = ReferralRequest.SP_TYPE) TokenOrListParam codes,
        @OptionalParam(name = ReferralRequest.SP_RES_ID) StringParam id,
        @OptionalParam(name = ReferralRequest.SP_PATIENT) ReferenceParam patient

    );

    List<ReferralRequestEntity> searchReferralRequestEntity(FhirContext ctx,

        @OptionalParam(name = ReferralRequest.SP_IDENTIFIER) TokenParam identifier,
        @OptionalParam(name = ReferralRequest.SP_TYPE) TokenOrListParam codes,
        @OptionalParam(name = ReferralRequest.SP_RES_ID) StringParam id,
        @OptionalParam(name = ReferralRequest.SP_PATIENT) ReferenceParam patient
    );
}

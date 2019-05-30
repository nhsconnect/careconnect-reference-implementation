package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.Claim;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Resource;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.claim.ClaimEntity;

import java.util.List;


public interface ClaimRepository extends BaseRepository<ClaimEntity,Claim> {
    void save(FhirContext ctx, ClaimEntity claim) throws OperationOutcomeException;

    Claim read(FhirContext ctx, IdType theId);

    ClaimEntity readEntity(FhirContext ctx, IdType theId);


    ClaimEntity readEntity(FhirContext ctx, TokenParam identifier);

    Claim create(FhirContext ctx, Claim claim, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;


    List<Resource> search(
            FhirContext ctx,
            @OptionalParam(name = Claim.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Claim.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Claim.SP_RES_ID) StringParam id
    );

    List<ClaimEntity> searchEntity(FhirContext ctx,
                                      @OptionalParam(name = Claim.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = Claim.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = Claim.SP_RES_ID) StringParam id
    );
}

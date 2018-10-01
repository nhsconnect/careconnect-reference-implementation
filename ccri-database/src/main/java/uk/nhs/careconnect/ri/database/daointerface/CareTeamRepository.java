package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.CareTeam;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;


import java.util.List;

public interface CareTeamRepository extends BaseRepository<CareTeamEntity,CareTeam> {
    void save(FhirContext ctx, CareTeamEntity team) throws OperationOutcomeException;

    CareTeam read(FhirContext ctx, IdType theId);

    CareTeam create(FhirContext ctx, CareTeam team, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;

    CareTeamEntity readEntity(FhirContext ctx, IdType theId);

    List<CareTeam> search(FhirContext ctx,
                           @OptionalParam(name = CareTeam.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = CareTeam.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = CareTeam.SP_RES_ID) StringParam id
    );

    List<CareTeamEntity> searchEntity(FhirContext ctx,
                                       @OptionalParam(name = CareTeam.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = CareTeam.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = CareTeam.SP_RES_ID) StringParam id
    );
}

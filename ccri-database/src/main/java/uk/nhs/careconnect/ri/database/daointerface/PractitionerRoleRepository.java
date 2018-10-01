package uk.nhs.careconnect.ri.database.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import uk.nhs.careconnect.fhir.OperationOutcomeException;

import java.util.List;

public interface PractitionerRoleRepository extends BaseRepository<uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerRole,PractitionerRole> {

    void save(FhirContext ctx, uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerRole practitioner) throws OperationOutcomeException;

    PractitionerRole read(FhirContext ctx, IdType theId);

    uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerRole readEntity(FhirContext ctx, IdType theId);

    PractitionerRole create(FhirContext ctx, PractitionerRole practitionerRole, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException;


    List<PractitionerRole> search(FhirContext ctx,
            @OptionalParam(name = PractitionerRole.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = PractitionerRole.SP_PRACTITIONER) ReferenceParam practitioner,
            @OptionalParam(name = PractitionerRole.SP_ORGANIZATION) ReferenceParam organisation
            , @OptionalParam(name = PractitionerRole.SP_RES_ID) StringParam id
    );
    List<uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerRole> searchEntity(FhirContext ctx,
                                                                                           @OptionalParam(name = PractitionerRole.SP_IDENTIFIER) TokenParam identifier,
                                                                                           @OptionalParam(name = PractitionerRole.SP_PRACTITIONER) ReferenceParam practitioner,
                                                                                           @OptionalParam(name = PractitionerRole.SP_ORGANIZATION) ReferenceParam organisation
            , @OptionalParam(name = PractitionerRole.SP_RES_ID) StringParam id
    );

}

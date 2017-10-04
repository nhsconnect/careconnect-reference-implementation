package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.PractitionerRole;

import java.util.List;

public interface PractitionerRoleRepository {

    void save(uk.nhs.careconnect.ri.entity.practitioner.PractitionerRole practitioner);

    PractitionerRole read(IdType theId);

    uk.nhs.careconnect.ri.entity.practitioner.PractitionerRole readEntity(IdType theId);

    PractitionerRole create(PractitionerRole practitionerRole, @IdParam IdType theId, @ConditionalUrlParam String theConditional);

    List<PractitionerRole> search(
            @OptionalParam(name = PractitionerRole.SP_PRACTITIONER) ReferenceParam practitioner,
            @OptionalParam(name = PractitionerRole.SP_ORGANIZATION) ReferenceParam organisation
    );


}

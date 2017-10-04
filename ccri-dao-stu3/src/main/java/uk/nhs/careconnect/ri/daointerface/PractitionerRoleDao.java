package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.IdType;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerRole;

import java.util.List;

public class PractitionerRoleDao implements PractitionerRoleRepository {
    @Override
    public void save(PractitionerRole practitioner) {

    }

    @Override
    public org.hl7.fhir.dstu3.model.PractitionerRole read(IdType theId) {
        return null;
    }

    @Override
    public PractitionerRole readEntity(IdType theId) {
        return null;
    }

    @Override
    public org.hl7.fhir.dstu3.model.PractitionerRole create(org.hl7.fhir.dstu3.model.PractitionerRole practitionerRole, IdType theId, String theConditional) {
        return null;
    }


    @Override
    public List<org.hl7.fhir.dstu3.model.PractitionerRole> search(ReferenceParam practitioner, ReferenceParam organisation) {
        return null;
    }

}

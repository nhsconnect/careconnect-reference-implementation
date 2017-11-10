package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class EncounterDao implements EncounterRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void save(EncounterEntity encounter) {

    }

    @Override
    public Encounter read(IdType theId) {
        return null;
    }

    @Override
    public Encounter create(Encounter encounter, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<Encounter> search(ReferenceParam patient, DateRangeParam date, ReferenceParam episode) {
        return null;
    }

    @Override
    public List<EncounterEntity> searchEntity(ReferenceParam patient, DateRangeParam date, ReferenceParam episode) {
        return null;
    }
}

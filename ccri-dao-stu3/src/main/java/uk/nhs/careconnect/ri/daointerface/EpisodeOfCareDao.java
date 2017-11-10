package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class EpisodeOfCareDao implements EpisodeOfCareRepository {

    @PersistenceContext
    EntityManager em;


    @Override
    public void save(EpisodeOfCare episode) {

    }

    @Override
    public EpisodeOfCare read(IdType theId) {
        return null;
    }

    @Override
    public EpisodeOfCare create(EpisodeOfCare episode, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<EpisodeOfCare> search(ReferenceParam patient, DateRangeParam date) {
        return null;
    }

    @Override
    public List<EpisodeOfCareEntity> searchEntity(ReferenceParam patient, DateRangeParam date) {
        return null;
    }
}

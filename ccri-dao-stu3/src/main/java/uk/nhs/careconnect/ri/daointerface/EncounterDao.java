package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.EncounterEntityToFHIREncounterTransformer;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class EncounterDao implements EncounterRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private EncounterEntityToFHIREncounterTransformer encounterEntityToFHIREncounterTransformer;

    @Override
    public void save(EncounterEntity encounter) {

    }

    @Override
    public Encounter read(IdType theId) {

        EncounterEntity encounter = (EncounterEntity) em.find(EncounterEntity.class,Long.parseLong(theId.getIdPart()));

        return encounter == null
                ? null
                : encounterEntityToFHIREncounterTransformer.transform(encounter);
    }

    @Override
    public Encounter create(Encounter encounter, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<Encounter> search(ReferenceParam patient, DateRangeParam date, ReferenceParam episode) {
        List<EncounterEntity> qryResults = searchEntity(patient, date, episode);
        List<Encounter> results = new ArrayList<>();

        for (EncounterEntity encounterEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Encounter encounter = encounterEntityToFHIREncounterTransformer.transform(encounterEntity);
            results.add(encounter);
        }

        return results;
    }

    @Override
    public List<EncounterEntity> searchEntity(ReferenceParam patient, DateRangeParam date, ReferenceParam episode) {
        List<EncounterEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<EncounterEntity> criteria = builder.createQuery(EncounterEntity.class);
        Root<EncounterEntity> root = criteria.from(EncounterEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Encounter> results = new ArrayList<Encounter>();

        if (patient != null) {
            Join<EncounterEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

            Predicate p = builder.equal(join.get("id"),patient.getIdPart());
            predList.add(p);
        }


        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            criteria.select(root).where(predArray);
        }
        else
        {
            criteria.select(root);
        }

        qryResults = em.createQuery(criteria).getResultList();
        return qryResults;
    }
}

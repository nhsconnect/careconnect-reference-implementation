package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.database.daointerface.EpisodeOfCareRepository;
import uk.nhs.careconnect.ri.dao.transforms.EpisodeOfCareEntityToFHIREpisodeOfCareTransformer;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class EpisodeOfCareDao implements EpisodeOfCareRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private EpisodeOfCareEntityToFHIREpisodeOfCareTransformer episodeOfCareEntityToFHIREpisodeOfCareTransformer;

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(EpisodeOfCareEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public EpisodeOfCareEntity readEntity(FhirContext ctx, IdType theId) {
        return null;
    }

    @Override
    public void save(FhirContext ctx, EpisodeOfCareEntity resource) {

    }


    @Override
    public void save(FhirContext ctx, EpisodeOfCare episode) {

    }
    @Override
    public EpisodeOfCare read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            EpisodeOfCareEntity episode = (EpisodeOfCareEntity) em.find(EpisodeOfCareEntity.class, Long.parseLong(theId.getIdPart()));

            return episode == null
                    ? null
                    : episodeOfCareEntityToFHIREpisodeOfCareTransformer.transform(episode);
        } else {
            return null;
        }

    }

    @Override
    public EpisodeOfCare create(FhirContext ctx,EpisodeOfCare episode, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<EpisodeOfCare> search(FhirContext ctx,ReferenceParam patient, DateRangeParam date, StringParam resid) {

        List<EpisodeOfCareEntity> qryResults = searchEntity(ctx,patient, date,resid);
        List<EpisodeOfCare> results = new ArrayList<>();

        for (EpisodeOfCareEntity episodeEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            EpisodeOfCare episode = episodeOfCareEntityToFHIREpisodeOfCareTransformer.transform(episodeEntity);
            results.add(episode);
        }

        return results;
    }
    @Override
    public List<EpisodeOfCareEntity> searchEntity(FhirContext ctx,ReferenceParam patient, DateRangeParam date, StringParam resid) {
        List<EpisodeOfCareEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<EpisodeOfCareEntity> criteria = builder.createQuery(EpisodeOfCareEntity.class);
        Root<EpisodeOfCareEntity> root = criteria.from(EpisodeOfCareEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<EpisodeOfCare> results = new ArrayList<EpisodeOfCare>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<EpisodeOfCareEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<EpisodeOfCareEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
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

        qryResults = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS).getResultList();
        return qryResults;
    }
}

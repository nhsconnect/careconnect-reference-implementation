package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.AllergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer;
import uk.nhs.careconnect.ri.entity.allergy.AllergyIntoleranceEntity;
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
public class AllergyIntoleranceDao implements AllergyIntoleranceRepository {

    @PersistenceContext
    EntityManager em;

    private static final Logger log = LoggerFactory.getLogger(AllergyIntoleranceDao.class);

    @Override
    public void save(AllergyIntoleranceEntity allergy) {

    }

    @Autowired
    AllergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer;

    @Override
    public AllergyIntolerance read(IdType theId) {
        AllergyIntoleranceEntity allergyIntolerance = (AllergyIntoleranceEntity) em.find(AllergyIntoleranceEntity.class,Long.parseLong(theId.getIdPart()));

        return allergyIntolerance == null
                ? null
                : allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer.transform(allergyIntolerance);
    }

    @Override
    public AllergyIntolerance create(AllergyIntolerance allergy, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<AllergyIntolerance> search(ReferenceParam patient, DateRangeParam date, TokenParam clinicalStatus) {
        List<AllergyIntoleranceEntity> qryResults = searchEntity(patient, date, clinicalStatus);
        List<AllergyIntolerance> results = new ArrayList<>();

        for (AllergyIntoleranceEntity allergyIntoleranceEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            AllergyIntolerance allergyIntolerance = allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer.transform(allergyIntoleranceEntity);
            results.add(allergyIntolerance);
        }

        return results;
    }

    @Override
    public List<AllergyIntoleranceEntity> searchEntity(ReferenceParam patient, DateRangeParam date, TokenParam clinicalStatus) {
        List<AllergyIntoleranceEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<AllergyIntoleranceEntity> criteria = builder.createQuery(AllergyIntoleranceEntity.class);
        Root<AllergyIntoleranceEntity> root = criteria.from(AllergyIntoleranceEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<AllergyIntolerance> results = new ArrayList<AllergyIntolerance>();

        if (patient != null) {
            Join<AllergyIntoleranceEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

            Predicate p = builder.equal(join.get("id"),patient.getIdPart());
            predList.add(p);
        }

        if (clinicalStatus != null) {

            Integer status = null;
            switch (clinicalStatus.getValue().toLowerCase()) {
                case "active":
                    status = 0;
                    break;
                case "inactive":
                    status = 1;
                    break;
                case "resolved":
                    status = 2;
                    break;
                default: status = -1;

            }

            Predicate p = builder.equal(root.get("clinicalStatus"), status);
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

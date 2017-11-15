package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateParam;
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
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
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

    public boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }
    @Override
    public AllergyIntolerance read(IdType theId) {
        if (isNumeric(theId.getIdPart())) {
            AllergyIntoleranceEntity allergyIntolerance = (AllergyIntoleranceEntity) em.find(AllergyIntoleranceEntity.class, Long.parseLong(theId.getIdPart()));

            return allergyIntolerance == null
                    ? null
                    : allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer.transform(allergyIntolerance);
        } else {
            return null;
        }
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

        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);

        if (date !=null)
        {


            if (date.getLowerBoundAsInstant() != null) log.debug("getLowerBoundAsInstant()="+date.getLowerBoundAsInstant().toString());
            if (date.getUpperBoundAsInstant() != null) log.debug("getUpperBoundAsInstant()="+date.getUpperBoundAsInstant().toString());


            if (date.getLowerBound() != null) {

                DateParam dateParam = date.getLowerBound();
                log.debug("Lower Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case GREATERTHAN: {
                        Predicate p = builder.greaterThan(root.<Date>get("assertedDateTime"), parameterLower);
                        predList.add(p);

                        break;
                    }
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("assertedDateTime"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("assertedDateTime"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("assertedDateTime"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("assertedDateTime"), parameterLower);
                        predList.add(p);
                        break;

                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + date.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }
            if (date.getUpperBound() != null) {

                DateParam dateParam = date.getUpperBound();

                log.debug("Upper Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case APPROXIMATE:
                    case EQUAL: {
                        Predicate pupper = builder.lessThan(root.<Date>get("assertedDateTime"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("assertedDateTime"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("assertedDateTime"), parameterUpper);
                        predList.add(p);

                        break;
                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + date.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }

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


        TypedQuery<AllergyIntoleranceEntity> typedQuery = em.createQuery(criteria);

        if (date != null) {
            if (date.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, date.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            if (date.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, date.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
        }
        qryResults = typedQuery.getResultList();

        return qryResults;
    }
}

package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.ConditionEntityToFHIRConditionTransformer;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionCategory;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
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
public class ConditionDao implements ConditionRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ConditionEntityToFHIRConditionTransformer conditionEntityToFHIRConditionTransformer;

    private static final Logger log = LoggerFactory.getLogger(ConditionDao.class);

    public boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    @Override
    public void save(ConditionEntity condition) {

    }

    @Override
    public Condition read(IdType theId) {

        if (isNumeric(theId.getIdPart())) {
            ConditionEntity condition = (ConditionEntity) em.find(ConditionEntity.class, Long.parseLong(theId.getIdPart()));

            return condition == null
                    ? null
                    : conditionEntityToFHIRConditionTransformer.transform(condition);
        } else  {
            return null;
        }
    }

    @Override
    public Condition create(Condition condition, IdType theId, String theConditional) {
        return null;
    }


    @Override
    public List<Condition> search(ReferenceParam patient, TokenParam category, TokenParam clinicalstatus, DateRangeParam asserted) {
        List<ConditionEntity> qryResults = searchEntity(patient, category, clinicalstatus, asserted);
        List<Condition> results = new ArrayList<>();

        for (ConditionEntity conditionEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Condition condition = conditionEntityToFHIRConditionTransformer.transform(conditionEntity);
            results.add(condition);
        }

        return results;
    }

    @Override
    public List<ConditionEntity> searchEntity(ReferenceParam patient, TokenParam category, TokenParam clinicalstatus, DateRangeParam asserted) {
        List<ConditionEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ConditionEntity> criteria = builder.createQuery(ConditionEntity.class);
        Root<ConditionEntity> root = criteria.from(ConditionEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Condition> results = new ArrayList<Condition>();

        if (patient != null) {
            Join<ConditionEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

            Predicate p = builder.equal(join.get("id"),patient.getIdPart());
            predList.add(p);
        }
        if (clinicalstatus != null) {
            Integer status = null;
            switch (clinicalstatus.getValue().toLowerCase()) {
                case "active":
                    status = 0;
                    break;
                case "recurrence":

                    status = 1;
                    break;
                case "inactive":
                    status = 2;
                    break;
                case "remission":
                    status = 3;
                    break;
                case "resolved":
                    status = 4;
                    break;
                default:
                    status=-1;
            }

            Predicate p = builder.equal(root.get("clinicalStatus"), status);
            predList.add(p);

        }
        if (category != null) {
            Join<ConditionEntity, ConditionCategory> join = root.join("categories", JoinType.LEFT);
            Join<ConditionCategory, ConceptEntity> joinConcept = join.join("category", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),category.getValue());
            predList.add(p);
        }

        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);

        if (asserted !=null)
        {


            if (asserted.getLowerBoundAsInstant() != null) log.debug("getLowerBoundAsInstant()="+asserted.getLowerBoundAsInstant().toString());
            if (asserted.getUpperBoundAsInstant() != null) log.debug("getUpperBoundAsInstant()="+asserted.getUpperBoundAsInstant().toString());


            if (asserted.getLowerBound() != null) {

                DateParam dateParam = asserted.getLowerBound();
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
                        log.trace("DEFAULT DATE(0) Prefix = " + asserted.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }
            if (asserted.getUpperBound() != null) {

                DateParam dateParam = asserted.getUpperBound();

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
                        log.trace("DEFAULT DATE(0) Prefix = " + asserted.getValuesAsQueryTokens().get(0).getPrefix());
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

        TypedQuery<ConditionEntity> typedQuery = em.createQuery(criteria);

        if (asserted != null) {
            if (asserted.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, asserted.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            if (asserted.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, asserted.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
        }
        qryResults = typedQuery.getResultList();
        return qryResults;
    }
}

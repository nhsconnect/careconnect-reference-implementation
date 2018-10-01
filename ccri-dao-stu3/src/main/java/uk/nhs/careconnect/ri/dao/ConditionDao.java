package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.ConditionEntityToFHIRConditionTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionCategory;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionIdentifier;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
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
            @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    ConditionEntityToFHIRConditionTransformer conditionEntityToFHIRConditionTransformer;

    private static final Logger log = LoggerFactory.getLogger(ConditionDao.class);

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ConditionEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public void save(FhirContext ctx, ConditionEntity condition) {

    }

    @Override
    public Condition read(FhirContext ctx,IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            ConditionEntity condition = (ConditionEntity) em.find(ConditionEntity.class, Long.parseLong(theId.getIdPart()));

            return condition == null
                    ? null
                    : conditionEntityToFHIRConditionTransformer.transform(condition);
        } else  {
            return null;
        }
    }
    @Override
    public ConditionEntity readEntity(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ConditionEntity condition = (ConditionEntity) em.find(ConditionEntity.class, Long.parseLong(theId.getIdPart()));

            return condition;
        } else {
            return null;
        }
    }

    @Override
    public Condition create(FhirContext ctx,Condition condition, IdType theId, String theConditional) throws OperationOutcomeException
    {

        log.debug("Condition.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        ConditionEntity conditionEntity = null;

        if (condition.hasId()) conditionEntity = readEntity(ctx, condition.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/condition")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<ConditionEntity> results = searchEntity(ctx, null, null,null, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/condition"),null);
                    for (ConditionEntity con : results) {
                        conditionEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (conditionEntity == null) {
            conditionEntity = new ConditionEntity();
        }


        PatientEntity patientEntity = null;
        if (condition.hasSubject()) {
            log.trace(condition.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(condition.getSubject().getReference()));
            conditionEntity.setPatient(patientEntity);
        }
        if (condition.hasClinicalStatus()) {
            conditionEntity.setClinicalStatus(condition.getClinicalStatus());
        }
        if (condition.hasVerificationStatus()) {
            conditionEntity.setVerificationStatus(condition.getVerificationStatus());
        }
        if (condition.hasSeverity()) {
            ConceptEntity code = conceptDao.findAddCode(condition.getSeverity().getCoding().get(0));
            if (code != null) { conditionEntity.setSeverity(code); }
            else {
                log.info("Severity Code: Missing System/Code = "+ condition.getSeverity().getCoding().get(0).getSystem() +" code = "+condition.getSeverity().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing Severity System/Code = "+ condition.getSeverity().getCoding().get(0).getSystem() +" code = "+condition.getSeverity().getCoding().get(0).getCode());
            }
        }
        if (condition.hasCode()) {
            ConceptEntity code = conceptDao.findAddCode(condition.getCode().getCoding().get(0));
            if (code != null) { conditionEntity.setCode(code); }
            else {
                log.info("Code: Missing System/Code = "+ condition.getCode().getCoding().get(0).getSystem() +" code = "+condition.getCode().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ condition.getCode().getCoding().get(0).getSystem()
                        +" code = "+condition.getCode().getCoding().get(0).getCode());
            }
        }
        if (condition.hasAssertedDate()) {
            conditionEntity.setAssertedDateTime(condition.getAssertedDate());
        }
        if (condition.hasContext() && condition.getContext().getReference().contains("Encounter")) {
            EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(condition.getContext().getReference()));
            conditionEntity.setContextEncounter(encounterEntity);
        }
        if (condition.hasAsserter() && condition.getAsserter().getReference().contains("Practitioner")) {
            PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(condition.getAsserter().getReference()));
            conditionEntity.setAsserterPractitioner(practitionerEntity);
        }




        em.persist(conditionEntity);

        for (Identifier identifier : condition.getIdentifier()) {
            ConditionIdentifier conditionIdentifier = null;

            for (ConditionIdentifier orgSearch : conditionEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    conditionIdentifier = orgSearch;
                    break;
                }
            }
            if (conditionIdentifier == null)  conditionIdentifier = new ConditionIdentifier();

            conditionIdentifier.setValue(identifier.getValue());
            conditionIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            conditionIdentifier.setCondition(conditionEntity);
            em.persist(conditionIdentifier);
        }

        for (CodeableConcept concept :condition.getCategory()) {
            ConditionCategory conditionCategory = null;

            for (ConditionCategory catSearch : conditionEntity.getCategories()) {
                if (catSearch.getCategory().getCode().equals(catSearch.getCategory().getCode()) && catSearch.getCategory().getSystem().equals(catSearch.getCategory().getSystem())) {
                    conditionCategory = catSearch;
                    break;
                }
            }
            if (conditionCategory == null)  conditionCategory = new ConditionCategory();

            ConceptEntity code = conceptDao.findAddCode(concept.getCoding().get(0));
            if (code != null) {
                conditionCategory.setCategory(code);
                conditionCategory.setCondition(conditionEntity);
                em.persist(conditionCategory);
            }
            else {
                log.info("Category Code: Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                throw new IllegalArgumentException("Missing Category System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
            }
        }

        return conditionEntityToFHIRConditionTransformer.transform(conditionEntity);
    }


    @Override
    public List<Condition> search(FhirContext ctx,ReferenceParam patient, TokenParam category, TokenParam clinicalstatus, DateRangeParam asserted, TokenParam identifier, StringParam resid) {
        List<ConditionEntity> qryResults = searchEntity(ctx,patient, category, clinicalstatus, asserted,identifier,resid);
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
    public List<ConditionEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam category, TokenParam clinicalstatus, DateRangeParam asserted, TokenParam identifier, StringParam id) {

        List<ConditionEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ConditionEntity> criteria = builder.createQuery(ConditionEntity.class);
        Root<ConditionEntity> root = criteria.from(ConditionEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Condition> results = new ArrayList<Condition>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<ConditionEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"),patient.getIdPart());
                predList.add(p);
            } else {
                Join<ConditionEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"),-1);
                predList.add(p);
            }

        }
        if (id != null) {
            Predicate p = builder.equal(root.get("id"),id.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<ConditionEntity, ConditionIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

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
                    /* HAPI slips the dates forward
                    case GREATERTHAN: {
                        Predicate p = builder.greaterThan(root.<Date>get("assertedDateTime"), parameterLower);
                        predList.add(p);

                        break;
                    }
                    */
                    case GREATERTHAN:
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
                        Predicate p = builder.lessThanOrEqualTo(root.get("assertedDateTime"), parameterUpper);
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

        criteria.orderBy(builder.desc(root.get("assertedDateTime")));

        TypedQuery<ConditionEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        if (asserted != null) {
            if (asserted.getLowerBound() != null) {
                log.debug("asserted.getLowerBoundAsInstant() = "+asserted.getLowerBoundAsInstant());
                typedQuery.setParameter(parameterLower, asserted.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            }
            if (asserted.getUpperBound() != null) {
                log.debug("asserted.getUpperBoundAsInstant() = "+asserted.getUpperBoundAsInstant());
                typedQuery.setParameter(parameterUpper, asserted.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
            }
        }
        qryResults = typedQuery.getResultList();
        return qryResults;
    }
}

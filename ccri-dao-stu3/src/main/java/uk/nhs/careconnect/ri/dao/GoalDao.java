package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Goal;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.GoalEntityToFHIRGoalTransformer;
import uk.nhs.careconnect.ri.database.entity.goal.GoalEntity;
import uk.nhs.careconnect.ri.database.entity.goal.GoalIdentifier;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static uk.nhs.careconnect.ri.dao.daoutils.MAXROWS;

@Repository
@Transactional
public class GoalDao implements GoalRepository {

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
    GoalEntityToFHIRGoalTransformer goalEntityToFHIRGoalTransformer;

    private static final Logger log = LoggerFactory.getLogger(GoalDao.class);

    @Override
    public void save(FhirContext ctx, GoalEntity goal) throws OperationOutcomeException {

    }

    @Override
    public Goal read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            GoalEntity goal = (GoalEntity) em.find(GoalEntity.class, Long.parseLong(theId.getIdPart()));
            return goalEntityToFHIRGoalTransformer.transform(goal);
        }
        return null;
    }

    @Override
    public Goal create(FhirContext ctx, Goal goal, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("Goal.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        GoalEntity goalEntity = null;

        if (goal.hasId()) goalEntity = readEntity(ctx, goal.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/goal")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<GoalEntity> results = searchEntity(ctx, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/goal"),null);
                    for (GoalEntity con : results) {
                        goalEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (goalEntity == null) goalEntity = new GoalEntity();


        PatientEntity patientEntity = null;
        if (goal.hasSubject()) {
            log.trace(goal.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(goal.getSubject().getReference()));
            goalEntity.setPatient(patientEntity);
        }

        if (goal.hasStatus()) {
            goalEntity.setStatus(goal.getStatus());
        }



        em.persist(goalEntity);



        for (Identifier identifier : goal.getIdentifier()) {
            GoalIdentifier goalIdentifier = null;

            for (GoalIdentifier orgSearch : goalEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    goalIdentifier = orgSearch;
                    break;
                }
            }
            if (goalIdentifier == null)  goalIdentifier = new GoalIdentifier();

            goalIdentifier.setValue(identifier.getValue());
            goalIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            goalIdentifier.setGoal(goalEntity);
            em.persist(goalIdentifier);
        }




        return goalEntityToFHIRGoalTransformer.transform(goalEntity);
    }

    @Override
    public GoalEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            GoalEntity goalIntolerance = (GoalEntity) em.find(GoalEntity.class, Long.parseLong(theId.getIdPart()));
            return goalIntolerance;
        }
        return null;
    }

    @Override
    public List<Goal> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id) {
        List<GoalEntity> qryResults = searchEntity(ctx,patient, identifier,id);
        List<Goal> results = new ArrayList<>();

        for (GoalEntity goalIntoleranceEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Goal goal = goalEntityToFHIRGoalTransformer.transform(goalIntoleranceEntity);
            results.add(goal);
        }

        return results;
    }

    @Override
    public List<GoalEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam resid) {
        List<GoalEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<GoalEntity> criteria = builder.createQuery(GoalEntity.class);
        Root<GoalEntity> root = criteria.from(GoalEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Goal> results = new ArrayList<Goal>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<GoalEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<GoalEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<GoalEntity, GoalIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        
        ParameterExpression<Date> parameterLower = builder.parameter(Date.class);
        ParameterExpression<Date> parameterUpper = builder.parameter(Date.class);

        

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


        TypedQuery<GoalEntity> typedQuery = em.createQuery(criteria).setMaxResults(MAXROWS);
        
        qryResults = typedQuery.getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(GoalEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }
}

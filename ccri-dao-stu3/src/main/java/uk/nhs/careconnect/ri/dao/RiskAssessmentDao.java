package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.RiskAssessment;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.RiskAssessmentEntityToFHIRRiskAssessmentTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentEntity;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentIdentifier;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentPrediction;

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

@Repository
@Transactional
public class RiskAssessmentDao implements RiskAssessmentRepository {

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
    ConditionRepository conditionDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;
    
    @Autowired
    RiskAssessmentEntityToFHIRRiskAssessmentTransformer riskEntityToFHIRRiskAssessmentTransformer;

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentDao.class);

    @Override
    public void save(FhirContext ctx, RiskAssessmentEntity risk) throws OperationOutcomeException {

    }

    @Override
    public RiskAssessment read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            RiskAssessmentEntity risk = (RiskAssessmentEntity) em.find(RiskAssessmentEntity.class, Long.parseLong(theId.getIdPart()));
            return riskEntityToFHIRRiskAssessmentTransformer.transform(risk);
        }
        return null;
    }

    @Override
    public RiskAssessment create(FhirContext ctx, RiskAssessment risk, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("RiskAssessment.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        RiskAssessmentEntity riskEntity = null;

        if (risk.hasId()) riskEntity = readEntity(ctx, risk.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/risk")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<RiskAssessmentEntity> results = searchEntity(ctx, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/risk"),null);
                    for (RiskAssessmentEntity con : results) {
                        riskEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (riskEntity == null) riskEntity = new RiskAssessmentEntity();


        PatientEntity patientEntity = null;
        if (risk.hasSubject()) {
            log.trace(risk.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(risk.getSubject().getReference()));
            riskEntity.setPatient(patientEntity);
        }

        if (risk.hasStatus()) {
            riskEntity.setStatus(risk.getStatus());
        }
        if (risk.hasCode()) {
            ConceptEntity concept = conceptDao.findAddCode(risk.getCode().getCodingFirstRep());
            if (concept!=null) {
                riskEntity.setRiskCode(concept);
            }
        }

        if (risk.hasContext()) {
            if (risk.getContext().getReference().contains("Encounter")) {
                EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(risk.getContext().getReference()));
                riskEntity.setContextEncounter(encounterEntity);
            }
        }

        if (risk.hasOccurrenceDateTimeType()) {
            try {
                riskEntity.setOccurrenceEndDateTime(risk.getOccurrenceDateTimeType().getValue());
            } catch (Exception ex) {

            }
        }
        if (risk.hasCondition()) {
            ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new IdType(risk.getCondition().getReference()));
            if (conditionEntity!=null) {
                riskEntity.setCondition(conditionEntity);
            }
        }

        if (risk.hasMitigation()) {
            riskEntity.setMitigation(risk.getMitigation());
        }

        em.persist(riskEntity);



        if (risk.hasIdentifier()) {
            Identifier identifier = risk.getIdentifier();
            RiskAssessmentIdentifier
                    riskIdentifier = null;

            for (RiskAssessmentIdentifier orgSearch : riskEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    riskIdentifier = orgSearch;
                    break;
                }
            }
            if (riskIdentifier == null)  riskIdentifier = new RiskAssessmentIdentifier();

            riskIdentifier.setValue(identifier.getValue());
            riskIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            riskIdentifier.setRiskAssessment(riskEntity);
            em.persist(riskIdentifier);
        }

        for (RiskAssessmentPrediction pred: riskEntity.getPredictions()) {
            em.remove(pred);
        }
        for (RiskAssessment.RiskAssessmentPredictionComponent component : risk.getPrediction()) {
            RiskAssessmentPrediction prediction = new RiskAssessmentPrediction();

            prediction.setRiskAssessment(riskEntity);

            if (component.hasOutcome()) {
                ConceptEntity concept = conceptDao.findAddCode(component.getOutcome().getCodingFirstRep());
                if (concept!=null) {
                    prediction.setOutcome(concept);
                }
            }
            if (component.hasQualitativeRisk()) {
                ConceptEntity concept = conceptDao.findAddCode(component.getQualitativeRisk().getCodingFirstRep());
                if (concept!=null) {
                    prediction.setQualitiveRiskConcept(concept);
                }
            }
            em.persist(prediction);
        }

        return riskEntityToFHIRRiskAssessmentTransformer.transform(riskEntity);
    }

    @Override
    public RiskAssessmentEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            RiskAssessmentEntity riskIntolerance = (RiskAssessmentEntity) em.find(RiskAssessmentEntity.class, Long.parseLong(theId.getIdPart()));
            return riskIntolerance;
        }
        return null;
    }

    @Override
    public List<RiskAssessment> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id) {
        List<RiskAssessmentEntity> qryResults = searchEntity(ctx,patient, identifier,id);
        List<RiskAssessment> results = new ArrayList<>();

        for (RiskAssessmentEntity riskIntoleranceEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            RiskAssessment risk = riskEntityToFHIRRiskAssessmentTransformer.transform(riskIntoleranceEntity);
            results.add(risk);
        }

        return results;
    }

    @Override
    public List<RiskAssessmentEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam resid) {
        List<RiskAssessmentEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<RiskAssessmentEntity> criteria = builder.createQuery(RiskAssessmentEntity.class);
        Root<RiskAssessmentEntity> root = criteria.from(RiskAssessmentEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<RiskAssessment> results = new ArrayList<RiskAssessment>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<RiskAssessmentEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<RiskAssessmentEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<RiskAssessmentEntity, RiskAssessmentIdentifier> join = root.join("identifiers", JoinType.LEFT);

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


        TypedQuery<RiskAssessmentEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);
        
        qryResults = typedQuery.getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(RiskAssessmentEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }
}

package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.ClinicalImpressionEntityToFHIRClinicalImpressionTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentEntity;
import uk.nhs.careconnect.ri.database.entity.allergy.AllergyIntoleranceEntity;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionEntity;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionFinding;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionIdentifier;
import uk.nhs.careconnect.ri.database.entity.clinicialImpression.ClinicalImpressionPrognosis;

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
public class ClinicalImpressionDao implements ClinicalImpressionRepository {

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
    AllergyIntoleranceRepository allergyDao;

    @Autowired
    RiskAssessmentRepository riskDao;

    @Autowired
    ObservationRepository observationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;
    
    @Autowired
    ClinicalImpressionEntityToFHIRClinicalImpressionTransformer impressionEntityToFHIRClinicalImpressionTransformer;

    private static final Logger log = LoggerFactory.getLogger(ClinicalImpressionDao.class);

    @Override
    public void save(FhirContext ctx, ClinicalImpressionEntity impression) throws OperationOutcomeException {

    }

    @Override
    public ClinicalImpression read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ClinicalImpressionEntity impression = (ClinicalImpressionEntity) em.find(ClinicalImpressionEntity.class, Long.parseLong(theId.getIdPart()));
            return impressionEntityToFHIRClinicalImpressionTransformer.transform(impression);
        }
        return null;
    }

    @Override
    public ClinicalImpression create(FhirContext ctx, ClinicalImpression impression, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("ClinicalImpression.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        ClinicalImpressionEntity impressionEntity = null;

        if (impression.hasId()) impressionEntity = readEntity(ctx, impression.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/impression")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<ClinicalImpressionEntity> results = searchEntity(ctx, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/impression"),null);
                    for (ClinicalImpressionEntity con : results) {
                        impressionEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (impressionEntity == null) impressionEntity = new ClinicalImpressionEntity();

        if (impression.hasStatus()) {
            impressionEntity.setStatus(impression.getStatus());
        }

        if (impression.hasCode()) {
            ConceptEntity concept = conceptDao.findAddCode(impression.getCode().getCodingFirstRep());
            if (concept!=null) {
                impressionEntity.setRiskCode(concept);
            }
        }

        PatientEntity patientEntity = null;
        if (impression.hasSubject()) {
            log.trace(impression.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(impression.getSubject().getReference()));
            impressionEntity.setPatient(patientEntity);
        }

        if (impression.hasDescription()) {
            impressionEntity.setDescription(impression.getDescription());
        }


        if (impression.hasContext()) {
            if (impression.getContext().getReference().contains("Encounter")) {
                EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(impression.getContext().getReference()));
                impressionEntity.setContextEncounter(encounterEntity);
            }
        }

        if (impression.hasEffectivePeriod() ) {
            try {
                impressionEntity.setEffectiveEndDateTime(impression.getEffectivePeriod().getEnd());
            } catch (Exception ex) {

            }
            try {
                impressionEntity.setEffectiveStartDateTime(impression.getEffectivePeriod().getStart());
            } catch (Exception ex) {

            }
        }
        if (impression.hasEffectiveDateTimeType()) {
            try {
                impressionEntity.setEffectiveStartDateTime(impression.getEffectiveDateTimeType().getValue());
            } catch (Exception ex) {

            }
        }

        if (impression.hasDate()) {
            try {
                impressionEntity.setImpressionDateTime(impression.getDate());
            } catch (Exception ex) {

            }
        }

        if (impression.hasAssessor()) {
            PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(impression.getAssessor().getReference()));
            if (practitionerEntity!=null) {
                impressionEntity.setAssessorPractitioner(practitionerEntity);
            }
        }

        if (impression.hasProblem()) {
            for (Reference reference :impression.getProblem()) {
                if (reference.getReference().contains("Condition")) {
                    ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new IdType(reference.getReference()));
                    if (conditionEntity != null) {
                        impressionEntity.setProblemCondition(conditionEntity);
                    }
                }
                if (reference.getReference().contains("AllergyIntolerance")) {
                    AllergyIntoleranceEntity
                            allergyIntoleranceEntity = allergyDao.readEntity(ctx, new IdType(reference.getReference()));
                    if (allergyIntoleranceEntity != null) {
                        impressionEntity.setProblemAllergy(allergyIntoleranceEntity);
                    }
                }
            }
        }
        em.persist(impressionEntity);


        for (Identifier identifier : impression.getIdentifier()) {
            ClinicalImpressionIdentifier
                    impressionIdentifier = null;

            for (ClinicalImpressionIdentifier orgSearch : impressionEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    impressionIdentifier = orgSearch;
                    break;
                }
            }
            if (impressionIdentifier == null)  impressionIdentifier = new ClinicalImpressionIdentifier();

            impressionIdentifier.setValue(identifier.getValue());
            impressionIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            impressionIdentifier.setClinicalImpression(impressionEntity);
            em.persist(impressionIdentifier);
        }

        for (ClinicalImpressionFinding finding : impressionEntity.getFindings()) {
            em.remove(finding);
        }
        for (ClinicalImpression.ClinicalImpressionFindingComponent finding : impression.getFinding()) {
            ClinicalImpressionFinding findingEntity = new ClinicalImpressionFinding();
            findingEntity.setClinicalImpression(impressionEntity);
            if (finding.hasItemReference()) {
                try {
                    if (finding.getItemReference().getReference().contains("Condition")) {
                        ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new IdType(finding.getItemReference().getReference()));
                        if (conditionEntity != null) {
                            findingEntity.setItemCondition(conditionEntity);
                        }
                    }
                    if (finding.getItemReference().getReference().contains("Observation")) {
                        ObservationEntity observationEntity = observationDao.readEntity(ctx, new IdType(finding.getItemReference().getReference()));
                        if (observationEntity != null) {
                            findingEntity.setItemObservation(observationEntity);
                        }
                    }
                } catch (Exception ex) {

                }
            }
            if (finding.hasItemCodeableConcept()) {
                try {
                    ConceptEntity conceptEntity = conceptDao.findAddCode(finding.getItemCodeableConcept().getCodingFirstRep());
                    if (conceptEntity!=null) {
                        findingEntity.setItemCode(conceptEntity);

                    }
                } catch (Exception ex) {

                }
            }
            if (finding.hasBasis()) {
                findingEntity.setBasis(finding.getBasis());
            }
            em.persist(findingEntity);
        }

        for (ClinicalImpressionPrognosis prognosis : impressionEntity.getPrognosis()) {
            em.remove(prognosis);
        }
        for (CodeableConcept concept : impression.getPrognosisCodeableConcept()) {
            ClinicalImpressionPrognosis prognosisEntity = new ClinicalImpressionPrognosis();
            prognosisEntity.setClinicalImpression(impressionEntity);
            ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCodingFirstRep());
            if (conceptEntity!=null) {
                prognosisEntity.setPrognosisCode(conceptEntity);
                em.persist(prognosisEntity);
            }

        }
        for (Reference reference : impression.getPrognosisReference()) {
            ClinicalImpressionPrognosis prognosisEntity = new ClinicalImpressionPrognosis();
            prognosisEntity.setClinicalImpression(impressionEntity);
            if (reference.getReference().contains("RiskAssessment")) {
                RiskAssessmentEntity riskAssessmentEntity = riskDao.readEntity(ctx, new IdType(reference.getReference()));
                if (riskAssessmentEntity != null) {
                    prognosisEntity.setPrognosisRisk(riskAssessmentEntity);
                    em.persist(prognosisEntity);
                }
            }
        }
        return impressionEntityToFHIRClinicalImpressionTransformer.transform(impressionEntity);
    }

    @Override
    public ClinicalImpressionEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ClinicalImpressionEntity impressionIntolerance = (ClinicalImpressionEntity) em.find(ClinicalImpressionEntity.class, Long.parseLong(theId.getIdPart()));
            return impressionIntolerance;
        }
        return null;
    }

    @Override
    public List<ClinicalImpression> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id) {
        List<ClinicalImpressionEntity> qryResults = searchEntity(ctx,patient, identifier,id);
        List<ClinicalImpression> results = new ArrayList<>();

        for (ClinicalImpressionEntity impressionIntoleranceEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            ClinicalImpression impression = impressionEntityToFHIRClinicalImpressionTransformer.transform(impressionIntoleranceEntity);
            results.add(impression);
        }

        return results;
    }

    @Override
    public List<ClinicalImpressionEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam resid) {
        List<ClinicalImpressionEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ClinicalImpressionEntity> criteria = builder.createQuery(ClinicalImpressionEntity.class);
        Root<ClinicalImpressionEntity> root = criteria.from(ClinicalImpressionEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<ClinicalImpression> results = new ArrayList<ClinicalImpression>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<ClinicalImpressionEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<ClinicalImpressionEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<ClinicalImpressionEntity, ClinicalImpressionIdentifier> join = root.join("identifiers", JoinType.LEFT);

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


        TypedQuery<ClinicalImpressionEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);
        
        qryResults = typedQuery.getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ClinicalImpressionEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }
}

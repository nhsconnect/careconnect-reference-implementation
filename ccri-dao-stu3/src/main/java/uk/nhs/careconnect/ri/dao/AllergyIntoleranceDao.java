package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.AllergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.allergy.AllergyIntoleranceIdentifier;
import uk.nhs.careconnect.ri.database.entity.allergy.AllergyIntoleranceManifestation;
import uk.nhs.careconnect.ri.database.entity.allergy.AllergyIntoleranceReaction;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.allergy.AllergyIntoleranceEntity;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;

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
public class AllergyIntoleranceDao implements AllergyIntoleranceRepository {

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


    private static final Logger log = LoggerFactory.getLogger(AllergyIntoleranceDao.class);

    @Override
    public void save(FhirContext ctx,AllergyIntoleranceEntity allergy) {

    }



    @Autowired
    AllergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer;

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(AllergyIntoleranceEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }



    @Override
    public AllergyIntolerance read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            AllergyIntoleranceEntity allergyIntolerance = (AllergyIntoleranceEntity) em.find(AllergyIntoleranceEntity.class, Long.parseLong(theId.getIdPart()));

            return allergyIntolerance == null
                    ? null
                    : allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer.transform(allergyIntolerance);
        } else {
            return null;
        }
    }

    @Override
    public AllergyIntoleranceEntity readEntity(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            AllergyIntoleranceEntity allergyIntolerance = (AllergyIntoleranceEntity) em.find(AllergyIntoleranceEntity.class, Long.parseLong(theId.getIdPart()));
            return allergyIntolerance;
        }
        return null;
    }

    @Override
    public AllergyIntolerance create(FhirContext ctx, AllergyIntolerance allergy, IdType theId, String theConditional) throws OperationOutcomeException {

        log.debug("Allergy.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        AllergyIntoleranceEntity allergyEntity = null;

        if (allergy.hasId()) allergyEntity = readEntity(ctx, allergy.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/allergy")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<AllergyIntoleranceEntity> results = searchEntity(ctx, null, null,null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/allergy"),null);
                    for (AllergyIntoleranceEntity con : results) {
                        allergyEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (allergyEntity == null) allergyEntity = new AllergyIntoleranceEntity();


        PatientEntity patientEntity = null;
        if (allergy.hasPatient()) {
            log.trace(allergy.getPatient().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(allergy.getPatient().getReference()));
            allergyEntity.setPatient(patientEntity);
        }
        for (Extension extension : allergy.getExtension()) {
            switch (extension.getUrl()) {
                case CareConnectExtension.UrlAssociatedEncounter :
                    Reference encounterReference = (Reference) extension.getValue();
                    EncounterEntity encounter = encounterDao.readEntity(ctx,new IdType(encounterReference.getReference()));
                    allergyEntity.setAssociatedEncounter(encounter);
            }
        }
        if (allergy.hasClinicalStatus()) {
            allergyEntity.setClinicalStatus(allergy.getClinicalStatus());
        }
        if (allergy.hasVerificationStatus()) {
            allergyEntity.setVerificationStatus(allergy.getVerificationStatus());
        }
        if (allergy.hasCode()) {
            ConceptEntity code = conceptDao.findAddCode(allergy.getCode().getCoding().get(0));
            if (code != null) { allergyEntity.setCode(code); }
            else {
                String message = "Code: Missing System/Code = "+ allergy.getCode().getCoding().get(0).getSystem() +" code = "+allergy.getCode().getCoding().get(0).getCode();

                log.error(message);
                throw new OperationOutcomeException("Patient",message, OperationOutcome.IssueType.CODEINVALID);
            }
        }
        if (allergy.hasAssertedDate()) {
            allergyEntity.setAssertedDateTime(allergy.getAssertedDate());
        }
        if (allergy.hasLastOccurrence()) {
            allergyEntity.setLastOccurenceDateTime(allergy.getLastOccurrence());
        }
        if (allergy.hasOnset()) {
            try {
                allergyEntity.setOnsetDateTime(allergy.getOnsetDateTimeType().getValue());
            } catch (Exception ex) {

            }
        }
        if (allergy.hasAsserter() && allergy.getAsserter().getReference().contains("Practitioner")) {
            PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(allergy.getAsserter().getReference()));
            allergyEntity.setAsserterPractitioner(practitionerEntity);
        }

        em.persist(allergyEntity);

        for (Identifier identifier : allergy.getIdentifier()) {
            AllergyIntoleranceIdentifier allergyIdentifier = null;

            for (AllergyIntoleranceIdentifier orgSearch : allergyEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    allergyIdentifier = orgSearch;
                    break;
                }
            }
            if (allergyIdentifier == null)  allergyIdentifier = new AllergyIntoleranceIdentifier();

            allergyIdentifier.setValue(identifier.getValue());
            allergyIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            allergyIdentifier.setAllergyIntolerance(allergyEntity);
            em.persist(allergyIdentifier);
        }
        for (AllergyIntolerance.AllergyIntoleranceReactionComponent reaction : allergy.getReaction()) {
            AllergyIntoleranceReaction allergyReaction = null;

            for (AllergyIntoleranceReaction orgSearch : allergyEntity.getReactions()) {
                if (reaction.getManifestationFirstRep().getCoding().get(0).getSystem().equals(orgSearch.getManifestations().get(0).getManifestation().getSystem())
                        && reaction.getManifestationFirstRep().getCoding().get(0).getCode().equals(orgSearch.getManifestations().get(0).getManifestation().getCode())) {
                    allergyReaction = orgSearch;
                    break;
                }
            }
            if (allergyReaction == null)  allergyReaction = new AllergyIntoleranceReaction();

            ConceptEntity code = conceptDao.findAddCode(reaction.getManifestationFirstRep().getCoding().get(0));
            if (code != null) {

                AllergyIntoleranceManifestation man = null;
                // 11/1/2018 KGM Search for existing manifestations
                for (AllergyIntoleranceManifestation manSearch : allergyReaction.getManifestations()) {
                    if (manSearch.getManifestation().getCode().equals(code.getCode())) {
                        man = manSearch;
                        break;
                    }
                }
                if (man == null) man = new AllergyIntoleranceManifestation();
                man.setManifestation(code);
                man.setAllergyReaction(allergyReaction);
                em.persist(man);

                allergyReaction.setAllergy(allergyEntity);
                em.persist(allergyReaction);
            }
            else {
                String message = "Code: Missing System/Code = "+ reaction.getManifestationFirstRep().getCoding().get(0).getSystem() +" code = "+reaction.getManifestationFirstRep().getCoding().get(0).getCode();
                log.error(message);
                throw new OperationOutcomeException("AllergyIntolerance",message, OperationOutcome.IssueType.CODEINVALID);
            }
        }



        return allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer.transform(allergyEntity);
    }

    @Override
    public List<AllergyIntolerance> search(FhirContext ctx, ReferenceParam patient, DateRangeParam date, TokenParam clinicalStatus, TokenParam identifier , StringParam resid) {
        List<AllergyIntoleranceEntity> qryResults = searchEntity(ctx,patient, date, clinicalStatus,identifier,resid);
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
    public List<AllergyIntoleranceEntity> searchEntity(FhirContext ctx, ReferenceParam patient, DateRangeParam date, TokenParam clinicalStatus, TokenParam identifier ,StringParam resid) {


        List<AllergyIntoleranceEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<AllergyIntoleranceEntity> criteria = builder.createQuery(AllergyIntoleranceEntity.class);
        Root<AllergyIntoleranceEntity> root = criteria.from(AllergyIntoleranceEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<AllergyIntolerance> results = new ArrayList<AllergyIntolerance>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<AllergyIntoleranceEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<AllergyIntoleranceEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<AllergyIntoleranceEntity, AllergyIntoleranceIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

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
                   /* case GREATERTHAN: {
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
        criteria.orderBy(builder.desc(root.get("assertedDateTime")));

        TypedQuery<AllergyIntoleranceEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

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

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
import uk.nhs.careconnect.ri.dao.transforms.ProcedureEntityToFHIRProcedureTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureIdentifier;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedurePerformer;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;

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
public class ProcedureDao implements ProcedureRepository {

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
    OrganisationRepository organisationDao;

    @Autowired
    LocationRepository locationDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;
    
    @Autowired
    ProcedureEntityToFHIRProcedureTransformer procedureEntityToFHIRProcedureTransformer;

    private static final Logger log = LoggerFactory.getLogger(ProcedureDao.class);


    @Override
    public void save(FhirContext ctx, ProcedureEntity procedure) {

    }
    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ProcedureEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }
    @Override
    public Procedure read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ProcedureEntity procedure = (ProcedureEntity) em.find(ProcedureEntity.class, Long.parseLong(theId.getIdPart()));

            return procedure == null
                    ? null
                    : procedureEntityToFHIRProcedureTransformer.transform(procedure);
        } else {
            return null;
        }
    }

    @Override
    public ProcedureEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ProcedureEntity procedure = (ProcedureEntity) em.find(ProcedureEntity.class, Long.parseLong(theId.getIdPart()));

            return procedure ;
        } else {
            return null;
        }
    }


    @Override
    public Procedure create(FhirContext ctx,Procedure procedure, IdType theId, String theConditional) throws OperationOutcomeException {

        log.debug("Condition.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        ProcedureEntity procedureEntity = null;

        if (procedure.hasId()) procedureEntity = readEntity(ctx, procedure.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/procedure")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<ProcedureEntity> results = searchEntity(ctx, null, null,null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/procedure"),null);
                    for (ProcedureEntity con : results) {
                        procedureEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (procedureEntity == null) procedureEntity = new ProcedureEntity();


        PatientEntity patientEntity = null;
        if (procedure.hasSubject()) {
            log.trace(procedure.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(procedure.getSubject().getReference()));
            procedureEntity.setPatient(patientEntity);
        }
        if (procedure.hasStatus()) {
            procedureEntity.setStatus(procedure.getStatus());
        }
        if (procedure.hasNotDone()) {
            procedureEntity.setNotDone(procedure.getNotDone());
        }

        if (procedure.hasCode()) {
            ConceptEntity code = conceptDao.findAddCode(procedure.getCode().getCoding().get(0));
            if (code != null) {
                procedureEntity.setCode(code);
            }
            else {
                log.info("Code: Missing System/Code = "+ procedure.getCode().getCoding().get(0).getSystem()
                        +" code = "+procedure.getCode().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ procedure.getCode().getCoding().get(0).getSystem()
                        +" code = "+procedure.getCode().getCoding().get(0).getCode());
            }
        }
        if (procedure.hasCategory()) {
            ConceptEntity code = conceptDao.findAddCode(procedure.getCategory().getCoding().get(0));
            if (code != null) {
                procedureEntity.setCategory(code); }
            else {
                log.info("Category: Missing System/Code = "+ procedure.getCategory().getCoding().get(0).getSystem()
                        +" code = "+procedure.getCategory().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing Category System/Code = "+ procedure.getCategory().getCoding().get(0).getSystem()
                        +" code = "+procedure.getCategory().getCoding().get(0).getCode());
            }
        }
        if (procedure.hasOutcome()) {
            ConceptEntity code = conceptDao.findAddCode(procedure.getOutcome().getCoding().get(0));
            if (code != null) {
                procedureEntity.setOutcome(code); }
            else {
                log.info("Outcome: Missing System/Code = "+ procedure.getOutcome().getCoding().get(0).getSystem()
                        +" code = "+procedure.getOutcome().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing Outcome System/Code = "+ procedure.getOutcome().getCoding().get(0).getSystem()
                        +" code = "+procedure.getOutcome().getCoding().get(0).getCode());
            }
        }
        if (procedure.hasLocation()) {
            LocationEntity locationEntity = locationDao.readEntity(ctx, new IdType(procedure.getLocation().getReference()));
            procedureEntity.setLocation(locationEntity);
        }
        if (procedure.hasPerformed()) {
            try {
                if (procedure.getPerformed() instanceof DateTimeType) {
                    procedureEntity.setPerformedDate(procedure.getPerformedDateTimeType().getValue());
                } else if (procedure.getPerformed() instanceof Period) {
                    procedureEntity.setPerformedDate(procedure.getPerformedPeriod().getStart());
                    procedureEntity.setPerformedEndDate(procedure.getPerformedPeriod().getEnd());
                }
            } catch (Exception ex) {
                throw new OperationOutcomeException("Procedure","Invalid Date Time: "+ex.getMessage(),OperationOutcome.IssueType.CODEINVALID);
            }
        }
        if (procedure.hasContext() && procedure.getContext().getReference().contains("Encounter")) {
            EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(procedure.getContext().getReference()));
            procedureEntity.setContextEncounter(encounterEntity);
        }
        em.persist(procedureEntity);

        for (Identifier identifier : procedure.getIdentifier()) {
            ProcedureIdentifier procedureIdentifier = null;

            for (ProcedureIdentifier orgSearch : procedureEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    procedureIdentifier = orgSearch;
                    break;
                }
            }
            if (procedureIdentifier == null)  procedureIdentifier = new ProcedureIdentifier();

            procedureIdentifier.setValue(identifier.getValue());
            procedureIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            procedureIdentifier.setProcedure(procedureEntity);
            em.persist(procedureIdentifier);
        }

         for (Procedure.ProcedurePerformerComponent performer : procedure.getPerformer()) {
            ProcedurePerformer
                    procedurePerformer = null;

            for (ProcedurePerformer perSearch : procedureEntity.getPerformers()) {
                if (performer.getActor().getReference().equals(performer.getActor().getReference()) ) {
                    procedurePerformer = perSearch;
                    break;
                }
            }
            if (procedurePerformer == null)  procedurePerformer = new ProcedurePerformer();

            if (performer.getActor().getReference().contains("Organization")) {
                procedurePerformer.setActorOrganisation(organisationDao.readEntity(ctx, new IdType(performer.getActor().getReference())));
            }
             if (performer.getActor().getReference().contains("Practitioner")) {
                 procedurePerformer.setActorPractitioner(practitionerDao.readEntity(ctx, new IdType(performer.getActor().getReference())));
             }

            procedurePerformer.setProcedure(procedureEntity);
            em.persist(procedurePerformer);
        }

        return procedureEntityToFHIRProcedureTransformer.transform(procedureEntity);
    }


    @Override
    public List<Procedure> search(FhirContext ctx,ReferenceParam patient, DateRangeParam date,  ReferenceParam subject, TokenParam identifier, StringParam resid) {

        List<ProcedureEntity> qryResults = searchEntity(ctx,patient, date, subject,identifier,resid);
        List<Procedure> results = new ArrayList<>();

        for (ProcedureEntity procedureEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Procedure procedure = procedureEntityToFHIRProcedureTransformer.transform(procedureEntity);
            results.add(procedure);
        }

        return results;
    }

    @Override
    public List<ProcedureEntity> searchEntity(FhirContext ctx,ReferenceParam patient,DateRangeParam date,  ReferenceParam subject, TokenParam identifier, StringParam resid) {
        List<ProcedureEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ProcedureEntity> criteria = builder.createQuery(ProcedureEntity.class);
        Root<ProcedureEntity> root = criteria.from(ProcedureEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Procedure> results = new ArrayList<Procedure>();

        if (subject != null) {
            patient = subject;
        }
        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<ProcedureEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<ProcedureEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<ProcedureEntity, ProcedureIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

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
                    case GREATERTHAN: /*{
                        Predicate p = builder.greaterThan(root.<Date>get("performedDate"), parameterLower);
                        predList.add(p);

                        break;
                    }*/
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("performedDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("performedDate"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("performedDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("performedDate"), parameterLower);
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
                        Predicate pupper = builder.lessThan(root.<Date>get("performedDate"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("performedDate"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("performedDate"), parameterUpper);
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

        criteria.orderBy(builder.desc(root.get("performedDate")));

        TypedQuery<ProcedureEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

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

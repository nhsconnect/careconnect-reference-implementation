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
import uk.nhs.careconnect.ri.dao.transforms.MedicationStatementEntityToFHIRMedicationStatementTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationStatement.*;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.database.entity.medicationStatement.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;

@Repository
@Transactional
public class MedicationStatementDao implements MedicationStatementRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MedicationRequestRepository prescriptionDao;

    @Autowired
    private OrganisationRepository organisationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

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
    ObservationRepository observationDao;

    @Autowired
    ProcedureRepository procedureDao;

    @Autowired
    EpisodeOfCareRepository episodeDao;

    @Autowired
    MedicationRepository medicationDao;

    private static final Logger log = LoggerFactory.getLogger(MedicationStatementDao.class);

    @Autowired
    private MedicationStatementEntityToFHIRMedicationStatementTransformer
            medicationStatementEntityToFHIRMedicationStatementTransformer;

    @Override
    public void save(FhirContext ctx, MedicationStatementEntity statement) {

    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(MedicationStatementEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public MedicationStatementEntity readEntity(FhirContext ctx, IdType theId) {
        return em.find(MedicationStatementEntity.class, Long.parseLong(theId.getIdPart()));
    }


    @Override
    public MedicationStatement read(FhirContext ctx,IdType theId) {

        MedicationStatementEntity statementEntity =  em.find(MedicationStatementEntity.class, Long.parseLong(theId.getIdPart()));

        return (statementEntity == null) ? null : medicationStatementEntityToFHIRMedicationStatementTransformer.transform(statementEntity);
    }

    @Override
    public MedicationStatement create(FhirContext ctx,MedicationStatement statement, IdType theId, String theConditional) throws OperationOutcomeException {

        log.debug("MedicationStatement.save");

        MedicationStatementEntity statementEntity = null;

        if (statement.hasId()) statementEntity = readEntity(ctx, statement.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/statement")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<MedicationStatementEntity> results = searchEntity(ctx
                            , null
                            , null
                            ,null
                            , null
                            , new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/statement")
                    );
                    for (MedicationStatementEntity con : results) {
                        statementEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (statementEntity == null) statementEntity = new MedicationStatementEntity();


        PatientEntity patientEntity = null;
        if (statement.hasSubject()) {
            log.trace(statement.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(statement.getSubject().getReference()));
            statementEntity.setPatient(patientEntity);
        }

        if (statement.hasStatus()) {
            statementEntity.setStatus(statement.getStatus());
        }
        
        if (statement.hasContext()) {
            if (statement.getContext().getReference().contains("Encounter")) {

                EncounterEntity encounter = encounterDao.readEntity(ctx,new IdType(statement.getContext().getReference()));
                statementEntity.setContextEncounter(encounter);
            }
        
        }

        if (statement.hasCategory()) {
            CodeableConcept concept = statement.getCategory();
            ConceptEntity code = conceptDao.findAddCode(concept.getCodingFirstRep());
            if (code != null) statementEntity.setCategoryCode(code);
        }



        if (statement.hasMedicationCodeableConcept()) {
            try {
                List<MedicationEntity> listMedication = medicationDao.searchEntity(ctx,new TokenParam()
                        .setSystem(statement.getMedicationCodeableConcept().getCoding().get(0).getSystem())
                        .setValue(statement.getMedicationCodeableConcept().getCoding().get(0).getCode()),null);
                if (listMedication.size() >0 ) {
                    statementEntity.setMedicationEntity(listMedication.get(0));
                } else {

                    Medication medication = new Medication();
                    medication.getCode().addCoding()
                            .setSystem(statement.getMedicationCodeableConcept().getCoding().get(0).getSystem())
                            .setDisplay(statement.getMedicationCodeableConcept().getCoding().get(0).getDisplay())
                            .setCode(statement.getMedicationCodeableConcept().getCoding().get(0).getCode());
                    MedicationEntity medicationNew = medicationDao.createEntity(ctx,medication,null,null);
                    statementEntity.setMedicationEntity(medicationNew);
                }
            } catch (Exception ex) {}
        }
        if (statement.hasMedicationReference()) {
            try {
                MedicationEntity medicationEntity = medicationDao.readEntity(ctx, new IdType(statement.getMedicationReference().getReference()));
                statementEntity.setMedicationEntity(medicationEntity);
            } catch(Exception ex) {}
        }


        if (statement.hasDateAsserted()) {
            statementEntity.setAssertedDate(statement.getDateAsserted());
        }

        if (statement.hasEffectivePeriod()) {
            try {
                statementEntity.setEffectiveStartDate(statement.getEffectivePeriod().getStart());
                statementEntity.setEffectiveEndDate(statement.getEffectivePeriod().getEnd());
            } catch (Exception ex) {

            }
         } else if (statement.hasEffective()) {
            try {
            statementEntity.setEffectiveStartDate(statement.getEffectiveDateTimeType().getValue()); }
            catch (Exception ex) {

            }
        }

        if (statement.hasInformationSource()) {
            Reference reference = statement.getInformationSource();
            if (reference.getReference().contains("Patient")) {
                PatientEntity patientEntity1 = patientDao.readEntity(ctx,new IdType(reference.getReference()));
                statementEntity.setInformationPatient(patientEntity1);
            }
            if (reference.getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx,new IdType(reference.getReference()));
                statementEntity.setInformationPractitioner(practitionerEntity);
            }
            if (reference.getReference().contains("Organization")) {
                OrganisationEntity organisationEntity = organisationDao.readEntity(ctx,new IdType(reference.getReference()));
                statementEntity.setInformationOrganisation(organisationEntity);
            }
        }

        if (statement.hasTaken()) {
            statementEntity.setTaken(statement.getTaken());
        }

        if (statement.hasReasonCode()) {
            Coding code = new Coding().setCode(statement.getReasonCode().get(0).getCodingFirstRep().getCode()).setSystem(statement.getReasonCode().get(0).getCodingFirstRep().getSystem());
            ConceptEntity codeEntity = conceptDao.findAddCode(code);
            if (codeEntity != null) statementEntity.setReasonCode(codeEntity);
        }

        if (statement.hasReasonNotTaken()) {
            ConceptEntity codeEntity = conceptDao.findAddCode(statement.getReasonNotTaken().get(0).getCoding().get(0));
            if (codeEntity != null) statementEntity.setNotTakenCode(codeEntity);
        }

        if (statement.hasNote()) {
            statementEntity.setNote(statement.getNoteFirstRep().getText());
        }

        em.persist(statementEntity);

        for (Identifier identifier : statement.getIdentifier()) {
            MedicationStatementIdentifier statementIdentifier = null;

            for (MedicationStatementIdentifier orgSearch : statementEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    statementIdentifier = orgSearch;
                    break;
                }
            }
            if (statementIdentifier == null)  statementIdentifier = new MedicationStatementIdentifier();

            statementIdentifier.setValue(identifier.getValue());
            statementIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            statementIdentifier.setMedicationStatement(statementEntity);
            em.persist(statementIdentifier);
        }

        // Don't attempt to rebuild dosages
        for ( MedicationStatementDosage dosageEntity : statementEntity.getDosages()) {
            em.remove(dosageEntity);
        }
        statementEntity.setDosages(new HashSet<>());
        em.persist(statementEntity);

        Integer cnt = 0;
        for (Dosage dosage : statement.getDosage()) {
            log.debug("Iteration "+cnt);
            cnt++;
            MedicationStatementDosage dosageEntity = new MedicationStatementDosage();
            dosageEntity.setMedicationStatement(statementEntity);

            if (dosage.hasAdditionalInstruction()) {

                ConceptEntity code = conceptDao.findAddCode(dosage.getAdditionalInstruction().get(0).getCoding().get(0));
                if (code != null) {
                    dosageEntity.setAdditionalInstructionCode(code);
                } else {
                    log.info("Code: Missing System/Code = " + dosage.getAdditionalInstruction().get(0).getCoding().get(0).getSystem()
                            + " code = " + dosage.getAdditionalInstruction().get(0).getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + dosage.getAdditionalInstruction().get(0).getCoding().get(0).getSystem()
                            + " code = " + dosage.getAdditionalInstruction().get(0).getCoding().get(0).getCode());
                }
            }
            if (dosage.hasAsNeededCodeableConcept()) {

                try {
                    ConceptEntity code = conceptDao.findAddCode(dosage.getAsNeededCodeableConcept().getCoding().get(0));
                    if (code != null) {
                        dosageEntity.setAdditionalInstructionCode(code);
                    } else {
                        log.info("Code: Missing System/Code = " + dosage.getAsNeededCodeableConcept().getCoding().get(0).getSystem()
                                + " code = " + dosage.getAsNeededCodeableConcept().getCoding().get(0).getCode());

                        throw new IllegalArgumentException("Missing System/Code = " + dosage.getAsNeededCodeableConcept().getCoding().get(0).getSystem()
                                + " code = " + dosage.getAsNeededCodeableConcept().getCoding().get(0).getCode());
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
            if (dosage.hasRoute()) {

                ConceptEntity code = conceptDao.findAddCode(dosage.getRoute().getCoding().get(0));
                if (code != null) {
                    dosageEntity.setRouteCode(code);
                } else {
                    log.info("Code: Missing System/Code = " + dosage.getRoute().getCoding().get(0).getSystem()
                            + " code = " + dosage.getRoute().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + dosage.getRoute().getCoding().get(0).getSystem()
                            + " code = " + dosage.getRoute().getCoding().get(0).getCode());
                }
            }
            if (dosage.hasAsNeededBooleanType()) {
                try {
                    dosageEntity.setAsNeededBoolean(dosage.getAsNeededBooleanType().booleanValue());
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
            if (dosage.hasText()) {
                dosageEntity.setOtherText(dosage.getText());
            }
            if (dosage.hasPatientInstruction()) {
                dosageEntity.setPatientInstruction(dosage.getPatientInstruction());
            }
            if (dosage.hasDoseSimpleQuantity() ) {
                try {
                    SimpleQuantity qty = dosage.getDoseSimpleQuantity();

                    if (qty.hasCode()) {
                        ConceptEntity code = conceptDao.findAddCode(qty);
                        if (code != null) {
                            dosageEntity.setDoseUnitOfMeasure(code);
                        } else {
                            log.info("Code: Missing System/Code = " + qty.getSystem()
                                    + " code = " + qty.getCode());

                            throw new IllegalArgumentException("Missing System/Code = " + qty.getSystem()
                                    + " code = " + qty.getCode());
                        }
                    }
                    dosageEntity.setDoseQuantity(qty.getValue());
                }
                catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }

            if (dosage.hasDoseRange()) {

                try {
                    SimpleQuantity qty = dosage.getDoseRange().getHigh();
                    dosageEntity.setDoseRangeHigh(qty.getValue());

                    if (qty.hasCode()) {
                        ConceptEntity code = conceptDao.findAddCode(qty);
                        if (code != null) {
                            dosageEntity.setDoseHighUnitOfMeasure(code);
                        } else {
                            log.info("Code: Missing System/Code = " + qty.getSystem()
                                    + " code = " + qty.getCode());

                            throw new IllegalArgumentException("Missing System/Code = " + qty.getSystem()
                                    + " code = " + qty.getCode());
                        }
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
                try {
                    SimpleQuantity qty = dosage.getDoseRange().getLow();
                    dosageEntity.setDoseRangeLow(qty.getValue());

                    if (qty.hasCode()) {
                        ConceptEntity code = conceptDao.findAddCode(qty);
                        if (code != null) {
                            dosageEntity.setDoseLowUnitOfMeasure(code);
                        } else {
                            log.info("Code: Missing System/Code = " + qty.getSystem()
                                    + " code = " + qty.getCode());

                            throw new IllegalArgumentException("Missing System/Code = " + qty.getSystem()
                                    + " code = " + qty.getCode());
                        }
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
            em.persist(dosageEntity);
        }

        // Derives

        for ( MedicationStatementDerivedFrom derivedFromEntity : statementEntity.getDerives()) {
            em.remove(derivedFromEntity);
        }
        statementEntity.setDerives(new HashSet<>());
        em.persist(statementEntity);

        for (Reference reference : statement.getDerivedFrom()) {
            // NOT YET IMPLEMENTED
        }

        // Reasons

        for ( MedicationStatementReason reasonEntity : statementEntity.getReasons()) {
            em.remove(reasonEntity);
        }
        statementEntity.setReasons(new HashSet<>());
        for (Reference reference : statement.getReasonReference()) {
            if (reference.getReference().contains("Condition")) {
                ConditionEntity conditionEntity = conditionDao.readEntity(ctx,new IdType(reference.getReference()));
                if (conditionEntity != null) {
                    MedicationStatementReason reasonEntity = new MedicationStatementReason();
                    reasonEntity.setMedicationStatement(statementEntity);
                    reasonEntity.setCondition(conditionEntity);
                    em.persist(reasonEntity);
                    statementEntity.getReasons().add(reasonEntity);
                }
            }
            if (reference.getReference().contains("Observation")) {
                ObservationEntity observationEntity = observationDao.readEntity(ctx,new IdType(reference.getReference()));
                if (observationEntity != null) {
                    MedicationStatementReason reasonEntity = new MedicationStatementReason();
                    reasonEntity.setMedicationStatement(statementEntity);
                    reasonEntity.setObservation(observationEntity);
                    em.persist(reasonEntity);
                    statementEntity.getReasons().add(reasonEntity);
                }
            }
        }
        em.persist(statementEntity);

        // PartOf

        for ( MedicationStatementPartOf partOfEntity : statementEntity.getPartOfs()) {
            em.remove(partOfEntity);
        }
        statementEntity.setReasons(new HashSet<>());
        em.persist(statementEntity);
        for (Reference reference : statement.getPartOf()) {
            if (reference.getReference().contains("Observation")) {
                ObservationEntity observationEntity = observationDao.readEntity(ctx,new IdType(reference.getReference()));
                if (observationEntity != null) {
                    MedicationStatementPartOf partOfEntity = new MedicationStatementPartOf();
                    partOfEntity.setMedicationStatement(statementEntity);
                    partOfEntity.setObservation(observationEntity);
                    em.persist(partOfEntity);
                    statementEntity.getPartOfs().add(partOfEntity);
                }
            }
            if (reference.getReference().contains("Procedure")) {
                ProcedureEntity procedureEntity = procedureDao.readEntity(ctx,new IdType(reference.getReference()));
                if (procedureEntity != null) {
                    MedicationStatementPartOf partOfEntity = new MedicationStatementPartOf();
                    partOfEntity.setMedicationStatement(statementEntity);
                    partOfEntity.setProcedure(procedureEntity);
                    em.persist(partOfEntity);
                    statementEntity.getPartOfs().add(partOfEntity);
                }
            }
            if (reference.getReference().contains("MedicationStatement")) {
                MedicationStatementEntity statementEntityPartOf = readEntity(ctx,new IdType(reference.getReference()));
                if (statementEntityPartOf != null) {
                    MedicationStatementPartOf partOfEntity = new MedicationStatementPartOf();
                    partOfEntity.setMedicationStatement(statementEntity);
                    partOfEntity.setMedicationStatement(statementEntityPartOf);
                    em.persist(partOfEntity);
                    statementEntity.getPartOfs().add(partOfEntity);
                }
            }
        }

        // BasedOn

        for ( MedicationStatementBasedOn basedOnEntity : statementEntity.getBasedOn()) {
            em.remove(basedOnEntity);
        }
        statementEntity.setReasons(new HashSet<>());
        em.persist(statementEntity);
        for (Reference reference : statement.getBasedOn()) {
            if (reference.getReference().contains("MedicationRequest")) {
                MedicationRequestEntity requestEntity = prescriptionDao.readEntity(ctx,new IdType(reference.getReference()));
                if (requestEntity != null) {
                    MedicationStatementBasedOn basedOnEntity = new MedicationStatementBasedOn();
                    basedOnEntity.setMedicationStatement(statementEntity);
                    basedOnEntity.setPrescription(requestEntity);
                    em.persist(basedOnEntity);
                    statementEntity.getBasedOn().add(basedOnEntity);
                }
            }
        }

        return medicationStatementEntityToFHIRMedicationStatementTransformer.transform(statementEntity);
    }

    @Override
    public List<MedicationStatement> search(FhirContext ctx, ReferenceParam patient, DateRangeParam effectiveDate, TokenParam status, StringParam resid, TokenParam identifier) {
        List<MedicationStatementEntity> statementEntities = searchEntity(ctx,patient,effectiveDate,status,resid,identifier);
        List<MedicationStatement> results = new ArrayList<>();

        for (MedicationStatementEntity statementEntity : statementEntities)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            MedicationStatement medicationStatement =  medicationStatementEntityToFHIRMedicationStatementTransformer.transform(statementEntity);
            results.add(medicationStatement);
        }
        return results;

    }

    @Override
    public List<MedicationStatementEntity> searchEntity(FhirContext ctx,ReferenceParam patient, DateRangeParam effectiveDate, TokenParam status, StringParam resid, TokenParam identifier) {
        List<MedicationStatementEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<MedicationStatementEntity> criteria = builder.createQuery(MedicationStatementEntity.class);
        Root<MedicationStatementEntity> root = criteria.from(MedicationStatementEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<MedicationStatement> results = new ArrayList<MedicationStatement>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<MedicationStatementEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<MedicationStatementEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        // REVISIT KGM 28/2/2018 Added Medication search. This is using itself not Medication table
        
        if (identifier !=null)
        {
            Join<MedicationStatementEntity, MedicationStatementIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        if (status != null) {
            Integer presstatus = null;
            switch (status.getValue().toLowerCase()) {
                case "active":
                    presstatus = 0;
                    break;
                case "on-hold":
                    presstatus = 1;
                    break;
                case "cancelled":
                    presstatus = 2;
                    break;
                case "completed":
                    presstatus = 3;
                    break;
                case "entered-in-error":
                    presstatus = 4;
                    break;
                case "stopped":
                    presstatus = 5;
                    break;
                case "draft":
                    presstatus = 6;
                    break;
                case "unknown":
                    presstatus = 7;
                    break;

            }

            Predicate p = builder.equal(root.get("status"), presstatus);
            predList.add(p);

        }


        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);

        if (effectiveDate !=null)
        {


            if (effectiveDate.getLowerBound() != null) {

                DateParam dateParam = effectiveDate.getLowerBound();


                switch (dateParam.getPrefix()) {
                    case GREATERTHAN: /*{
                        Predicate p = builder.greaterThan(root.<Date>get("effectiveStartDate"), parameterLower);
                        predList.add(p);

                        break;
                    }*/
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("effectiveStartDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("effectiveStartDate"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("effectiveStartDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("effectiveStartDate"), parameterLower);
                        predList.add(p);
                        break;

                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + effectiveDate.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }
            if (effectiveDate.getUpperBound() != null) {

                DateParam dateParam = effectiveDate.getUpperBound();

                log.debug("Upper Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case APPROXIMATE:
                    case EQUAL: {
                        Predicate pupper = builder.lessThan(root.<Date>get("effectiveStartDate"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("effectiveStartDate"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("effectiveStartDate"), parameterUpper);
                        predList.add(p);

                        break;
                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + effectiveDate.getValuesAsQueryTokens().get(0).getPrefix());
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
        criteria.orderBy(builder.desc(root.get("effectiveStartDate")));
        TypedQuery<MedicationStatementEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        if (effectiveDate != null) {
            if (effectiveDate.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, effectiveDate.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            if (effectiveDate.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, effectiveDate.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
        }
        qryResults = typedQuery.getResultList();
        return qryResults;
    }

}

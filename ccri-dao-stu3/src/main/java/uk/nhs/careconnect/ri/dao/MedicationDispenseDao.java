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
import uk.nhs.careconnect.ri.dao.transforms.MedicationDispenseEntityToFHIRMedicationDispenseTransformer;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseDosage;
import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseEntity;
import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseIdentifier;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class MedicationDispenseDao implements MedicationDispenseRepository {

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
    private OrganisationRepository organisationDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    EpisodeOfCareRepository episodeDao;

    @Autowired
    LocationRepository locationDao;

    @Autowired
    MedicationRequestRepository dispenseDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    MedicationRepository medicationDao;
    
    @Autowired
    MedicationDispenseEntityToFHIRMedicationDispenseTransformer medicationDispenseEntityToFHIRMedicationDispenseTransformer;

    private static final Logger log = LoggerFactory.getLogger(MedicationDispenseDao.class);

    @Override
    public void save(FhirContext ctx, MedicationDispenseEntity statement) throws OperationOutcomeException {

    }

    @Override
    public MedicationDispense read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationDispenseEntity medicationDispense = (MedicationDispenseEntity) em.find(MedicationDispenseEntity.class, Long.parseLong(theId.getIdPart()));
            return medicationDispenseEntityToFHIRMedicationDispenseTransformer.transform(medicationDispense);
        }
        return null;
    }

    @Override
    public MedicationDispense create(FhirContext ctx, MedicationDispense dispense, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("MedicationDispense.save");

        MedicationDispenseEntity dispenseEntity = null;

        if (dispense.hasId()) dispenseEntity = readEntity(ctx, dispense.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/dispense")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<MedicationDispenseEntity> results = searchEntity(ctx
                            , null
                            , null
                            ,null
                            , new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/dispense")
                            ,null
                            , null);
                    for (MedicationDispenseEntity con : results) {
                        dispenseEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (dispenseEntity == null) dispenseEntity = new MedicationDispenseEntity();


        if (dispense.hasStatus()) {
            dispenseEntity.setStatus(dispense.getStatus());
        }
        if (dispense.hasCategory()) {
            try {
                ConceptEntity code = conceptDao.findAddCode(dispense.getCategory().getCoding().get(0));
                if (code != null) {
                    dispenseEntity.setCategoryCode(code);
                } else {
                    log.info("Code: Missing System/Code = " + dispense.getCategory().getCoding().get(0).getSystem()
                            + " code = " + dispense.getCategory().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + dispense.getCategory().getCoding().get(0).getSystem()
                            + " code = " + dispense.getCategory().getCoding().get(0).getCode());
                }
            } catch (Exception ex) {}
        }


        if (dispense.hasMedicationCodeableConcept()) {
            try {
                List<MedicationEntity> listMedication = medicationDao.searchEntity(ctx,new TokenParam()
                        .setSystem(dispense.getMedicationCodeableConcept().getCoding().get(0).getSystem())
                        .setValue(dispense.getMedicationCodeableConcept().getCoding().get(0).getCode()),null);
                if (listMedication.size() >0 ) {
                    dispenseEntity.setMedicationEntity(listMedication.get(0));
                } else {

                    Medication medication = new Medication();
                    medication.getCode().addCoding()
                            .setSystem(dispense.getMedicationCodeableConcept().getCoding().get(0).getSystem())
                            .setDisplay(dispense.getMedicationCodeableConcept().getCoding().get(0).getDisplay())
                            .setCode(dispense.getMedicationCodeableConcept().getCoding().get(0).getCode());
                    MedicationEntity medicationNew = medicationDao.createEntity(ctx,medication,null,null);
                    dispenseEntity.setMedicationEntity(medicationNew);
                }
            } catch (Exception ex) {}
        }
        if (dispense.hasMedicationReference()) {
            try {
                MedicationEntity medicationEntity = medicationDao.readEntity(ctx, new IdType(dispense.getMedicationReference().getReference()));
                dispenseEntity.setMedicationEntity(medicationEntity);
            } catch(Exception ex) {}
        }

        PatientEntity patientEntity = null;
        if (dispense.hasSubject()) {
            log.trace(dispense.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(dispense.getSubject().getReference()));
            dispenseEntity.setPatient(patientEntity);
        }

        if (dispense.hasContext()) {
            if (dispense.getContext().getReference().contains("Encounter")) {

                EncounterEntity encounter = encounterDao.readEntity(ctx,new IdType(dispense.getContext().getReference()));
                dispenseEntity.setContextEncounter(encounter);
            }
            if (dispense.getContext().getReference().contains("EpisodeOfCare")) {
                EpisodeOfCareEntity episode = episodeDao.readEntity(ctx,new IdType(dispense.getContext().getReference()));
                dispenseEntity.setContextEpisodeOfCare(episode);

            }
        }

        if (dispense.hasPerformer()) {
            if (dispense.getPerformerFirstRep().getActor().getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(dispense.getPerformerFirstRep().getActor().getReference()));
                dispenseEntity.setPerformerPractitioner(practitionerEntity);
            }
            if (dispense.getPerformerFirstRep().getOnBehalfOf().getReference().contains("Organization")) {
                OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(dispense.getPerformerFirstRep().getOnBehalfOf().getReference()));
                dispenseEntity.setPerformerOrganisation(organisationEntity);
            }
        }

        if (dispense.hasAuthorizingPrescription()) {
            MedicationRequestEntity medicationRequestEntity = dispenseDao.readEntity(ctx, new IdType(dispense.getAuthorizingPrescriptionFirstRep().getReference()));
            dispenseEntity.setPrescription(medicationRequestEntity);
        }

        if (dispense.hasType()) {
            try {
                ConceptEntity code = conceptDao.findAddCode(dispense.getType().getCoding().get(0));
                if (code != null) {
                    dispenseEntity.setTypeCode(code);
                } else {
                    log.info("Code: Missing System/Code = " + dispense.getType().getCoding().get(0).getSystem()
                            + " code = " + dispense.getType().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + dispense.getType().getCoding().get(0).getSystem()
                            + " code = " + dispense.getType().getCoding().get(0).getCode());
                }
            } catch (Exception ex) {}
        }

        // Quantity

        if (dispense.hasQuantity()) {
            if (!dispense.getQuantity().isEmpty()) {
                dispenseEntity.setQuantityValue(dispense.getQuantity().getValue());
                dispenseEntity.setQuantityUnit(dispense.getQuantity().getUnit());
            }
        }
        if (dispense.hasDaysSupply()) {
            if (!dispense.getDaysSupply().isEmpty()) {
                dispenseEntity.setDaysSupplyValue(dispense.getDaysSupply().getValue());
                dispenseEntity.setDaysSupplyUnit(dispense.getDaysSupply().getUnit());
            }
        }

        if (dispense.hasWhenPrepared()) {
            dispenseEntity.setWhenPrepared(dispense.getWhenPrepared());
        }
        if (dispense.hasWhenHandedOver()) {
            dispenseEntity.setWhenHandedOver(dispense.getWhenHandedOver());
        }

        if (dispense.hasDestination()) {
            LocationEntity locationEntity = locationDao.readEntity(ctx, new IdType(dispense.getDestination().getReference()));
            dispenseEntity.setLocation(locationEntity);
        }

        if (dispense.hasReceiver()) {
            if (dispense.getReceiverFirstRep().getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(dispense.getReceiverFirstRep().getReference()));
                dispenseEntity.setReceiverPractitioner(practitionerEntity);
            }
            if (dispense.getReceiverFirstRep().getReference().contains("Organization")) {
                OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(dispense.getReceiverFirstRep().getReference()));
                dispenseEntity.setReceiverOrganisaton(organisationEntity);
            }
        }

        if (dispense.hasSubstitution()) {
            dispenseEntity.setSubstituted(dispense.getSubstitution().getWasSubstituted());
            if (dispense.getSubstitution().hasReason()) {
                try {
                    ConceptEntity code = conceptDao.findAddCode(dispense.getSubstitution().getReasonFirstRep().getCoding().get(0));
                    if (code != null) {
                        dispenseEntity.setSubstitutionReasonCode(code);
                    } else {
                        log.info("Code: Missing System/Code = " + dispense.getSubstitution().getReasonFirstRep().getCoding().get(0).getSystem()
                                + " code = " + dispense.getSubstitution().getReasonFirstRep().getCoding().get(0).getCode());

                        throw new IllegalArgumentException("Missing System/Code = " + dispense.getSubstitution().getReasonFirstRep().getCoding().get(0).getSystem()
                                + " code = " + dispense.getSubstitution().getReasonFirstRep().getCoding().get(0).getCode());
                    }
                } catch (Exception ex) {}
            }
            if (dispense.getSubstitution().hasType()) {
                try {
                    ConceptEntity code = conceptDao.findAddCode(dispense.getSubstitution().getType().getCoding().get(0));
                    if (code != null) {
                        dispenseEntity.setSubstitutionTypeCode(code);
                    } else {
                        log.info("Code: Missing System/Code = " + dispense.getSubstitution().getType().getCoding().get(0).getSystem()
                                + " code = " + dispense.getSubstitution().getType().getCoding().get(0).getCode());

                        throw new IllegalArgumentException("Missing System/Code = " + dispense.getSubstitution().getType().getCoding().get(0).getSystem()
                                + " code = " + dispense.getSubstitution().getType().getCoding().get(0).getCode());
                    }
                } catch (Exception ex) {}
            }
            if (dispense.getSubstitution().hasResponsibleParty()) {
                if (dispense.getSubstitution().getResponsiblePartyFirstRep().getReference().contains("Practitioner")) {
                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(dispense.getSubstitution().getResponsiblePartyFirstRep().getReference()));
                    dispenseEntity.setSubstitutionPractitioner(practitionerEntity);
                }
            }
        }
        if (dispense.hasNotDone()) {
            dispenseEntity.setNotDone(dispense.getNotDone());
        }

        if (dispense.hasNotDoneReason() && dispense.hasNotDoneReasonCodeableConcept() ) {
            try {
                ConceptEntity code = conceptDao.findAddCode(dispense.getNotDoneReasonCodeableConcept().getCoding().get(0));
                if (code != null) {
                    dispenseEntity.setNotDoneCode(code);
                } else {
                    log.info("ReasonCode: Missing System/Code = " + dispense.getNotDoneReasonCodeableConcept().getCoding().get(0).getSystem()
                            + " code = " + dispense.getNotDoneReasonCodeableConcept().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + dispense.getNotDoneReasonCodeableConcept().getCoding().get(0).getSystem()
                            + " code = " + dispense.getNotDoneReasonCodeableConcept().getCoding().get(0).getCode());
                }
            } catch (Exception ex) {}
        }

        for (Identifier identifier : dispense.getIdentifier()) {
            MedicationDispenseIdentifier dispenseIdentifier = null;

            for (MedicationDispenseIdentifier orgSearch : dispenseEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    dispenseIdentifier = orgSearch;
                    break;
                }
            }
            if (dispenseIdentifier == null)  dispenseIdentifier = new MedicationDispenseIdentifier();

            dispenseIdentifier.setValue(identifier.getValue());
            dispenseIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            dispenseIdentifier.setMedicationDispense(dispenseEntity);
            em.persist(dispenseIdentifier);
        }

        // Don't attempt to rebuild dosages
        for ( MedicationDispenseDosage dosageEntity : dispenseEntity.getDosageInstructions()) {
            em.remove(dosageEntity);
        }
        dispenseEntity.setDosageInstructions(new HashSet<>());
        em.persist(dispenseEntity);

        Integer cnt = 0;
        for (Dosage dosage : dispense.getDosageInstruction()) {
            log.debug("Iteration "+cnt);
            cnt++;
            MedicationDispenseDosage dosageEntity = new MedicationDispenseDosage();
            dosageEntity.setMedicationDispense(dispenseEntity);

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

        return medicationDispenseEntityToFHIRMedicationDispenseTransformer.transform(dispenseEntity);
    }

    @Override
    public List<MedicationDispense> search(FhirContext ctx, ReferenceParam patient, TokenParam status, StringParam id, TokenParam identifier, TokenParam code, ReferenceParam medication) {
        List<MedicationDispenseEntity> qryResults = searchEntity(ctx,patient, status,id,identifier,code,medication);
        List<MedicationDispense> results = new ArrayList<>();

        for (MedicationDispenseEntity medicationDispenseIntoleranceEntity : qryResults) {
            MedicationDispense medicationDispense = medicationDispenseEntityToFHIRMedicationDispenseTransformer.transform(medicationDispenseIntoleranceEntity);
            results.add(medicationDispense);
        }

        return results;
    }

    @Override
    public List<MedicationDispenseEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam status, StringParam resid, TokenParam identifier, TokenParam code, ReferenceParam medication) {
        List<MedicationDispenseEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<MedicationDispenseEntity> criteria = builder.createQuery(MedicationDispenseEntity.class);
        Root<MedicationDispenseEntity> root = criteria.from(MedicationDispenseEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<MedicationDispense> results = new ArrayList<MedicationDispense>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<MedicationDispenseEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<MedicationDispenseEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        // REVISIT KGM 28/2/2018 Added Medication search. This is using itself not Medication table

        if (medication != null) {
            Predicate p = builder.equal(root.get("id"),medication.getIdPart());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<MedicationDispenseEntity, MedicationDispenseIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (code!=null) {
            log.trace("Search on MedicationDispense.medicationCode code = "+code.getValue());
            Join<MedicationDispenseEntity, ConceptEntity> joinConcept = root.join("medicationCode", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),code.getValue());
            predList.add(p);
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
        criteria.orderBy(builder.desc(root.get("whenPrepared")));
        TypedQuery<MedicationDispenseEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        qryResults = typedQuery.getResultList();
        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(MedicationDispenseEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public MedicationDispenseEntity readEntity(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationDispenseEntity medicationDispense = (MedicationDispenseEntity) em.find(MedicationDispenseEntity.class, Long.parseLong(theId.getIdPart()));
            return medicationDispense;
        }
        return null;
    }
}

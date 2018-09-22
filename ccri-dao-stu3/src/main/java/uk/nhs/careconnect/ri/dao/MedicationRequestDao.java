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
import uk.nhs.careconnect.ri.dao.transforms.MedicationRequestEntityToFHIRMedicationRequestTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestDosage;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestIdentifier;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;

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
public class MedicationRequestDao implements MedicationRequestRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private MedicationRequestEntityToFHIRMedicationRequestTransformer
            medicationRequestEntityToFHIRMedicationRequestTransformer;

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
    EpisodeOfCareRepository episodeDao;

    @Autowired
    LocationRepository locationDao;

    @Autowired
    MedicationRepository medicationDao;



    private static final Logger log = LoggerFactory.getLogger(MedicationRequestDao.class);

    @Override
    public void save(FhirContext ctx, MedicationRequestEntity prescription) {



    }

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(MedicationRequestEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public MedicationRequestEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationRequestEntity medicationRequestEntity = em.find(MedicationRequestEntity.class, Long.parseLong(theId.getIdPart()));

            return medicationRequestEntity ;
        } else {
            return null;
        }
    }


    @Override
    public MedicationRequest read(FhirContext ctx,IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationRequestEntity medicationRequestEntity = em.find(MedicationRequestEntity.class, Long.parseLong(theId.getIdPart()));

            return medicationRequestEntity == null
                    ? null
                    : medicationRequestEntityToFHIRMedicationRequestTransformer.transform(medicationRequestEntity);
        } else {
            return null;
        }
    }

    @Override
    public MedicationRequest create(FhirContext ctx,MedicationRequest prescription, IdType theId, String theConditional) throws OperationOutcomeException {

        log.debug("MedicationRequest.save");

        MedicationRequestEntity prescriptionEntity = null;

        if (prescription.hasId()) prescriptionEntity = readEntity(ctx, prescription.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/prescription")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<MedicationRequestEntity> results = searchEntity(ctx
                            , null
                            , null
                            ,null
                            , null
                            , new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/prescription")
                            ,null
                            , null);
                    for (MedicationRequestEntity con : results) {
                        prescriptionEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (prescriptionEntity == null) prescriptionEntity = new MedicationRequestEntity();


        PatientEntity patientEntity = null;
        if (prescription.hasSubject()) {
            log.trace(prescription.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(prescription.getSubject().getReference()));
            prescriptionEntity.setPatient(patientEntity);
        }

        if (prescription.hasStatus()) {
            prescriptionEntity.setStatus(prescription.getStatus());
        }
        if (prescription.hasIntent()) {
            prescriptionEntity.setIntent(prescription.getIntent());
        }
        if (prescription.hasPriority()) {
            prescriptionEntity.setPriority(prescription.getPriority());
        }
        if (prescription.hasContext()) {
            if (prescription.getContext().getReference().contains("Encounter")) {

                EncounterEntity encounter = encounterDao.readEntity(ctx,new IdType(prescription.getContext().getReference()));
                prescriptionEntity.setContextEncounter(encounter);
            }
            if (prescription.getContext().getReference().contains("EpisodeOfCare")) {
                EpisodeOfCareEntity episode = episodeDao.readEntity(ctx,new IdType(prescription.getContext().getReference()));
                prescriptionEntity.setContextEpisodeOfCare(episode);

            }
        }

        if (prescription.hasMedicationCodeableConcept()) {
            try {
                List<MedicationEntity> listMedication = medicationDao.searchEntity(ctx,new TokenParam()
                        .setSystem(prescription.getMedicationCodeableConcept().getCoding().get(0).getSystem())
                        .setValue(prescription.getMedicationCodeableConcept().getCoding().get(0).getCode()),null);
                if (listMedication.size() >0 ) {
                    prescriptionEntity.setMedicationEntity(listMedication.get(0));
                } else {

                    Medication medication = new Medication();
                    medication.getCode().addCoding()
                            .setSystem(prescription.getMedicationCodeableConcept().getCoding().get(0).getSystem())
                            .setDisplay(prescription.getMedicationCodeableConcept().getCoding().get(0).getDisplay())
                            .setCode(prescription.getMedicationCodeableConcept().getCoding().get(0).getCode());
                    MedicationEntity medicationNew = medicationDao.createEntity(ctx,medication,null,null);
                    prescriptionEntity.setMedicationEntity(medicationNew);
                }
            } catch (Exception ex) {}
        }
        if (prescription.hasMedicationReference()) {
            try {
                MedicationEntity medicationEntity = medicationDao.readEntity(ctx, new IdType(prescription.getMedicationReference().getReference()));
                prescriptionEntity.setMedicationEntity(medicationEntity);
            } catch(Exception ex) {}
        }

        if (prescription.hasAuthoredOn()) {
            prescriptionEntity.setAuthoredDate(prescription.getAuthoredOn());
        }

        if (prescription.hasRequester()) {
            if (prescription.getRequester().getAgent().getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(prescription.getRequester().getAgent().getReference()));
                prescriptionEntity.setRequesterPractitioner(practitionerEntity);
            }
            if (prescription.getRequester().getAgent().getReference().contains("Organization")) {
                OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(prescription.getRequester().getAgent().getReference()));
                prescriptionEntity.setRequesterOrganisation(organisationEntity);
            }
        }
        MedicationRequest.MedicationRequestDispenseRequestComponent dispense = prescription.getDispenseRequest();

        if (dispense.hasExpectedSupplyDuration()) {
            prescriptionEntity.setExpectedSupplyDuration(dispense.getExpectedSupplyDuration().getValue());

            ConceptEntity code = conceptDao.findAddCode(dispense.getExpectedSupplyDuration());
            if (code != null) {
                prescriptionEntity.setDurationUnitsCode(code);
            } else {
                log.info("Duration Code: Missing System/Code = " +dispense.getExpectedSupplyDuration().getSystem()
                        + " code = " + dispense.getExpectedSupplyDuration().getCode());

                throw new IllegalArgumentException("Missing Duration System/Code = " +dispense.getExpectedSupplyDuration().getSystem()
                        + " code = " + dispense.getExpectedSupplyDuration().getCode());
            }
        }
        if (dispense.hasNumberOfRepeatsAllowed()) {
            prescriptionEntity.setNumberOfRepeatsAllowed(dispense.getNumberOfRepeatsAllowed());
        }
        if (dispense.hasValidityPeriod()) {
            if (dispense.getValidityPeriod().hasStart()) {
                prescriptionEntity.setDispenseRequestStart(dispense.getValidityPeriod().getStart());
            }
            if (dispense.getValidityPeriod().hasEnd()) {
                prescriptionEntity.setDispenseRequestEnd(dispense.getValidityPeriod().getEnd());
            }
        }

        for (Extension extension : prescription.getExtension()) {
            if (extension.getUrl().equals(CareConnectExtension.UrlMedicationSupplyType) && extension.getValue() instanceof CodeableConcept) {
                CodeableConcept concept = (CodeableConcept) extension.getValue();
                ConceptEntity code = conceptDao.findAddCode(concept.getCodingFirstRep());
                if (code != null) prescriptionEntity.setSupplyTypeCode(code);
            }
            if (extension.getUrl().equals(CareConnectExtension.ShrActionCodeExtension) && extension.getValue() instanceof CodeableConcept) {
                CodeableConcept concept = (CodeableConcept) extension.getValue();
                ConceptEntity code = conceptDao.findAddCode(concept.getCodingFirstRep());
                if (code != null) prescriptionEntity.setSupplyTypeCode(code);
            }
        }

        em.persist(prescriptionEntity);

        for (Identifier identifier : prescription.getIdentifier()) {
            MedicationRequestIdentifier prescriptionIdentifier = null;

            for (MedicationRequestIdentifier orgSearch : prescriptionEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    prescriptionIdentifier = orgSearch;
                    break;
                }
            }
            if (prescriptionIdentifier == null)  prescriptionIdentifier = new MedicationRequestIdentifier();

            prescriptionIdentifier.setValue(identifier.getValue());
            prescriptionIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            prescriptionIdentifier.setMedicationRequest(prescriptionEntity);
            em.persist(prescriptionIdentifier);
        }

        // Don't attempt to rebuild dosages
        for ( MedicationRequestDosage dosageEntity : prescriptionEntity.getDosages()) {
            em.remove(dosageEntity);
        }
        prescriptionEntity.setDosages(new HashSet<>());
        em.persist(prescriptionEntity);

        Integer cnt = 0;
        for (Dosage dosage : prescription.getDosageInstruction()) {
            log.debug("Iteration "+cnt);
            cnt++;
            MedicationRequestDosage dosageEntity = new MedicationRequestDosage();
            dosageEntity.setMedicationRequest(prescriptionEntity);

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

        return medicationRequestEntityToFHIRMedicationRequestTransformer.transform(prescriptionEntity);
    }

    @Override
    public List<MedicationRequest> search(FhirContext ctx
            , ReferenceParam patient
            , TokenParam code
            , DateRangeParam authoredDate
            , TokenParam status
            , TokenParam identifier
            , StringParam resid
            , ReferenceParam medication) {
        List<MedicationRequestEntity> qryResults = searchEntity(ctx, patient, code, authoredDate, status, identifier,resid, medication);
        List<MedicationRequest> results = new ArrayList<>();

        for (MedicationRequestEntity medicationRequestEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            MedicationRequest medicationRequest =  medicationRequestEntityToFHIRMedicationRequestTransformer.transform(medicationRequestEntity);
            results.add(medicationRequest);
        }
        return results;
    }

    @Override
    public List<MedicationRequestEntity> searchEntity(FhirContext ctx
            , ReferenceParam patient
            , TokenParam code
            , DateRangeParam authoredDate
            , TokenParam status
            , TokenParam identifier
            , StringParam resid
            , ReferenceParam medication) {
        List<MedicationRequestEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<MedicationRequestEntity> criteria = builder.createQuery(MedicationRequestEntity.class);
        Root<MedicationRequestEntity> root = criteria.from(MedicationRequestEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<MedicationRequest> results = new ArrayList<MedicationRequest>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<MedicationRequestEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<MedicationRequestEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<MedicationRequestEntity, MedicationRequestIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (code!=null) {
            log.trace("Search on MedicationRequest.medicationCode code = "+code.getValue());
            Join<MedicationRequestEntity, ConceptEntity> joinConcept = root.join("medicationCode", JoinType.LEFT);
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

        if (authoredDate !=null)
        {


            if (authoredDate.getLowerBound() != null) {

                DateParam dateParam = authoredDate.getLowerBound();


                switch (dateParam.getPrefix()) {
                    case GREATERTHAN: /*{
                        Predicate p = builder.greaterThan(root.<Date>get("authoredDate"), parameterLower);
                        predList.add(p);

                        break;
                    }*/
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("authoredDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("authoredDate"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("authoredDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("authoredDate"), parameterLower);
                        predList.add(p);
                        break;

                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + authoredDate.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }
            if (authoredDate.getUpperBound() != null) {

                DateParam dateParam = authoredDate.getUpperBound();

                log.debug("Upper Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case APPROXIMATE:
                    case EQUAL: {
                        Predicate pupper = builder.lessThan(root.<Date>get("authoredDate"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("authoredDate"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("authoredDate"), parameterUpper);
                        predList.add(p);

                        break;
                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + authoredDate.getValuesAsQueryTokens().get(0).getPrefix());
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
        criteria.orderBy(builder.desc(root.get("authoredDate")));
        TypedQuery<MedicationRequestEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        if (authoredDate != null) {
            if (authoredDate.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, authoredDate.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            if (authoredDate.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, authoredDate.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
        }
        qryResults = typedQuery.getResultList();
        return qryResults;
    }


}

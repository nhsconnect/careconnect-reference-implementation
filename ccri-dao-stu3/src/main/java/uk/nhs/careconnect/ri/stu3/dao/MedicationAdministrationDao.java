package uk.nhs.careconnect.ri.stu3.dao;

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

import uk.nhs.careconnect.ri.stu3.dao.transforms.MedicationAdministrationEntityToFHIRMedicationAdministrationTransformer;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationDosage;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationIdentifier;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationNote;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

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
public class MedicationAdministrationDao implements MedicationAdministrationRepository {

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
    MedicationRequestRepository administrationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    MedicationRepository medicationDao;

    @Autowired
    private LibDao libDao;
    
    @Autowired
    MedicationAdministrationEntityToFHIRMedicationAdministrationTransformer medicationAdministrationEntityToFHIRMedicationAdministrationTransformer;

    private static final Logger log = LoggerFactory.getLogger(MedicationAdministrationDao.class);

    @Override
    public void save(FhirContext ctx, MedicationAdministrationEntity statement) throws OperationOutcomeException {

    }

    @Override
    public MedicationAdministration read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationAdministrationEntity medicationAdministration = (MedicationAdministrationEntity) em.find(MedicationAdministrationEntity.class, Long.parseLong(theId.getIdPart()));
            return medicationAdministrationEntityToFHIRMedicationAdministrationTransformer.transform(medicationAdministration);
        }
        return null;
    }

    @Override
    public MedicationAdministration create(FhirContext ctx, MedicationAdministration administration, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("MedicationAdministration.save");

        MedicationAdministrationEntity administrationEntity = null;

        if (administration.hasId()) administrationEntity = readEntity(ctx, administration.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/administration")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<MedicationAdministrationEntity> results = searchEntity(ctx
                            , null
                            , null
                            ,null
                            , new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/administration")
                            ,null
                            , null);
                    for (MedicationAdministrationEntity con : results) {
                        administrationEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (administrationEntity == null) administrationEntity = new MedicationAdministrationEntity();


        if (administration.hasStatus()) {
            administrationEntity.setStatus(administration.getStatus());
        }
        if (administration.hasCategory()) {
            try {
                ConceptEntity code = conceptDao.findAddCode(administration.getCategory().getCoding().get(0));
                if (code != null) {
                    administrationEntity.setCategoryCode(code);
                } else {
                    log.info("Code: Missing System/Code = " + administration.getCategory().getCoding().get(0).getSystem()
                            + " code = " + administration.getCategory().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + administration.getCategory().getCoding().get(0).getSystem()
                            + " code = " + administration.getCategory().getCoding().get(0).getCode());
                }
            } catch (Exception ex) {}
        }


        if (administration.hasMedicationCodeableConcept()) {
            try {
                List<MedicationEntity> listMedication = medicationDao.searchEntity(ctx,new TokenParam()
                        .setSystem(administration.getMedicationCodeableConcept().getCoding().get(0).getSystem())
                        .setValue(administration.getMedicationCodeableConcept().getCoding().get(0).getCode()),null);
                if (listMedication.size() >0 ) {
                    administrationEntity.setMedicationEntity(listMedication.get(0));
                } else {

                    Medication medication = new Medication();
                    medication.getCode().addCoding()
                            .setSystem(administration.getMedicationCodeableConcept().getCoding().get(0).getSystem())
                            .setDisplay(administration.getMedicationCodeableConcept().getCoding().get(0).getDisplay())
                            .setCode(administration.getMedicationCodeableConcept().getCoding().get(0).getCode());
                    MedicationEntity medicationNew = medicationDao.createEntity(ctx,medication,null,null);
                    administrationEntity.setMedicationEntity(medicationNew);
                }
            } catch (Exception ex) {}
        }
        if (administration.hasMedicationReference()) {
            try {
                MedicationEntity medicationEntity = medicationDao.readEntity(ctx, new IdType(administration.getMedicationReference().getReference()));
                administrationEntity.setMedicationEntity(medicationEntity);
            } catch(Exception ex) {}
        }

        PatientEntity patientEntity = null;
        if (administration.hasSubject()) {
            log.trace(administration.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(administration.getSubject().getReference()));
            administrationEntity.setPatient(patientEntity);
        }

        if (administration.hasContext()) {
            if (administration.getContext().getReference().contains("Encounter")) {

                EncounterEntity encounter = encounterDao.readEntity(ctx,new IdType(administration.getContext().getReference()));
                administrationEntity.setContextEncounter(encounter);
            }
            if (administration.getContext().getReference().contains("EpisodeOfCare")) {
                EpisodeOfCareEntity episode = episodeDao.readEntity(ctx,new IdType(administration.getContext().getReference()));
                administrationEntity.setContextEpisodeOfCare(episode);

            }
        }

        if (administration.hasPerformer()) {
            if (administration.getPerformerFirstRep().hasActor()) {
                if (administration.getPerformerFirstRep().getActor().getReference().contains("Practitioner")) {
                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(administration.getPerformerFirstRep().getActor().getReference()));
                    administrationEntity.setPerformerPractitioner(practitionerEntity);
                }
            }
            if (administration.getPerformerFirstRep().hasOnBehalfOf()) {
                if (administration.getPerformerFirstRep().getOnBehalfOf().getReference().contains("Organization")) {
                    OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(administration.getPerformerFirstRep().getOnBehalfOf().getReference()));
                    administrationEntity.setPerformerOrganisation(organisationEntity);
                }
            }
        }

        if (administration.hasPrescription()) {
            MedicationRequestEntity medicationRequestEntity = administrationDao.readEntity(ctx, new IdType(administration.getPrescription().getReference()));
            administrationEntity.setPrescription(medicationRequestEntity);
        }


        if (administration.hasEffectivePeriod()) {
            administrationEntity.setEffectiveStart(administration.getEffectivePeriod().getStart());
            administrationEntity.setEffectiveEnd(administration.getEffectivePeriod().getEnd());
        }

        if (administration.hasEffectiveDateTimeType()) {
            administrationEntity.setEffectiveStart(administration.getEffectiveDateTimeType().getValue());
        }


        if (administration.hasReasonNotGiven()  ) {
            try {
                ConceptEntity code = conceptDao.findAddCode(administration.getReasonNotGivenFirstRep().getCoding().get(0));
                if (code != null) {
                    administrationEntity.setReasonNotGivenCode(code);
                } else {
                    log.info("ReasonCode: Missing System/Code = " + administration.getReasonNotGivenFirstRep().getCoding().get(0).getSystem()
                            + " code = " + administration.getReasonNotGivenFirstRep().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + administration.getReasonNotGivenFirstRep().getCoding().get(0).getSystem()
                            + " code = " + administration.getReasonNotGivenFirstRep().getCoding().get(0).getCode());
                }
            } catch (Exception ex) {}
        }

        if (administration.hasReasonCode()  ) {
            try {
                ConceptEntity code = conceptDao.findAddCode(administration.getReasonCodeFirstRep().getCoding().get(0));
                if (code != null) {
                    administrationEntity.setReasonCode(code);
                } else {
                    log.info("ReasonCode: Missing System/Code = " + administration.getReasonCodeFirstRep().getCoding().get(0).getSystem()
                            + " code = " + administration.getReasonCodeFirstRep().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + administration.getReasonCodeFirstRep().getCoding().get(0).getSystem()
                            + " code = " + administration.getReasonCodeFirstRep().getCoding().get(0).getCode());
                }
            } catch (Exception ex) {}
        }

        for (Identifier identifier : administration.getIdentifier()) {
            MedicationAdministrationIdentifier administrationIdentifier = null;

            for (MedicationAdministrationIdentifier orgSearch : administrationEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    administrationIdentifier = orgSearch;
                    break;
                }
            }
            if (administrationIdentifier == null)  administrationIdentifier = new MedicationAdministrationIdentifier();

            administrationIdentifier= (MedicationAdministrationIdentifier) libDao.setIdentifier(identifier,  administrationIdentifier);
            administrationIdentifier.setMedicationAdministration(administrationEntity);
            em.persist(administrationIdentifier);
        }

        for (MedicationAdministrationNote note : administrationEntity.getNotes()) {
            em.remove(note);
        }
        for (Annotation annotation : administration.getNote()) {
            MedicationAdministrationNote note = new MedicationAdministrationNote();
            note.setMedicationAdministration(administrationEntity);
            if (annotation.hasAuthorReference()) {
                if (annotation.getAuthorReference().getReference().contains("Patient")) {
                    PatientEntity patientEntity1= patientDao.readEntity(ctx, new IdType(annotation.getAuthorReference().getReference()));
                    note.setNotePatient(patientEntity1);
                }
                if (annotation.getAuthorReference().getReference().contains("Practitioner")) {
                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(annotation.getAuthorReference().getReference()));
                    note.setNotePractitioner(practitionerEntity);
                }

            }
            if (annotation.hasText()) {
                note.setNoteText(annotation.getText());
            }
            em.persist(note);
        }

        // Don't attempt to rebuild dosages
        for ( MedicationAdministrationDosage dosageEntity : administrationEntity.getDosages()) {
            em.remove(dosageEntity);
        }
        administrationEntity.setDosages(new HashSet<>());
        em.persist(administrationEntity);

        Integer cnt = 0;
        if (administration.hasDosage()) {
            log.debug("Iteration "+cnt);
            cnt++;
            MedicationAdministrationDosage dosageEntity = new MedicationAdministrationDosage();
            dosageEntity.setMedicationAdministration(administrationEntity);

            if (administration.getDosage().hasRoute()) {
                ConceptEntity code = conceptDao.findAddCode(administration.getDosage().getRoute().getCodingFirstRep());
                if (code != null) dosageEntity.setRouteCode(code);
            }

            if (administration.getDosage().hasText()) {
                dosageEntity.setDosageText(administration.getDosage().getText());
            }

            if (administration.getDosage().hasDose() ) {
                try {
                    SimpleQuantity qty = administration.getDosage().getDose();

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


            em.persist(dosageEntity);
        }

        return medicationAdministrationEntityToFHIRMedicationAdministrationTransformer.transform(administrationEntity);
    }

    @Override
    public List<MedicationAdministration> search(FhirContext ctx, ReferenceParam patient, TokenParam status, StringParam id, TokenParam identifier, TokenParam code, ReferenceParam medication) {
        List<MedicationAdministrationEntity> qryResults = searchEntity(ctx,patient, status,id,identifier,code,medication);
        List<MedicationAdministration> results = new ArrayList<>();

        for (MedicationAdministrationEntity medicationAdministrationIntoleranceEntity : qryResults) {
            MedicationAdministration medicationAdministration = medicationAdministrationEntityToFHIRMedicationAdministrationTransformer.transform(medicationAdministrationIntoleranceEntity);
            results.add(medicationAdministration);
        }

        return results;
    }

    @Override
    public List<MedicationAdministrationEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam status, StringParam resid, TokenParam identifier, TokenParam code, ReferenceParam medication) {
        List<MedicationAdministrationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<MedicationAdministrationEntity> criteria = builder.createQuery(MedicationAdministrationEntity.class);
        Root<MedicationAdministrationEntity> root = criteria.from(MedicationAdministrationEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<MedicationAdministration> results = new ArrayList<MedicationAdministration>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<MedicationAdministrationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<MedicationAdministrationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<MedicationAdministrationEntity, MedicationAdministrationIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (code!=null) {
            log.trace("Search on MedicationAdministration.medicationCode code = "+code.getValue());
            Join<MedicationAdministrationEntity, ConceptEntity> joinConcept = root.join("medicationCode", JoinType.LEFT);
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
        criteria.orderBy(builder.desc(root.get("effectiveStart")));
        TypedQuery<MedicationAdministrationEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        qryResults = typedQuery.getResultList();
        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(MedicationAdministrationEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public MedicationAdministrationEntity readEntity(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationAdministrationEntity medicationAdministration = (MedicationAdministrationEntity) em.find(MedicationAdministrationEntity.class, Long.parseLong(theId.getIdPart()));
            return medicationAdministration;
        }
        return null;
    }
}

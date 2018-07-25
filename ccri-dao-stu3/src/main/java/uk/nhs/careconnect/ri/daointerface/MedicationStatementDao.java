package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.daointerface.transforms.MedicationRequestEntityToFHIRMedicationStatementTransformer;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.entity.medicationStatement.MedicationStatementDosage;
import uk.nhs.careconnect.ri.entity.medicationStatement.MedicationStatementEntity;
import uk.nhs.careconnect.ri.entity.medicationStatement.MedicationStatementIdentifier;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    EncounterRepository encounterDao;

    @Autowired
    EpisodeOfCareRepository episodeDao;

    private static final Logger log = LoggerFactory.getLogger(MedicationStatementDao.class);

    @Autowired
    private MedicationRequestEntityToFHIRMedicationStatementTransformer
            medicationRequestEntityToFHIRMedicationStatementTransformer;

    @Override
    public void save(FhirContext ctx, MedicationStatementEntity statement) {

    }

    @Override
    public Long count() {
        // TODO this is a work around while the data examples are minimal
      return prescriptionDao.count();
    }


    @Override
    public MedicationStatementEntity readEntity(FhirContext ctx, IdType theId) {
        return null;

    }


    @Override
    public MedicationStatement read(FhirContext ctx,IdType theId) {

        MedicationRequestEntity statementEntity = prescriptionDao.readEntity(ctx, theId);

        return (statementEntity == null) ? null : medicationRequestEntityToFHIRMedicationStatementTransformer.transform(statementEntity);
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
                ConceptEntity code = conceptDao.findAddCode(statement.getMedicationCodeableConcept().getCoding().get(0));
                if (code != null) {
                    statementEntity.setMedicationCode(code);
                } else {
                    log.info("Code: Missing System/Code = " + statement.getMedicationCodeableConcept().getCoding().get(0).getSystem()
                            + " code = " + statement.getMedicationCodeableConcept().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + statement.getMedicationCodeableConcept().getCoding().get(0).getSystem()
                            + " code = " + statement.getMedicationCodeableConcept().getCoding().get(0).getCode());
                }
            } catch (Exception ex) {}
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
            Coding code = new Coding().setCode(statement.getTaken().toCode()).setSystem(statement.getTaken().getSystem());
            ConceptEntity codeEntity = conceptDao.findAddCode(code);
            if (codeEntity != null) statementEntity.setTakenCode(codeEntity);
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
            MedicationStatementIdentifier prescriptionIdentifier = null;

            for (MedicationStatementIdentifier orgSearch : statementEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    prescriptionIdentifier = orgSearch;
                    break;
                }
            }
            if (prescriptionIdentifier == null)  prescriptionIdentifier = new MedicationStatementIdentifier();

            prescriptionIdentifier.setValue(identifier.getValue());
            prescriptionIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            prescriptionIdentifier.setMedicationStatement(statementEntity);
            em.persist(prescriptionIdentifier);
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
        
        return null;
    }

    @Override
    public List<MedicationStatement> search(FhirContext ctx,ReferenceParam patient, DateRangeParam effectiveDate, TokenParam status, TokenParam resid, TokenParam identifier) {
        List<MedicationRequestEntity> prescriptions = prescriptionDao.searchEntity(ctx,patient,null,effectiveDate,status,null,resid,null);
        List<MedicationStatement> results = new ArrayList<>();

        for (MedicationRequestEntity statementEntity : prescriptions)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            MedicationStatement medicationStatement =  medicationRequestEntityToFHIRMedicationStatementTransformer.transform(statementEntity);
            results.add(medicationStatement);
        }
        return results;

    }

    @Override
    public List<MedicationStatementEntity> searchEntity(FhirContext ctx,ReferenceParam patient, DateRangeParam effectiveDate, TokenParam status, TokenParam resid, TokenParam identifier) {
        return null;
    }
}

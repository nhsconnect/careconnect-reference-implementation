package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.transforms.*;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.carePlan.CarePlanEntity;
import uk.nhs.careconnect.ri.database.entity.composition.CompositionEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.diagnosticReport.DiagnosticReportEntity;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.*;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.immunisation.ImmunisationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationAdministration.MedicationAdministrationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationDispense.MedicationDispenseEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;
import uk.nhs.careconnect.ri.database.entity.referral.ReferralRequestEntity;
import uk.nhs.careconnect.ri.database.entity.riskAssessment.RiskAssessmentEntity;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.nhs.careconnect.ri.database.entity.list.ListEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.database.entity.relatedPerson.RelatedPersonEntity;


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
public class  EncounterDao implements EncounterRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private EncounterEntityToFHIREncounterTransformer encounterEntityToFHIREncounterTransformer;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    RelatedPersonRepository personDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    LocationRepository locationDao;

    @Autowired
    ConditionRepository conditionDao;

    @Autowired
    EpisodeOfCareRepository episodeDao;

    @Autowired
    private LibDao libDao;

    @Autowired
    ProcedureEntityToFHIRProcedureTransformer procedureEntityToFHIRProcedureTransformer;

    @Autowired
    private ObservationEntityToFHIRObservationTransformer observationEntityToFHIRObservationTransformer;

    @Autowired
    ConditionEntityToFHIRConditionTransformer conditionEntityToFHIRConditionTransformer;

    @Autowired
    private OrganisationEntityToFHIROrganizationTransformer organisationEntityToFHIROrganizationTransformer;

    @Autowired
    private PractitionerEntityToFHIRPractitionerTransformer practitionerEntityToFHIRPractitionerTransformer;

    @Autowired
    private LocationEntityToFHIRLocationTransformer locationEntityToFHIRLocationTransformer;

    @Autowired
    private PatientEntityToFHIRPatientTransformer patientEntityToFHIRPatientTransformer;

    @Autowired
    private CompositionEntityToFHIRCompositionTransformer compositionEntityToFHIRCompositionTransformer;

    @Autowired
    private ReferralRequestEntityToFHIRReferralRequestTransformer referralRequestEntityToFHIRReferralRequestTransformer;

    @Autowired
    private MedicationRequestEntityToFHIRMedicationRequestTransformer
            medicationRequestEntityToFHIRMedicationRequestTransformer;

    @Autowired
    private MedicationEntityToFHIRMedicationTransformer
            medicationEntityToFHIRMedicationTransformer;

    @Autowired
    private MedicationAdministrationEntityToFHIRMedicationAdministrationTransformer medicationAdministrationEntityToFHIRMedicationAdministrationTransformer;

    @Autowired
    private MedicationDispenseEntityToFHIRMedicationDispenseTransformer medicationDispenseEntityToFHIRMedicationDispenseTransformer;


    @Autowired
    private RelatedPersonEntityToFHIRRelatedPersonTransformer
        relatedPersonEntityToFHIRRelatedPersonTransformer;


    @Autowired
    CarePlanEntityToFHIRCarePlanTransformer carePlanIntoleranceEntityToFHIRCarePlanTransformer;

    @Autowired
    DiagnosticReportEntityToFHIRDiagnosticReportTransformer diagnosticReportEntityToFHIRDiagnosticReportTransformer;

    // Docs
    @Autowired
    DocumentReferenceEntityToFHIRDocumentReferenceTransformer documentReferenceEntityToFHIRDocumentReferenceTransformer;

    // Imms

    @Autowired
    ImmunisationEntityToFHIRImmunizationTransformer immunisationEntityToFHIRImmunizationTransformer;

    // List

    @Autowired
    ListEntityToFHIRListResourceTransformer listEntityToFHIRListResourceTransformer;

    // QuestionnaireResponse

    @Autowired
    QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer questionnaireResponseEntityToFHIRQuestionnaireResponseTransformer;

    // Risk Assessment
    @Autowired
    RiskAssessmentEntityToFHIRRiskAssessmentTransformer riskAssessmentEntityToFHIRRiskAssessmentTransformer;


    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(EncounterDao.class);


    @Override
    public void save(FhirContext ctx, EncounterEntity encounter) {

    }

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(EncounterEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public Encounter read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            EncounterEntity encounter = (EncounterEntity) em.find(EncounterEntity.class, Long.parseLong(theId.getIdPart()));

            return encounter == null
                    ? null
                    : encounterEntityToFHIREncounterTransformer.transform(encounter);
        } else {
            return null;
        }
    }

    @Override
    public EncounterEntity readEntity(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            EncounterEntity encounter = (EncounterEntity) em.find(EncounterEntity.class, Long.parseLong(theId.getIdPart()));

            return encounter;
        } else {
            return null;
        }
    }

    @Override
    public Encounter create(FhirContext ctx,Encounter encounter, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("Encounter.save");
      //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        EncounterEntity encounterEntity = null;

        if (encounter.hasId()) encounterEntity = readEntity(ctx, encounter.getIdElement());

        if (theConditional != null) {
            try {

                //CareConnectSystem.ODSOrganisationCode
                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/encounter")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<EncounterEntity> results = searchEntity(ctx, null, null,null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/encounter"),null, null, null,null, null);
                    for (EncounterEntity enc : results) {
                        encounterEntity = enc;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (encounterEntity == null) encounterEntity = new EncounterEntity();


        PatientEntity patientEntity = null;
        if (encounter.hasSubject()) {
            log.trace(encounter.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(encounter.getSubject().getReference()));
            encounterEntity.setPatient(patientEntity);
        }
        
        log.trace("checking for extension");
        if (encounter.hasExtension())
        {
        	log.trace("checking for extension-- has extension : " + encounter.getExtension().size() );
            for (Extension extension : encounter.getExtension()) {
            switch (extension.getUrl()) {	
            case CareConnectExtension.UrlServiceType :
            		log.trace("url is " + extension.getUrl());
            		
            		CodeableConcept extensionValue = (CodeableConcept) extension.getValue();
            		
            		log.trace("System/code  is : " + extensionValue.getCoding().get(0).getSystem());
            		ConceptEntity code = conceptDao.findAddCode(extensionValue.getCoding().get(0));
            		if (code != null) 
            			{ 
            				log.debug("the code found in the db");
            				encounterEntity.setServiceType(code);
            			}
            		else {

                        String message = "Encounter Extension: Missing System/Code = "+ extensionValue.getCoding().get(0).getSystem() +" code = "+extensionValue.getCoding().get(0).getCode();
                        log.error(message);
                        throw new OperationOutcomeException("Encounter",message, OperationOutcome.IssueType.CODEINVALID);
                    }
            		break;
            	}
            }
        }
        
        if (encounter.hasClass_()) {
            ConceptEntity code = conceptDao.findAddCode(encounter.getClass_());
            if (code != null) { encounterEntity._setClass(code); }
            else {
                log.info("Code: Missing System/Code = "+ encounter.getClass_().getSystem() +" code = "+encounter.getClass_().getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ encounter.getClass_().getSystem() +" code = "+encounter.getClass_().getCode());
            }
        }

        if (encounter.hasType()) {
            ConceptEntity code = conceptDao.findAddCode(encounter.getType().get(0).getCoding().get(0));
            if (code != null) { encounterEntity.setType(code); }
            else {
                log.info("Code: Missing System/Code = "+encounter.getType().get(0).getCoding().get(0).getSystem() +" code = "+encounter.getType().get(0).getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ encounter.getType().get(0).getCoding().get(0).getSystem() +" code = "+encounter.getType().get(0).getCoding().get(0).getCode());
            }
        }

        if (encounter.hasStatus()) {
            encounterEntity.setStatus(encounter.getStatus());
        }



        if (encounter.hasPeriod()) {

            if (encounter.getPeriod().hasStart()) {
                encounterEntity.setPeriodStartDate(encounter.getPeriod().getStart());

            if (encounter.getPeriod().hasEnd()) {
                if (encounter.getPeriod().getEnd().after(encounter.getPeriod().getStart()))
                encounterEntity.setPeriodEndDate(encounter.getPeriod().getEnd());
                else
                    encounterEntity.setPeriodEndDate(null); // KGM 15/12/2017 Ensure end date is after start date , if not set end date to null
            }
            }
        }

        if (encounter.hasPriority()) {
            ConceptEntity code = conceptDao.findCode(encounter.getPriority().getCoding().get(0));
            if (code != null) { encounterEntity.setPriority(code); }
            else {

                String message = "Code: Missing System/Code = "+encounter.getPriority().getCoding().get(0).getSystem() +" code = "+encounter.getPriority().getCoding().get(0).getCode();
                log.error(message);
                throw new OperationOutcomeException("Patient",message, OperationOutcome.IssueType.CODEINVALID);

            }
        }

        if (encounter.hasPartOf()) {
            EncounterEntity partOf = readEntity(ctx, new IdType(encounter.getPartOf().getReference()));
            if (partOf != null) encounterEntity.setPartOfEncounter(partOf);
        }

        if (encounter.hasServiceProvider()) {

            log.debug("encounter.getServiceProvider().getReference=" + (encounter.getServiceProvider().getReference()));

            OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(encounter.getServiceProvider().getReference()));
            if (organisationEntity != null) encounterEntity.setServiceProvider(organisationEntity);
        }

        em.persist(encounterEntity);

        for (Identifier identifier : encounter.getIdentifier()) {
            EncounterIdentifier encounterIdentifier = null;

            for (EncounterIdentifier orgSearch : encounterEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    encounterIdentifier = orgSearch;
                    break;
                }
            }
            if (encounterIdentifier == null)  encounterIdentifier = new EncounterIdentifier();

            if (identifier.hasValue()) encounterIdentifier.setValue(identifier.getValue());
            if (identifier.hasSystem()) encounterIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            encounterIdentifier.setEncounter(encounterEntity);
            em.persist(encounterIdentifier);
        }

        for (EncounterDiagnosis searchEncounter : encounterEntity.getDiagnoses()) {
            em.remove(searchEncounter);
        }
        for (Encounter.DiagnosisComponent component:encounter.getDiagnosis()) {

            EncounterDiagnosis encounterDiagnosis = null;
            /*
            for (EncounterDiagnosis searchEncounter : encounterEntity.getDiagnoses()) {
                if (searchEncounter.getCondition().getId().equals(component.getCondition().getIdElement().getValue())) {
                    encounterDiagnosis = searchEncounter;
                    break;
                }
            }*/
            if (encounterDiagnosis == null) {
                encounterDiagnosis= new EncounterDiagnosis();
                encounterDiagnosis.setEncounter(encounterEntity);
                ConditionEntity condition = conditionDao.readEntity(ctx,new IdType(component.getCondition().getReference()));
                encounterDiagnosis.setCondition(condition);
                em.persist(encounterDiagnosis);
            }

        }

        if (encounter.hasParticipant()) {

            for ( EncounterParticipant encounterParticipantDel : encounterEntity.getParticipants()) {
                em.remove(encounterParticipantDel);
            }

            for(Encounter.EncounterParticipantComponent participant : encounter.getParticipant()) {

                EncounterParticipant encounterParticipant = new EncounterParticipant();

                encounterParticipant.setEncounter(encounterEntity);

                if (participant.getIndividual().getReference().contains("Practitioner")) {

                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx,new IdType("Practitioner/"+participant.getIndividual().getReference()));
                    if (practitionerEntity != null ) encounterParticipant.setParticipant(practitionerEntity);
                }
                if (participant.getIndividual().getReference().contains("RelatedPerson")) {

                    RelatedPersonEntity personEntity = personDao.readEntity(ctx,new IdType("RelatedPerson/"+participant.getIndividual().getReference()));
                    if (personEntity != null ) encounterParticipant.setPerson(personEntity);
                }

                if (participant.hasType()) {
                    ConceptEntity code = conceptDao.findAddCode(participant.getType().get(0).getCoding().get(0));
                    if (code != null) {
                        encounterParticipant.setParticipantType(code);
                    } else {
                        String message = "Code: Missing System/Code = "+participant.getType().get(0).getCoding().get(0).getSystem() +" code = "+participant.getType().get(0).getCoding().get(0).getCode();
                        log.error(message);
                        throw new OperationOutcomeException("Patient",message, OperationOutcome.IssueType.CODEINVALID);
                    }

                }
                em.persist(encounterParticipant);

            }
        }

        if (encounter.hasLocation()) {
            for (EncounterLocation encounterLocation : encounterEntity.getLocations()) {
                em.remove(encounterLocation);
            }
            for (Encounter.EncounterLocationComponent locationComponent : encounter.getLocation()) {
                EncounterLocation encounterLocation = new EncounterLocation();
                encounterLocation.setEncounter(encounterEntity);
                if (locationComponent.hasLocation()) {
                    LocationEntity locationEntity = locationDao.readEntity(ctx, new IdType(locationComponent.getLocation().getReference()));
                    if (locationEntity == null) {
                        throw new OperationOutcomeException("Encounter","Encounter missing location", OperationOutcome.IssueType.CODEINVALID);
                    }
                    encounterLocation.setLocation(locationEntity);
                }
                if (locationComponent.hasStatus()) {
                    encounterLocation.setStatus(locationComponent.getStatus());
                }
                if (locationComponent.hasPeriod()) {
                    if (locationComponent.getPeriod().hasStart()) {
                        encounterLocation.setPeriodStartDate(locationComponent.getPeriod().getStart());
                    }
                    if (locationComponent.getPeriod().hasEnd()) {
                        encounterLocation.setPeriodEndDate(locationComponent.getPeriod().getEnd());
                    }
                }
                em.persist(encounterLocation);
                encounterEntity.getLocations().add(encounterLocation);
            }
        }

        for (EncounterEpisode encounterEpisode : encounterEntity.getEpisodes()) {
            em.remove(encounterEpisode);
        }
        if (encounter.hasEpisodeOfCare()) {
            for (Reference reference : encounter.getEpisodeOfCare()) {
                EncounterEpisode encounterEpisode = new EncounterEpisode();
                encounterEpisode.setEncounter(encounterEntity);

                EpisodeOfCareEntity episode = episodeDao.readEntity(ctx,new IdType(reference.getReference()));
                encounterEpisode.setEpisode(episode);
                em.persist(encounterEpisode);

            }

        }

        return encounterEntityToFHIREncounterTransformer.transform(encounterEntity);
    }

    @Override
    public List<Resource> search(FhirContext ctx, ReferenceParam patient, DateRangeParam date, ReferenceParam episode, TokenParam identifier, StringParam resid, Set<Include> reverseIncludes, Set<Include> includes
            , @OptionalParam(name = Encounter.SP_TYPE) TokenParam type
            , @OptionalParam(name = Encounter.SP_STATUS) TokenParam status
    ) {
        List<EncounterEntity> qryResults = searchEntity(ctx,patient, date, episode, identifier,resid,reverseIncludes,includes, type,status);
        List<Resource> results = new ArrayList<>();

        for (EncounterEntity encounterEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Encounter encounter = encounterEntityToFHIREncounterTransformer.transform(encounterEntity);
            results.add(encounter);
        }
        // If reverse include selected
        if (reverseIncludes!= null) {
            //log.info("Reverse includes");
            for (EncounterEntity encounterEntity : qryResults) {
                for (Include include : reverseIncludes) {
                    switch(include.getValue()) {

                        case "*":
                            for (ProcedureEntity procedureEntity : encounterEntity.getProcedureEncounters()) {
                                addToResults(results, procedureEntityToFHIRProcedureTransformer.transform(procedureEntity));
                            }
                            for (ObservationEntity observationEntity : encounterEntity.getObservationEncounters()) {
                                addToResults(results, observationEntityToFHIRObservationTransformer.transform(observationEntity));
                            }
                            for (ConditionEntity conditionEntity : encounterEntity.getConditionEncounters()) {
                                addToResults(results, conditionEntityToFHIRConditionTransformer.transform(conditionEntity));
                            }


                            for (CarePlanEntity carePlanEntity : encounterEntity.getCarePlans()) {
                                addToResults(results, carePlanIntoleranceEntityToFHIRCarePlanTransformer.transform(carePlanEntity));
                            }
                            for (DiagnosticReportEntity diagnosticReportEntity : encounterEntity.getDiagnosticReports()) {
                                addToResults(results, diagnosticReportEntityToFHIRDiagnosticReportTransformer.transform(diagnosticReportEntity));
                            }

                            // Docs
                            for (DocumentReferenceEntity documentReferenceEntity : encounterEntity.getDocuments()) {
                                addToResults(results, documentReferenceEntityToFHIRDocumentReferenceTransformer.transform(documentReferenceEntity));
                            }

                            // Imms
                            for (ImmunisationEntity immunisationEntity : encounterEntity.getImmunisations()) {
                                addToResults(results, immunisationEntityToFHIRImmunizationTransformer.transform(immunisationEntity));
                            }

                            // List
                            for (ListEntity listEntity : encounterEntity.getLists()) {
                                addToResults(results, listEntityToFHIRListResourceTransformer.transform(listEntity));

                            }
                            // Meds
                            for (MedicationRequestEntity medicationRequestEntity : encounterEntity.getMedicationRequestEncounters()) {
                                addToResults(results, medicationRequestEntityToFHIRMedicationRequestTransformer.transform(medicationRequestEntity));
                                if (medicationRequestEntity.getMedicationEntity() != null) {
                                    addToResults(results, medicationEntityToFHIRMedicationTransformer.transform(medicationRequestEntity.getMedicationEntity()));
                                }
                            }
                            for (MedicationDispenseEntity medicationDispenseEntity : encounterEntity.getMedicationDisenseEncounters()) {
                                addToResults(results, medicationDispenseEntityToFHIRMedicationDispenseTransformer.transform(medicationDispenseEntity));
                                if (medicationDispenseEntity.getMedicationEntity() != null) {
                                    addToResults(results, medicationEntityToFHIRMedicationTransformer.transform(medicationDispenseEntity.getMedicationEntity()));
                                }
                            }

                            for (MedicationAdministrationEntity medicationAdministrationEntity : encounterEntity.getMedicationAdministrationEncounters()) {
                                addToResults(results, medicationAdministrationEntityToFHIRMedicationAdministrationTransformer.transform(medicationAdministrationEntity));
                                if (medicationAdministrationEntity.getMedicationEntity() != null) {
                                    addToResults(results, medicationEntityToFHIRMedicationTransformer.transform(medicationAdministrationEntity.getMedicationEntity()));
                                }
                            }

                            // QuestionnaireResponse
                            for (QuestionnaireResponseEntity questionnaireResponseEntity : encounterEntity.getForms()) {
                                addToResults(results, questionnaireResponseEntityToFHIRQuestionnaireResponseTransformer.transform(questionnaireResponseEntity));
                            }

                            // Risk Assessment
                            for (RiskAssessmentEntity riskAssessmentEntity : encounterEntity.getRisks()) {
                                addToResults(results, riskAssessmentEntityToFHIRRiskAssessmentTransformer.transform(riskAssessmentEntity));
                            }
                            // Referrals
                            for (ReferralRequestEntity referralRequestEntity : encounterEntity.getReferrals()) {
                                addToResults(results, referralRequestEntityToFHIRReferralRequestTransformer.transform(referralRequestEntity));
                            }

                            for (EncounterEntity encounterEntityChild : encounterEntity.getChildEncounters()) {
                                addToResults(results, encounterEntityToFHIREncounterTransformer.transform(encounterEntityChild));
                            }


                            break;

                        case "Composition:encounter" :
                            for (CompositionEntity compositionEntity : encounterEntity.getCompositions()) {
                                addToResults(results, compositionEntityToFHIRCompositionTransformer.transform(compositionEntity));
                            }
                            break;
                        case "Encounter:part-of":
                            for (EncounterEntity encounterEntityChild : encounterEntity.getChildEncounters()) {
                                addToResults(results, encounterEntityToFHIREncounterTransformer.transform(encounterEntityChild));
                            }
                            break;

                        case "Condition:context":
                            for (ConditionEntity conditionEntity : encounterEntity.getConditionEncounters()) {
                                addToResults(results, conditionEntityToFHIRConditionTransformer.transform(conditionEntity));
                            }
                            break;


                        case "DocumentReference:context" :
                            for (DocumentReferenceEntity documentReferenceEntity : encounterEntity.getDocuments()) {
                                addToResults(results, documentReferenceEntityToFHIRDocumentReferenceTransformer.transform(documentReferenceEntity));
                            }
                            break;
                        case "Immunization:encounter" :
                            // Imms
                            for (ImmunisationEntity immunisationEntity : encounterEntity.getImmunisations()) {
                                addToResults(results, immunisationEntityToFHIRImmunizationTransformer.transform(immunisationEntity));
                            }
                            break;
                        case "MedicationDispense:context":
                            for (MedicationDispenseEntity medicationDispenseEntity : encounterEntity.getMedicationDisenseEncounters()) {
                                addToResults(results, medicationDispenseEntityToFHIRMedicationDispenseTransformer.transform(medicationDispenseEntity));
                                if (medicationDispenseEntity.getMedicationEntity() != null) {
                                    addToResults(results, medicationEntityToFHIRMedicationTransformer.transform(medicationDispenseEntity.getMedicationEntity()));
                                }
                            }
                            break;
                        case "MedicationAdministration:context":
                            for (MedicationAdministrationEntity medicationAdministrationEntity : encounterEntity.getMedicationAdministrationEncounters()) {
                                addToResults(results, medicationAdministrationEntityToFHIRMedicationAdministrationTransformer.transform(medicationAdministrationEntity));
                                if (medicationAdministrationEntity.getMedicationEntity() != null) {
                                    addToResults(results, medicationEntityToFHIRMedicationTransformer.transform(medicationAdministrationEntity.getMedicationEntity()));
                                }
                            }
                            break;
                        case "MedicationRequest:context":
                            for (MedicationRequestEntity medicationRequestEntity : encounterEntity.getMedicationRequestEncounters()) {
                                addToResults(results, medicationRequestEntityToFHIRMedicationRequestTransformer.transform(medicationRequestEntity));
                                if (medicationRequestEntity.getMedicationEntity() != null) {
                                    addToResults(results, medicationEntityToFHIRMedicationTransformer.transform(medicationRequestEntity.getMedicationEntity()));
                                }
                            }
                            break;
                        case "Observation:context":
                            for (ObservationEntity observationEntity : encounterEntity.getObservationEncounters()) {
                                if (observationEntity.getParentObservation() == null) {
                                    addToResults(results, observationEntityToFHIRObservationTransformer.transform(observationEntity));
                                }
                            }
                            break;
                        case "Procedure:context":
                            for (ProcedureEntity procedureEntity : encounterEntity.getProcedureEncounters()) {
                                addToResults(results, procedureEntityToFHIRProcedureTransformer.transform(procedureEntity));
                            }
                            break;

                        case "ReferralRequest:encounter" :
                            for (ReferralRequestEntity referralRequestEntity : encounterEntity.getReferrals()) {
                                addToResults(results,referralRequestEntityToFHIRReferralRequestTransformer.transform(referralRequestEntity) );
                            }
                            break;
                    }

                }
            }
        }

        if (includes !=null) {
            for (EncounterEntity encounterEntity : qryResults) {
                for (Include include : includes) {
                    switch(include.getValue()) {
                        case
                            "Encounter:participant":
                            for (EncounterParticipant encounterParticipant : encounterEntity.getParticipants()) {
                                if (encounterParticipant.getParticipant() != null) {
                                    addToResults(results, practitionerEntityToFHIRPractitionerTransformer.transform(encounterParticipant.getParticipant()));
                                }
                                if (encounterParticipant.getPerson() != null) {
                                    addToResults(results, relatedPersonEntityToFHIRRelatedPersonTransformer.transform(encounterParticipant.getPerson()));
                                }
                            }
                            break;
                        case
                            "Encounter:patient":
                            if (encounterEntity.getPatient()!=null) {
                                PatientEntity patientEntity = encounterEntity.getPatient();
                                addToResults(results,patientEntityToFHIRPatientTransformer.transform(patientEntity));
                                /*
                                if (patientEntity.getGP()!=null) {
                                    addToResults(results,practitionerEntityToFHIRPractitionerTransformer.transform(patientEntity.getGP()));
                                }
                                if (patientEntity.getPractice()!=null) {
                                    addToResults(results,organisationEntityToFHIROrganizationTransformer.transform(patientEntity.getPractice()));
                                }
                                */
                            }
                            break;
                        case "Encounter:service-provider":
                            if (encounterEntity.getServiceProvider()!=null) {
                                addToResults(results,organisationEntityToFHIROrganizationTransformer.transform(encounterEntity.getServiceProvider()));
                            }
                            break;
                        case "Encounter:location":
                            if (encounterEntity.getLocations()!=null) {
                                for (EncounterLocation encounterLocation : encounterEntity.getLocations()) {
                                    addToResults(results, locationEntityToFHIRLocationTransformer.transform(encounterLocation.getLocation()));
                                }
                            }
                            break;
                        case "*":
                                if (encounterEntity.getServiceProvider()!=null) {
                                    addToResults(results,organisationEntityToFHIROrganizationTransformer.transform(encounterEntity.getServiceProvider()));
                                }
                                if (encounterEntity.getLocations()!=null) {
                                    for (EncounterLocation encounterLocation : encounterEntity.getLocations()) {
                                        addToResults(results, locationEntityToFHIRLocationTransformer.transform(encounterLocation.getLocation()));
                                    }
                                }
                                for (EncounterParticipant encounterParticipant : encounterEntity.getParticipants()) {
                                    if (encounterParticipant.getParticipant() != null) {
                                        addToResults(results, practitionerEntityToFHIRPractitionerTransformer.transform(encounterParticipant.getParticipant()));
                                    }
                                    if (encounterParticipant.getPerson() != null) {
                                        addToResults(results, relatedPersonEntityToFHIRRelatedPersonTransformer.transform(encounterParticipant.getPerson()));
                                    }
                                }
                                if (encounterEntity.getPatient()!=null) {
                                    PatientEntity patientEntity = encounterEntity.getPatient();
                                    addToResults(results,patientEntityToFHIRPatientTransformer.transform(patientEntity));
                                    if (patientEntity.getGP()!=null) {
                                        addToResults(results,practitionerEntityToFHIRPractitionerTransformer.transform(patientEntity.getGP()));
                                    }
                                    if (patientEntity.getPractice()!=null) {
                                        addToResults(results,organisationEntityToFHIROrganizationTransformer.transform(patientEntity.getPractice()));
                                    }
                                }
                            break;

                    }
                }
            }
        }


        return results;
    }
    private void addToResults(List<Resource> results, Resource resource) {
        Boolean found = false;
        for (Resource resourceSearch : results) {
            if (resourceSearch.getId().equals(resource.getId()) && resourceSearch.getResourceType().equals(resource.getResourceType())) found = true;
        }
        if (!found) results.add(resource);
    }

    @Override
    public List<EncounterEntity> searchEntity(FhirContext ctx,ReferenceParam patient, DateRangeParam date, ReferenceParam episode, TokenParam identifier,StringParam resid,Set<Include> reverseIncludes, Set<Include> includes
            , @OptionalParam(name = Encounter.SP_TYPE) TokenParam type
            , @OptionalParam(name = Encounter.SP_STATUS) TokenParam status
    ) {
        List<EncounterEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<EncounterEntity> criteria = builder.createQuery(EncounterEntity.class);
        Root<EncounterEntity> root = criteria.from(EncounterEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Encounter> results = new ArrayList<Encounter>();


        if (identifier !=null)
        {
            Join<EncounterEntity, EncounterIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<EncounterEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<EncounterEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (type != null) {
            log.trace("Search on Encounter.type code = "+type.getValue());

            Join<EncounterEntity, ConceptEntity> joinConcept = root.join("type", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),type.getValue());
            predList.add(p);
        }

        if (status != null) {
            Integer encounterstatus = null;
            switch (status.getValue().toLowerCase()) {
                case "planned":
                    encounterstatus = 0;
                    break;
                case "arrived":
                    encounterstatus = 1;
                    break;
                case "triaged":
                    encounterstatus = 2;
                    break;
                case "in-progress":
                    encounterstatus = 3;
                    break;
                case "onleave":
                    encounterstatus = 4;
                    break;
                case "finished":
                    encounterstatus = 5;
                    break;
                case "cancelled":
                    encounterstatus = 6;
                    break;
                default:
                    encounterstatus=-1;
            }


            Predicate p = builder.equal(root.get("status"), encounterstatus);
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
                    case GREATERTHAN:
                        /*{
                        Predicate p = builder.greaterThan(root.<Date>get("periodStartDate"), parameterLower);
                        predList.add(p);

                        break;
                    }*/
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("periodStartDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("periodStartDate"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("periodStartDate"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("periodStartDate"), parameterLower);
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
                        Predicate pupper = builder.lessThan(root.<Date>get("periodStartDate"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("periodStartDate"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("periodStartDate"), parameterUpper);
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
        criteria.orderBy(builder.desc(root.get("periodStartDate")));

        TypedQuery<EncounterEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

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

package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.ObservationEntityToFHIRObservationTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterParticipant;
import uk.nhs.careconnect.ri.database.entity.observation.*;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.observation.*;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;

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
public class ObservationDao implements ObservationRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    @Lazy
    PatientRepository patientDao;

    @Autowired
    @Lazy
    PractitionerRepository practitionerDao;

    @Autowired
    @Lazy
    OrganisationRepository organisationDao;

    @Autowired
    @Lazy
    QuestionnaireResponseRepository formDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(Observation.class);

    @Autowired
    private ObservationEntityToFHIRObservationTransformer observationEntityToFHIRObservationTransformer;

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();

        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        Root<ObservationEntity> root = cq.from(ObservationEntity.class);
        cq.select(qb.count(root));
        cq.where(qb.isNull(root.get("parentObservation")));
        return em.createQuery(cq).getSingleResult();
    }

    private ObservationEntity searchAndCreateComponentObservation(ObservationEntity parentEntity, ConceptEntity code) {
        // Look for previous entries with this code.
        ObservationEntity observationComponent = null;
        for (ObservationEntity observationEntityComponent : parentEntity.getComponents()) {
            if (code.getCode().equals(observationEntityComponent.getCode().getCode())
                    && code.getSystem().equals(observationEntityComponent.getCode().getSystem())) {
                observationComponent = observationEntityComponent;
            }
        }

        if (observationComponent==null) observationComponent = new ObservationEntity();

        return observationComponent;
    }

    @Override
    public Observation save(FhirContext ctx, Observation observation, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException {

     //   System.out.println("In ObservationDao.save");
        log.debug("Observation.save");
        ObservationEntity observationEntity = null;

        if (theId != null && daoutils.isNumeric(theId.getIdPart())) {
            log.info("theId.getIdPart()="+theId.getIdPart());
            observationEntity = (ObservationEntity) em.find(ObservationEntity.class, Long.parseLong(theId.getIdPart()));
        }

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/observation")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<ObservationEntity> results = searchEntity(ctx, null, null,null, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/observation"),null,null,null);
                    for (ObservationEntity con : results) {
                        observationEntity = con;
                        break;
                    }
                } else {
                    if (theConditional.contains("fhir.health.phr.example.com/Id/observation")) {
                        URI uri = new URI(theConditional);

                        String scheme = uri.getScheme();
                        String host = uri.getHost();
                        String query = uri.getRawQuery();
                        log.debug(query);
                        String[] spiltStr = query.split("%7C");
                        log.debug(spiltStr[1]);

                        List<ObservationEntity> results = searchEntity(ctx, null, null,null, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.health.phr.example.com/Id/observation"),null,null, null);
                        for (ObservationEntity con : results) {
                            observationEntity = con;
                            break;
                        }
                    } else {
                        log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                    }
                }

            } catch (Exception ex) {

            }
        }

        if (observationEntity == null) observationEntity = new ObservationEntity();

        observationEntity.setStatus(observation.getStatus());

        if (observation.hasCode()) {
          ConceptEntity code = conceptDao.findAddCode(observation.getCode().getCoding().get(0));
          if (code != null) { observationEntity.setCode(code); }
          else {
              log.info("Code: Missing System/Code = "+ observation.getCode().getCoding().get(0).getSystem() +" code = "+observation.getCode().getCoding().get(0).getCode());

              throw new IllegalArgumentException("Missing System/Code = "+ observation.getCode().getCoding().get(0).getSystem() +" code = "+observation.getCode().getCoding().get(0).getCode());
          }

        }
        if (observation.hasEffectiveDateTimeType()) {
            try {
                observationEntity.setEffectiveDateTime(observation.getEffectiveDateTimeType().getValue());
            } catch (Exception ex) {

            }
        }
        if (observation.hasIssued()) {
            observationEntity.setIssued(observation.getIssued());
        }

        PatientEntity patientEntity = null;
        if (observation.hasSubject()) {
            log.trace(observation.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(observation.getSubject().getReference()));
            observationEntity.setPatient(patientEntity);
        }
        // KGM 18/1/2018
        if (observation.hasContext()) {
            if (observation.getContext().getReference().contains("Encounter")) {
                EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(observation.getContext().getReference()));
                observationEntity.setContext(encounterEntity);
            }
        }
        try {
            if (observation.hasValueQuantity()) {

                observationEntity.setValueQuantity(observation.getValueQuantity().getValue());

                if (observation.getValueQuantity().getCode() != null) {
                    ConceptEntity concept = conceptDao.findAddCode(observation.getValueQuantity());
                    if (concept != null) observationEntity.setValueUnitOfMeasure(concept);

                }
            }
        } catch (Exception ex) { }

        // Body Site

        if (observation.hasBodySite()) {
            ConceptEntity code = conceptDao.findAddCode(observation.getBodySite().getCoding().get(0));
            if (code != null) { observationEntity.setBodySite(code); }
            else {
                log.info("Body: Missing System/Code = "+ observation.getBodySite().getCoding().get(0).getSystem() +" code = "+observation.getBodySite().getCoding().get(0).getCode());
                throw new IllegalArgumentException("Missing System/Code = "+ observation.getBodySite().getCoding().get(0).getSystem() +" code = "+observation.getBodySite().getCoding().get(0).getCode());
            }

        }

        // Method

        if (observation.hasMethod()) {
            ConceptEntity code = conceptDao.findAddCode(observation.getMethod().getCoding().get(0));
            if (code != null) { observationEntity.setMethod(code); }
            else {
                log.info("Method: Missing System/Code = "+ observation.getMethod().getCoding().get(0).getSystem() +" code = "+observation.getMethod().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ observation.getMethod().getCoding().get(0).getSystem() +" code = "+observation.getMethod().getCoding().get(0).getCode());
            }

        }

        // Interpretation

        if (observation.hasInterpretation()) {
            ConceptEntity code = conceptDao.findAddCode(observation.getInterpretation().getCoding().get(0));
            if (code != null) { observationEntity.setInterpretation(code); }
            else {
                log.error("Interpretation: Missing System/Code = "+ observation.getInterpretation().getCoding().get(0).getSystem() +" code = "+observation.getInterpretation().getCoding().get(0).getCode());
                throw new IllegalArgumentException("Missing System/Code = "+ observation.getInterpretation().getCoding().get(0).getSystem() +" code = "+observation.getInterpretation().getCoding().get(0).getCode());
            }

        }

        if (observation.hasComment()) {
            observationEntity.setComments(observation.getComment());
        }

        em.persist(observationEntity);

        /* Identity */

        for (Identifier identifier : observation.getIdentifier()) {


            ObservationIdentifier observationIdentifier = null;
            for (ObservationIdentifier orgSearch : observationEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    observationIdentifier = orgSearch;
                    break;
                }
            }
            if (observationIdentifier == null)  observationIdentifier = new ObservationIdentifier();

            observationIdentifier.setValue(identifier.getValue());
            observationIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            observationIdentifier.setObservation(observationEntity);
            em.persist(observationIdentifier);

        }

        // Range

        // Clear down existing ranges, no suitable (simple) identifier to check if already exists
        for (ObservationRange rangeEntity :observationEntity.getRanges() ) {
            em.remove(rangeEntity);
        }
        observationEntity.setRanges(new HashSet<>());

        for (Observation.ObservationReferenceRangeComponent range : observation.getReferenceRange()) {
            log.trace("Observation Range Found");

            ObservationRange rangeEntity = new ObservationRange();
            rangeEntity
                    .setObservation(observationEntity);
            if (range.hasLow()) rangeEntity.setLowQuantity(range.getLow().getValue());
            if (range.hasHigh()) rangeEntity.setHighQuantity(range.getHigh().getValue());
            if (range.hasType()) {
                ConceptEntity code = conceptDao.findAddCode(range.getType().getCoding().get(0));
                if (code != null) { rangeEntity.setType(code); }
                else {
                    log.info("Range: Missing System/Code = "+ range.getType().getCoding().get(0).getSystem() +" code = "+range.getType().getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ range.getType().getCoding().get(0).getSystem() +" code = "+range.getType().getCoding().get(0).getCode());
                }
            }
            log.trace(" ** Range Persist ** ");
            em.persist(rangeEntity);
            observationEntity.getRanges().add(rangeEntity);
        }


        for (Reference reference : observation.getPerformer()) {

            log.trace("Reference Typez = "+reference.getReferenceElement().getResourceType());
            switch (reference.getReferenceElement().getResourceType()) {
                case "Practitioner" :
                    log.trace("Practitioner DAO :"+reference.getReferenceElement().getResourceType());
                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(reference.getReference()));
                    if (practitionerEntity != null) {
                        ObservationPerformer performer = null;
                        for (ObservationPerformer performerSearch : observationEntity.getPerformers()) {
                            if (performerSearch.getPerformerPractitioner().getId().equals(practitionerEntity.getId())) {
                                performer = performerSearch;
                            }
                        }
                        if (performer == null) performer = new ObservationPerformer();
                        performer.setPerformerType(ObservationPerformer.performer.Practitioner);
                        performer.setPerformerPractitioner(practitionerEntity);
                        performer.setObservation(observationEntity);
                        em.persist(performer);
                        observationEntity.getPerformers().add(performer);
                    }
                    break;
                case "Patient":
                    log.trace("Patient DAO :"+reference.getReferenceElement().getResourceType());
                    PatientEntity patientPerformerEntity = patientDao.readEntity(ctx, new IdType(reference.getReference()));
                    if (patientEntity != null) {
                        ObservationPerformer performer = null;
                        for (ObservationPerformer performerSearch : observationEntity.getPerformers()) {
                            if (performerSearch.getPerformerPatient().getId().equals(patientPerformerEntity.getId())) {
                                performer = performerSearch;
                            }
                        }
                        if (performer == null) performer = new ObservationPerformer();
                        performer.setPerformerType(ObservationPerformer.performer.Patient);
                        performer.setPerformerPatient(patientPerformerEntity);
                        performer.setObservation(observationEntity);
                        em.persist(performer);
                        observationEntity.getPerformers().add(performer);
                    }
                    break;
                case "Organization":
                    OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(reference.getReference()));
                    if (organisationEntity != null) {
                        ObservationPerformer performer = null;
                        for (ObservationPerformer performerSearch : observationEntity.getPerformers()) {
                            if (performerSearch.getPerformerOrganisation().getId().equals(organisationEntity.getId())) {
                                performer = performerSearch;
                            }
                        }
                        if (performer == null) performer = new ObservationPerformer();
                        performer.setPerformerType(ObservationPerformer.performer.Organisation);
                        performer.setPerformerOrganisation(organisationEntity);
                        performer.setObservation(observationEntity);
                        em.persist(performer);
                        observationEntity.getPerformers().add(performer);
                    }
                    break;
                default:
                    log.debug("Not found this :"+reference.getReferenceElement().getResourceType());
            }
        }

        for (CodeableConcept concept :observation.getCategory()) {
            // Category must have a code 15/Jan/2018 testing with Synthea examples
            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    ObservationCategory category = null;
                    // Look for existing categories
                    for (ObservationCategory cat :observationEntity.getCategories()) {
                        category= cat;
                    }
                    if (category == null) category = new ObservationCategory();

                    category.setCategory(conceptEntity);
                    category.setObservation(observationEntity);
                    em.persist(category);
                    observationEntity.getCategories().add(category);
                }
                else {
                    log.info("Missing Category. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }

        for (Observation.ObservationComponentComponent component :observation.getComponent()) {
            ConceptEntity codeComponent = null;
            if (component.hasCode()) {
                codeComponent = conceptDao.findAddCode(component.getCode().getCoding().get(0));
            }

            ObservationEntity observationComponent = searchAndCreateComponentObservation(observationEntity,codeComponent);

            if (patientEntity != null) observationComponent.setPatient(patientEntity);
            observationComponent.setObservationType(ObservationEntity.ObservationType.component);

            if (observation.hasEffectiveDateTimeType()) {
                try {
                    observationComponent.setEffectiveDateTime(observation.getEffectiveDateTimeType().getValue());
                } catch (Exception ex) {

                }
            }
            // Code
            if (component.hasCode()) {
                // ConceptEntity code = conceptDao.findCode(component.getCode().getCoding().get(0).getSystem(),component.getCode().getCoding().get(0).getCode());
                if (codeComponent != null) observationComponent.setCode(codeComponent);
            }
            // Value

            try {
                if (component.hasValueQuantity()) {

                    observationComponent.setValueQuantity(component.getValueQuantity().getValue());

                    if (component.getValueQuantity().getCode() != null) {
                        ConceptEntity concept = conceptDao.findAddCode(component.getValueQuantity());
                        if (concept != null) {
                            observationComponent.setValueUnitOfMeasure(concept);
                        }
                        else {
                            log.info("Missing component.getValueQuantity. System/Code = "+ component.getValueQuantity().getSystem() +" code = "+component.getValueQuantity().getCode());
                            throw new IllegalArgumentException("Missing System/Code = "+ component.getValueQuantity().getSystem() +" code = "+component.getValueQuantity().getCode());

                        }

                    }
                }
            } catch (Exception ex) { }


            observationComponent.setParentObservation(observationEntity);
            em.persist(observationComponent);

            // Store the ValueCodeableConcept which is a child of the component.

            if (component.hasValueCodeableConcept()) {

                // Code


                ConceptEntity codeValue = null;
                try {

                    if (component.getValueCodeableConcept().getCoding().get(0).hasCode()) {
                        CodeableConcept valueConcept = component.getValueCodeableConcept();
                        codeValue = conceptDao.findAddCode(valueConcept.getCoding().get(0));
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }

                ObservationEntity observationComponentValue = searchAndCreateComponentObservation(observationEntity,codeComponent);

                if (codeValue != null) observationComponentValue.setCode(codeValue);

                if (patientEntity != null) observationComponentValue.setPatient(patientEntity);
                observationComponentValue.setObservationType(ObservationEntity.ObservationType.valueQuantity);
                observationComponentValue.setParentObservation(observationComponent);

                if (observation.hasEffectiveDateTimeType()) {
                    try {
                        observationComponentValue.setEffectiveDateTime(observation.getEffectiveDateTimeType().getValue());
                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                    }
                }



                em.persist(observationComponentValue);


            }

            observationEntity.getComponents().add(observationComponent);
        }

        // Store the valueCodeable Concept - note component has same structure

        if (observation.hasValueCodeableConcept()) {

            // Code

            ConceptEntity code = null;
            try {

                if (observation.getValueCodeableConcept().getCoding().get(0).hasCode()) {
                    CodeableConcept valueConcept = observation.getValueCodeableConcept();
                     code = conceptDao.findAddCode(valueConcept.getCoding().get(0));
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
            ObservationEntity observationValue = searchAndCreateComponentObservation(observationEntity,code);

            if (code != null) observationValue.setCode(code);

            if (patientEntity != null) observationValue.setPatient(patientEntity);

            // Check code TODO

            observationValue.setObservationType(ObservationEntity.ObservationType.valueQuantity);
            observationValue.setParentObservation(observationEntity);

            if (observation.hasEffectiveDateTimeType()) {
                try {
                    observationValue.setEffectiveDateTime(observation.getEffectiveDateTimeType().getValue());
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }

            em.persist(observationValue);
        }

        if (observation.hasRelated()) {
            for (ObservationRelated observationRelated : observationEntity.getRelatedResources()) {
                em.remove(observationRelated);
            }
            observationEntity.setRelatedResources(new HashSet<>());

            for (Observation.ObservationRelatedComponent observationRelatedComponent : observation.getRelated()) {
                ObservationRelated observationRelated = new ObservationRelated();
                observationRelated.setObservation(observationEntity);
                if (observationRelatedComponent.hasType()) {
                    observationRelated.setType(observationRelatedComponent.getType());
                }
                if (observationRelatedComponent.hasTarget()) {
                    log.info("hasRelated");
                    if (observationRelatedComponent.getTarget().getReference().contains("Observation")) {
                        ObservationEntity observationRelatedEntity= readEntity(ctx, new IdType(observationRelatedComponent.getTarget().getReference()));
                        observationRelated.setRelatedObservation(observationRelatedEntity);
                    }
                    if (observationRelatedComponent.getTarget().getReference().contains("QuestionnaireResponse")) {
                        QuestionnaireResponseEntity questionnaireResponseEntity = formDao.readEntity(ctx, new IdType(observationRelatedComponent.getTarget().getReference()));
                        observationRelated.setRelatedForm(questionnaireResponseEntity);
                    }
                }
                em.persist(observationRelated);
                observationEntity.getRelatedResources().add(observationRelated);
            }
        }
        observation = null;
        if (observationEntity != null) {
            observation = observationEntityToFHIRObservationTransformer.transform(observationEntity);
            observationEntity.setResource(ctx.newJsonParser().encodeResourceToString(observation));
            em.persist(observationEntity);
        }


        return observation;
    }

    @Override
    public Observation read(FhirContext ctx, IdType theId) {

        log.info("Looking for Observation = "+theId.getIdPart());
        if (theId.getIdPart() != null && (daoutils.isNumeric(theId.getIdPart()))) {
            ObservationEntity observationEntity = (ObservationEntity) em.find(ObservationEntity.class, Long.parseLong(theId.getIdPart()));

            Observation observation = null;
            if (observationEntity.getResource() != null) {
                observation = (Observation) ctx.newJsonParser().parseResource(observationEntity.getResource());
            } else {
                observation = observationEntityToFHIRObservationTransformer.transform(observationEntity);
                observationEntity.setResource(ctx.newJsonParser().encodeResourceToString(observation));
                em.persist(observationEntity);
            }
            return observation;

        }
        else { return null; }
    }

    @Override
    public ObservationEntity readEntity(FhirContext ctx, IdType theId) {
        log.debug("Observation Id = "+theId.getIdPart());
        return  (ObservationEntity) em.find(ObservationEntity.class,Long.parseLong(theId.getIdPart()));

    }

    @Override
    public void save(FhirContext ctx, ObservationEntity resource) {
        em.persist(resource);
    }


    @Override
    public List<Resource> search(FhirContext ctx, TokenParam category, TokenOrListParam codes, DateRangeParam effectiveDate, ReferenceParam patient, TokenParam identifier, StringParam resid, ReferenceParam subject, Set<Include> includes) {

        List<Resource> results = new ArrayList<>();
        List<ObservationEntity> qryResults = searchEntity(ctx, category, codes, effectiveDate, patient, identifier,resid,subject, includes);
        log.debug("Found Observations = "+qryResults.size());
        for (ObservationEntity observationEntity : qryResults)
        {
            Observation observation = null;
            if (observationEntity.getResource() != null) {
                observation = (Observation) ctx.newJsonParser().parseResource(observationEntity.getResource());
            } else {
                observation = observationEntityToFHIRObservationTransformer.transform(observationEntity);
                String resourceStr = ctx.newJsonParser().encodeResourceToString(observation);
                log.trace("Length = "+resourceStr.length() +" Data = " +resourceStr);
                observationEntity.setResource(resourceStr);
                em.persist(observationEntity);
            }
            results.add(observation);
        }

        if (includes !=null) {
            for (ObservationEntity observationEntity : qryResults) {
                for (Include include : includes) {
                    switch(include.getValue()) {
                        case "Observation.related":
                        case "*":
                            for (ObservationRelated relatedEntity : observationEntity.getRelatedResources())
                            if (relatedEntity.getRelatedObservation() !=null) {
                                ObservationEntity relatedObservation = relatedEntity.getRelatedObservation();
                                results.add(observationEntityToFHIRObservationTransformer.transform(relatedObservation));
                            }
                            break;

                    }
                }
            }
        }

        return results;
    }



    @Override
    public List<ObservationEntity> searchEntity(FhirContext ctx, TokenParam category, TokenOrListParam codes, DateRangeParam effectiveDate, ReferenceParam patient, TokenParam identifier, StringParam resid, ReferenceParam subject, Set<Include> includes) {


        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ObservationEntity> criteria = builder.createQuery(ObservationEntity.class);
        Root<ObservationEntity> root = criteria.from(ObservationEntity.class);
        List<Predicate> predList = new LinkedList<>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<ObservationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<ObservationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (subject != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(subject.getIdPart())) {
                Join<ObservationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), subject.getIdPart());
                predList.add(p);
            } else {
                Join<ObservationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        if (category!=null) {
            log.trace("Search on Observation.category code = "+category.getValue());
            Join<ObservationEntity, ObservationCategory> join = root.join("categories", JoinType.LEFT);
            Join<ObservationCategory, ConceptEntity> joinConcept = join.join("category", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),category.getValue());
            predList.add(p);
        }
        if (codes!=null) {
            List<Predicate> predOrList = new LinkedList<>();
            Join<ObservationEntity, ConceptEntity> joinConcept = root.join("code", JoinType.LEFT);
            Join<ConceptEntity, CodeSystemEntity> joinCodeSystem = joinConcept.join("codeSystemEntity", JoinType.LEFT);

            for (TokenParam code : codes.getValuesAsQueryTokens()) {
                log.trace("Search on Observation.code code = " + code.getValue());

                Predicate p = null;
                if (code.getSystem() != null) {
                    p = builder.and(builder.equal(joinCodeSystem.get("codeSystemUri"), code.getSystem()),builder.equal(joinConcept.get("code"), code.getValue()));
                } else {
                    p = builder.equal(joinConcept.get("code"), code.getValue());
                }
                predOrList.add(p);

            }
            if (predOrList.size()>0) {
                Predicate p = builder.or(predOrList.toArray(new Predicate[0]));
                predList.add(p);
            }
        }
        if (identifier !=null)
        {
            Join<ObservationEntity, ObservationIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);

        if (effectiveDate !=null)
        {


            if (effectiveDate.getLowerBoundAsInstant() != null) log.debug("getLowerBoundAsInstant()="+effectiveDate.getLowerBoundAsInstant().toString());
            if (effectiveDate.getUpperBoundAsInstant() != null) log.debug("getUpperBoundAsInstant()="+effectiveDate.getUpperBoundAsInstant().toString());


            if (effectiveDate.getLowerBound() != null) {

                DateParam dateParam = effectiveDate.getLowerBound();
                log.debug("Lower Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case GREATERTHAN: /* {
                        Predicate p = builder.greaterThan(root.<Date>get("effectiveDateTime"), parameterLower);
                        predList.add(p);

                        break;
                    } */
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("effectiveDateTime"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("effectiveDateTime"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("effectiveDateTime"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("effectiveDateTime"), parameterLower);
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
                        Predicate pupper = builder.lessThan(root.<Date>get("effectiveDateTime"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("effectiveDateTime"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("effectiveDateTime"), parameterUpper);
                        predList.add(p);

                        break;
                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + effectiveDate.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }

        }

        Predicate p = builder.isNull(root.get("parentObservation"));
        predList.add(p);

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
        criteria.orderBy(builder.desc(root.get("effectiveDateTime")));

        List<ObservationEntity> qryResults = null;
        TypedQuery<ObservationEntity> typedQuery = em.createQuery(criteria);

        if (effectiveDate != null) {
            if (effectiveDate.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, effectiveDate.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            if (effectiveDate.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, effectiveDate.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
        }

        qryResults = typedQuery.setMaxResults(daoutils.MAXROWS).getResultList();
        return qryResults;


    }
}

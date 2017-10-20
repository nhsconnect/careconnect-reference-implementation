package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.ObservationEntityToFHIRObservationTransformer;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationCategory;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationPerformer;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class ObservationDao implements ObservationRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    OrganisationRepository organisationDao;

    private static final Logger log = LoggerFactory.getLogger(Observation.class);

    @Autowired
    private ObservationEntityToFHIRObservationTransformer observationEntityToFHIRObservationTransformer;

    @Override
    public Observation save(Observation observation) {


        ObservationEntity observationEntity = null;

        if (observation.hasId()) observationEntity = readEntity(observation.getIdElement());

        if (observationEntity == null) observationEntity = new ObservationEntity();

        observationEntity.setStatus(observation.getStatus());
        for (Identifier identifier : observation.getIdentifier()) {
            //
        }
        if (observation.hasCode()) {
          ConceptEntity code = conceptDao.findCode(observation.getCode().getCoding().get(0).getSystem(),observation.getCode().getCoding().get(0).getCode());
          if (code != null) observationEntity.setCode(code);
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
            log.info(observation.getSubject().getReference());
            patientEntity = patientDao.readEntity(new IdType(observation.getSubject().getReference()));
            observationEntity.setPatient(patientEntity);
        }
        try {
            if (observation.hasValueQuantity()) {

                observationEntity.setValueQuantity(observation.getValueQuantity().getValue());

                if (observation.getValueQuantity().getCode() != null) {
                    ConceptEntity concept = conceptDao.findCode(observation.getValueQuantity().getSystem(),observation.getValueQuantity().getCode());
                    if (concept != null) observationEntity.setValueUnitOfMeasure(concept);

                }
            }
        } catch (Exception ex) { }



        em.persist(observationEntity);

        for (Reference reference : observation.getPerformer()) {

            log.info("Reference Typez = "+reference.getReferenceElement().getResourceType());
            switch (reference.getReferenceElement().getResourceType()) {
                case "Practitioner" :
                    log.info("Practitioner DAO :"+reference.getReferenceElement().getResourceType());
                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(new IdType(reference.getReference()));
                    if (practitionerEntity != null) {
                        ObservationPerformer performer = new ObservationPerformer();
                        performer.setPerformerType(ObservationPerformer.performer.Practitioner);
                        performer.setPerformerPractitioner(practitionerEntity);
                        performer.setObservation(observationEntity);
                        em.persist(performer);
                        observationEntity.getPerformers().add(performer);
                    }
                    break;
                case "Patient":
                    log.info("Patient DAO :"+reference.getReferenceElement().getResourceType());
                    PatientEntity patientperformerEntity = patientDao.readEntity(new IdType(reference.getReference()));
                    if (patientEntity != null) {
                        ObservationPerformer performer = new ObservationPerformer();
                        performer.setPerformerType(ObservationPerformer.performer.Patient);
                        performer.setPerformerPatient(patientperformerEntity);
                        performer.setObservation(observationEntity);
                        em.persist(performer);
                        observationEntity.getPerformers().add(performer);
                    }
                    break;
                case "Organization":
                    OrganisationEntity organisationEntity = organisationDao.readEntity(new IdType(reference.getReference()));
                    if (patientEntity != null) {
                        ObservationPerformer performer = new ObservationPerformer();
                        performer.setPerformerType(ObservationPerformer.performer.Organisation);
                        performer.setPerformerOrganisation(organisationEntity);
                        performer.setObservation(observationEntity);
                        em.persist(performer);
                        observationEntity.getPerformers().add(performer);
                    }
                    break;
                default:
                    log.info("Not found this :"+reference.getReferenceElement().getResourceType());
            }
        }

        for (CodeableConcept concept :observation.getCategory()) {
            if (concept.getCoding().size() > 0) {
                ConceptEntity conceptEntity = conceptDao.findCode(concept.getCoding().get(0).getSystem(), concept.getCoding().get(0).getCode());
                if (conceptEntity != null) {
                    ObservationCategory category = new ObservationCategory();
                    category.setCategory(conceptEntity);
                    category.setObservation(observationEntity);
                    em.persist(category);
                    observationEntity.getCategories().add(category);
                }
            }
        }

        for (Observation.ObservationComponentComponent component :observation.getComponent()) {
            ObservationEntity observationComponent = new ObservationEntity();
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
                ConceptEntity code = conceptDao.findCode(component.getCode().getCoding().get(0).getSystem(),component.getCode().getCoding().get(0).getCode());
                if (code != null) observationComponent.setCode(code);
            }
            // Value

            try {
                if (component.hasValueQuantity()) {

                    observationComponent.setValueQuantity(component.getValueQuantity().getValue());

                    if (component.getValueQuantity().getCode() != null) {
                        ConceptEntity concept = conceptDao.findCode(component.getValueQuantity().getSystem(),component.getValueQuantity().getCode());
                        if (concept != null) observationComponent.setValueUnitOfMeasure(concept);

                    }
                }
            } catch (Exception ex) { }


            observationComponent.setParentObservation(observationEntity);
            em.persist(observationComponent);

            // Store the ValueCodeableConcept which is a child of the component.

            if (component.hasValueCodeableConcept()) {

                ObservationEntity observationComponentValue = new ObservationEntity();

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

                // Code
                try {

                    if (component.getValueCodeableConcept().getCoding().get(0).hasCode()) {
                        CodeableConcept valueConcept = component.getValueCodeableConcept();
                        ConceptEntity code = conceptDao.findCode(valueConcept.getCoding().get(0).getSystem(),valueConcept.getCoding().get(0).getCode());
                        if (code != null) observationComponentValue.setCode(code);
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
                em.persist(observationComponentValue);


            }

            observationEntity.getComponents().add(observationComponent);
        }

        // Store the valueCodeable Concept - note component has same structure

        if (observation.hasValueCodeableConcept()) {

            ObservationEntity observationValue = new ObservationEntity();

            if (patientEntity != null) observationValue.setPatient(patientEntity);
            observationValue.setObservationType(ObservationEntity.ObservationType.valueQuantity);
            observationValue.setParentObservation(observationEntity);

            if (observation.hasEffectiveDateTimeType()) {
                try {
                    observationValue.setEffectiveDateTime(observation.getEffectiveDateTimeType().getValue());
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
            // Code
            try {

                if (observation.getValueCodeableConcept().getCoding().get(0).hasCode()) {
                    CodeableConcept valueConcept = observation.getValueCodeableConcept();
                    ConceptEntity code = conceptDao.findCode(valueConcept.getCoding().get(0).getSystem(),valueConcept.getCoding().get(0).getCode());
                    if (code != null) observationValue.setCode(code);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
            em.persist(observationValue);
        }
        return observation;
    }

    @Override
    public Observation read(IdType theId) {
        if (theId.getIdPart() != null) {
            ObservationEntity observationEntity = (ObservationEntity) em.find(ObservationEntity.class, Long.parseLong(theId.getIdPart()));

            return observationEntity == null
                    ? null
                    : observationEntityToFHIRObservationTransformer.transform(observationEntity);
        }
        else { return null; }
    }

    @Override
    public ObservationEntity readEntity(IdType theId) {
        log.info("Observation Id = "+theId.getIdPart());
        return  (ObservationEntity) em.find(ObservationEntity.class,Long.parseLong(theId.getIdPart()));

    }

    @Override
    public List<Observation> search(TokenParam category, TokenParam code, DateRangeParam effectiveDate, ReferenceParam patient) {
        List<ObservationEntity> qryResults = null;
        List<Observation> results = new ArrayList<Observation>();

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ObservationEntity> criteria = builder.createQuery(ObservationEntity.class);
        Root<ObservationEntity> root = criteria.from(ObservationEntity.class);
        List<Predicate> predList = new LinkedList<Predicate>();

        if (patient != null) {
            Join<ObservationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

            Predicate p = builder.equal(join.get("id"),patient.getIdPart());
            predList.add(p);
        }
        // Ensure we don't search on components
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

        qryResults = em.createQuery(criteria).getResultList();

        for (ObservationEntity observationEntity : qryResults)
        {

            Observation observation = observationEntityToFHIRObservationTransformer.transform(observationEntity);
            results.add(observation);
        }
        return results;
    }
}

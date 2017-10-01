package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.Transforms.ObservationEntityToFHIRObservationTransformer;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationCategory;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationPerformer;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
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
        if (observation.hasSubject()) {
            log.info(observation.getSubject().getReference());
            PatientEntity patientEntity = patientDao.readEntity(new IdType(observation.getSubject().getReference()));
            observationEntity.setPatient(patientEntity);
        }
        em.persist(observationEntity);

        for (Reference reference : observation.getPerformer()) {
            log.info("Reference Type = "+reference.getReferenceElement().getResourceType());
            if (reference.getReferenceElement().getResourceType().equals("Practitioner") ) {
                PractitionerEntity practitionerEntity = practitionerDao.readEntity(new IdType(reference.getReference()));
                if (practitionerEntity != null) {
                    ObservationPerformer performer = new ObservationPerformer();
                    performer.setPerformerType(ObservationPerformer.performer.Practitioner);
                    performer.setPerformerPractitioner(practitionerEntity);
                    performer.setObservation(observationEntity);
                    em.persist(performer);
                    observationEntity.getPerformers().add(performer);
                }
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
        return null;
    }
}

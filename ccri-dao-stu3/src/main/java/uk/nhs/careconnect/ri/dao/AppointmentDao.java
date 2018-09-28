package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.transforms.AppointmentEntityToFHIRAppointmentTransformer;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.appointment.*;
import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class AppointmentDao implements AppointmentRepository {

    @PersistenceContext
    EntityManager em;


    @Autowired
    AppointmentEntityToFHIRAppointmentTransformer appointmentEntityToFHIRAppointmentTransformer;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    LocationRepository locationDao;

    @Autowired
    SlotRepository slotDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(AppointmentDao.class);


    @Override
    public void save(FhirContext ctx, AppointmentEntity serviceEntity) {
        em.persist(serviceEntity);
    }

    @Override
    public Appointment read(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            AppointmentEntity appointmentEntity = (AppointmentEntity) em.find(AppointmentEntity.class, Long.parseLong(theId.getIdPart()));
            return appointmentEntity == null
                    ? null
                    : appointmentEntityToFHIRAppointmentTransformer.transform(appointmentEntity);

        } else {
            return null;
        }
    }

    @Override
    public AppointmentEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            AppointmentEntity appointmentEntity = (AppointmentEntity) em.find(AppointmentEntity.class, Long.parseLong(theId.getIdPart()));

            return appointmentEntity;

        } else {
            return null;
        }
    }

    @Override
    public void save(FhirContext ctx, HealthcareServiceEntity location) throws OperationOutcomeException {

    }

    @Override
    public List<Appointment> searchAppointment(FhirContext ctx, TokenParam identifier, StringParam type, StringParam codes, StringParam date, StringParam location) {
        return null;
    }

    @Override
    public List<Appointment> searchAppointment(FhirContext ctx, TokenParam identifier, StringParam status, StringParam date) {
        return null;
    }

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(AppointmentEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public Appointment create(FhirContext ctx, Appointment appointment, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("Appointment.save");

        AppointmentEntity appointmentEntity = null;

        if (appointment.hasId()) appointmentEntity = readEntity(ctx, appointment.getIdElement());

        if (theConditional != null) {
            try {
                if (theConditional.contains("https://tools.ietf.org/html/rfc4122")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    log.info("** Scheme = " + scheme);
                    String host = uri.getHost();
                    log.info("** Host = " + host);
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

/*                    List<AppointmentEntity> results = searchAppointmentEntity(ctx, new TokenParam().setValue(spiltStr[1]).setSystem("https://tools.ietf.org/html/rfc4122"), null, null, null, null);
                    for (AppointmentEntity con : results) {
                        appointmentEntity = con;
                        break;
                    }*/
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = " + theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (appointmentEntity == null) {
            appointmentEntity = new AppointmentEntity();
        }

        if (appointment.hasStatus()) {
            appointmentEntity.setStatus(appointment.getStatus());
        }

        log.debug("Appointment.saveCategory");
        if (appointment.hasAppointmentType()) {
            ConceptEntity code = conceptDao.findCode(appointment.getAppointmentType().getCoding().get(0));
            if (code != null) { appointmentEntity.setApointmentType(code); }
            else {
                log.info("Category: Missing System/Code = "+ appointment.getAppointmentType().getCoding().get(0).getSystem() +" code = "+appointment.getAppointmentType().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ appointment.getAppointmentType().getCoding().get(0).getSystem()
                        +" code = "+appointment.getAppointmentType().getCoding().get(0).getCode());
            }
        }

/*        log.debug("Appointment.saveReason");
        if (appointment.hasReason()) {
            ConceptEntity code = conceptDao.findCode(appointment.getReason().getCoding().get(0));
            if (code != null) { appointmentEntity.setReason(code); }
            else {
                log.info("Category: Missing System/Code = "+ appointment.getReason().getCoding().get(0).getSystem() +" code = "+appointment.getReason().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ appointment.getReason().getCoding().get(0).getSystem()
                        +" code = "+appointment.getReason().getCoding().get(0).getCode());
            }
        }*/

        if (appointment.hasPriority()) {
            appointmentEntity.setPriority(appointment.getPriority());
        }

        if (appointment.hasDescription()) {
            appointmentEntity.setDescription(appointment.getDescription());
        }

        if (appointment.hasStart()) {
            appointmentEntity.setStart(appointment.getStart());
        }

        if (appointment.hasEnd()) {
            appointmentEntity.setEnd(appointment.getEnd());
        }

/*        em.persist(appointmentEntity);
        log.debug("HealthcareService.saveIdentifier");
        for (Identifier identifier : appointment.getIdentifier()) {
            AppointmentIdentifier serviceIdentifier = null;

            for (AppointmentIdentifier orgSearch : appointmentEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    serviceIdentifier = orgSearch;
                    break;
                }
            }
            if (serviceIdentifier == null)  serviceIdentifier = new AppointmentIdentifier();

            serviceIdentifier.setValue(identifier.getValue());
            serviceIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            serviceIdentifier.setService(appointmentEntity);
            em.persist(serviceIdentifier);
        }*/
/*        log.debug("Appointment.saveSlot");
        for (Reference reference : appointment.getSlot()) {
            SlotEntity slotEntity = slotDao.readEntity(ctx, new IdType(reference.getReference()));
            if (slotEntity != null) {
                AppointmentSlot appointmentSlot = new AppointmentSlot();
                appointmentSlot.setLocation(slotEntity);
                appointmentSlot.setAppointment(appointmentEntity);
                em.persist(appointmentSlot);
            }
        }*/
/*        for (CodeableConcept concept :appointment.getSpecialty()) {

            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    AppointmentSpecialty specialtyEntity = null;
                    // Look for existing categories
                    for (AppointmentSpecialty cat :appointmentEntity.getSpecialties()) {
                        if (cat.getSpecialty().getCode().equals(concept.getCodingFirstRep().getCode())) specialtyEntity = cat;
                    }
                    if (specialtyEntity == null) specialtyEntity = new AppointmentSpecialty();

                    specialtyEntity.setSpecialty(conceptEntity);
                    specialtyEntity.setAppointment(appointmentEntity);
                    em.persist(specialtyEntity);
                    appointmentEntity.getSpecialties().add(specialtyEntity);
                }
                else {
                    log.info("Missing ServiceRequested. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }*/
/*        log.debug("Appointment.saveType");
        for (CodeableConcept concept :appointment.getType()) {

            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    AppointmentType type = null;
                    // Look for existing categories
                    for (AppointmentType cat :appointmentEntity.getTypes()) {
                        if (cat.getType_().getCode().equals(concept.getCodingFirstRep().getCode())) type = cat;
                    }
                    if (type == null) type = new AppointmentType();

                    type.setType_(conceptEntity);
                    type.setAppointment(appointmentEntity);
                    em.persist(type);
                    appointmentEntity.getTypes().add(type);
                }
                else {
                    log.info("Missing AppointmentRequested. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }*/
 /*       log.debug("Appointment.saveTelecom");
        for (ContactPoint telecom : appointment.getTelecom()) {
            AppointmentTelecom serviceTelecom = null;

            for (AppointmentTelecom orgSearch : appointmentEntity.getTelecoms()) {
                if (telecom.getValue().equals(orgSearch.getValue())) {
                    serviceTelecom = orgSearch;
                    break;
                }
            }
            if (serviceTelecom == null) {
                serviceTelecom = new AppointmentTelecom();
                serviceTelecom.setAppointment(appointmentEntity);
            }

            serviceTelecom.setValue(telecom.getValue());
            serviceTelecom.setSystem(telecom.getSystem());
            if (telecom.hasUse()) { serviceTelecom.setTelecomUse(telecom.getUse()); }

            em.persist(serviceTelecom);
        }
        log.info("HealthcareService.Transform");

        */
        return appointmentEntityToFHIRAppointmentTransformer.transform(appointmentEntity);

    }

/*    @Override
    public List<HealthcareService> searchAppointment(FhirContext ctx, TokenParam identifier, StringParam name, TokenOrListParam codes, TokenParam id, ReferenceParam organisation) {
        List<AppointmentEntity> qryResults = searchHealthcareServiceEntity(ctx,identifier,name, codes,id,organisation);
        List<HealthcareService> results = new ArrayList<>();

        for (AppointmentEntity healthcareServiceEntity : qryResults) {
            HealthcareService healthcareService = appointmentEntityToFHIRAppointmentTransformer.transform(appointmentEntity);
            results.add(healthcareService);
        }

        return results;
    }*/

/*
    @Override
    public List<AppointmentEntity> searchHealthcareServiceEntity(FhirContext ctx, TokenParam identifier, StringParam name, TokenOrListParam codes, TokenParam id, ReferenceParam organisation) {
        List<AppointmentEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<AppointmentEntity> criteria = builder.createQuery(AppointmentEntity.class);
        Root<AppointmentEntity> root = criteria.from(AppointmentEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Organization> results = new ArrayList<Organization>();

        if (identifier !=null)
        {
            Join<AppointmentEntity, AppointmentIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (id != null) {
            Predicate p = builder.equal(root.get("id"),id.getValue());
            predList.add(p);
        }
        if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal(name.getValue()+"%"))
                    );

            predList.add(p);
        }
        
*/


/*        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            criteria.select(root).where(predArray);
        }
        else
        {
            criteria.select(root);
        }

        qryResults = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS).getResultList();

        return qryResults;
    }*/

/*        @Override
        public Long count () {
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = qb.createQuery(Long.class);
            cq.select(qb.count(cq.from(AppointmentEntity.class)));
            return em.createQuery(cq).getSingleResult();
        }*/

}

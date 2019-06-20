package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.stu3.dao.transforms.AppointmentEntityToFHIRAppointmentTransformer;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentEntity;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentIdentifier;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentReason;
import uk.nhs.careconnect.ri.database.entity.appointment.AppointmentSlot;
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
    SlotRepository slotDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private LibDao libDao;

    private static final Logger log = LoggerFactory.getLogger(AppointmentDao.class);

    @Override
    public void save(FhirContext ctx, AppointmentEntity appointmentEntity) {
        em.persist(appointmentEntity);
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

                    List<AppointmentEntity> results = searchAppointmentEntity(ctx, new TokenParam().setValue(spiltStr[1]).setSystem("https://tools.ietf.org/html/rfc4122"), null);
                    for (AppointmentEntity con : results) {
                        appointmentEntity = con;
                        break;
                    }
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

        if(appointment.hasSlot()){

            SlotEntity slotEntity = (SlotEntity) slotDao.readEntity(ctx, new IdType(appointment.getSlot().get(0).getReference()));

            if(slotEntity != null){
                appointmentEntity.setSlot(slotEntity);
            }

        }

        if(appointment.hasAppointmentType()){

            ConceptEntity code = conceptDao.findAddCode(appointment.getAppointmentType().getCoding().get(0));

            if(code != null){
                appointmentEntity.setApointmentType(code);
            }

        }

        if(appointment.hasReason()){

            ConceptEntity code = conceptDao.findCode(appointment.getReason().get(0).getCoding().get(0));

            if(code != null){
                appointmentEntity.setApointmentType(code);
            }

        }

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

        em.persist(appointmentEntity);

        log.debug("Appointment.saveIdentifier");
        for (Identifier identifier : appointment.getIdentifier()) {
            AppointmentIdentifier appointmentIdentifier = null;

            for (AppointmentIdentifier orgSearch : appointmentEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    appointmentIdentifier = orgSearch;
                    break;
                }
            }
            if (appointmentIdentifier == null)  appointmentIdentifier = new AppointmentIdentifier();

            appointmentIdentifier= (AppointmentIdentifier) libDao.setIdentifier(identifier, appointmentIdentifier );
            appointmentIdentifier.setAppointment(appointmentEntity);
            em.persist(appointmentIdentifier);
        }


        for (CodeableConcept concept :appointment.getReason()) {
            // Category must have a code 15/Jan/2018 testing with Synthea examples
            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    AppointmentReason reason = null;
                    // Look for existing categories
                    for (AppointmentReason cat :appointmentEntity.getReasons()) {
                        if (cat.getReason().getCode().equals(concept.getCodingFirstRep().getCode())) reason= cat;
                    }
                    if (reason == null) reason = new AppointmentReason();

                    reason.setReason(conceptEntity);
                    reason.setAppointment(appointmentEntity);
                    em.persist(reason);
                    appointmentEntity.getReasons().add(reason);
                }
                else {
                    log.info("Missing Reason. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }

        log.debug("Appointment.saveReason");
        if (appointment.hasReason()) {
            Coding code = new Coding().setCode(appointment.getReason().get(0).getCodingFirstRep().getCode()).setSystem(appointment.getReason().get(0).getCodingFirstRep().getSystem());
            ConceptEntity codeEntity = conceptDao.findAddCode(code);
            if (codeEntity != null) appointmentEntity.setReason(codeEntity);
        }

        //Appointment appointment = new Appointment();
        Appointment.AppointmentParticipantComponent appointmentParticipantComponent = appointment.addParticipant();
        appointmentParticipantComponent.setActor(new Reference("Patient/1"));


        log.debug("Appointment.saveSlot");
        for (Reference reference : appointment.getSlot()) {
            SlotEntity slotEntity = slotDao.readEntity(ctx, new IdType(reference.getReference()));
            if (appointment.hasSlot()) {
                AppointmentSlot appointmentSlot = new AppointmentSlot();
                appointmentSlot.setSlot(appointmentSlot.getSlot());
                em.persist(appointmentSlot);
            }
        }

        if (appointment.hasCreated()) {
            appointmentEntity.setCreated(appointment.getCreated());
        }

        if (appointment.hasComment()) {
            appointmentEntity.setComment(appointment.getComment());
        }

/*        if (appointment.hasParticipant()) {
            appointmentEntity.setParticipant(appointment.getParticipant());
        }*/

/*        if(appointment.hasParticipant()){
            for(AppointmentP)
        }*/

        em.persist(appointmentEntity);
        return appointmentEntityToFHIRAppointmentTransformer.transform(appointmentEntity);

    }


    @Override
    public List<Appointment> searchAppointment(FhirContext ctx, TokenParam identifier, StringParam id) { // , ReferenceParam organisation
        List<AppointmentEntity> qryResults = searchAppointmentEntity(ctx,identifier, id); //,organisation
        List<Appointment> results = new ArrayList<>();

        for (AppointmentEntity appointmentEntity : qryResults) {
            Appointment appointment = appointmentEntityToFHIRAppointmentTransformer.transform(appointmentEntity);
            results.add(appointment);
        }

        return results;
    }

    @Override
    public List<AppointmentEntity> searchAppointmentEntity(FhirContext ctx, TokenParam identifier, StringParam id) { // , ReferenceParam organisation
        List<AppointmentEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<AppointmentEntity> criteria = builder.createQuery(AppointmentEntity.class);
        Root<AppointmentEntity> root = criteria.from(AppointmentEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        //List<Organization> results = new ArrayList<Organization>();

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

        qryResults = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS).getResultList();

        return qryResults;
    }


    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(AppointmentEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }

}

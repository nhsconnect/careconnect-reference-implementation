package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
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
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.ScheduleEntityToFHIRScheduleTransformer;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerRole;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleEntity;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleIdentifier;
import uk.nhs.careconnect.ri.database.entity.schedule.*;
import uk.nhs.careconnect.ri.database.entity.healthcareService.*;

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
public class ScheduleDao implements ScheduleRepository {

    @PersistenceContext
    EntityManager em;


    @Autowired
    ScheduleEntityToFHIRScheduleTransformer scheduleEntityToFHIRScheduleTransformer;

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
    PractitionerRepository practitionerDao;

    @Autowired
    PractitionerRoleRepository practitionerRoleDao;


    @Autowired
    HealthcareServiceRepository healthcareServiceDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(ScheduleDao.class);


    @Override
    public void save(FhirContext ctx, ScheduleEntity scheduleEntity) {
        em.persist(scheduleEntity);
    }

    @Override
    public Schedule read(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            ScheduleEntity scheduleEntity = (ScheduleEntity) em.find(ScheduleEntity.class, Long.parseLong(theId.getIdPart()));
            return scheduleEntity == null
                    ? null
                    : scheduleEntityToFHIRScheduleTransformer.transform(scheduleEntity);

        } else {
            return null;
        }
    }

    @Override
    public ScheduleEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ScheduleEntity scheduleEntity = (ScheduleEntity) em.find(ScheduleEntity.class, Long.parseLong(theId.getIdPart()));

            return scheduleEntity;

        } else {
            return null;
        }
    }

    @Override
    public Schedule create(FhirContext ctx, Schedule schedule, IdType theId, String theConditional) throws OperationOutcomeException  {
        log.debug("Schedule.save");

        ScheduleEntity scheduleEntity = null;

        if (schedule.hasId()) scheduleEntity = readEntity(ctx, schedule.getIdElement());

        if (theConditional != null) {
            try {
                if (theConditional.contains("https://tools.ietf.org/html/rfc4122")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    log.info("** Scheme = "+scheme);
                    String host = uri.getHost();
                    log.info("** Host = "+host);
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<ScheduleEntity> results = searchScheduleEntity(ctx,  new TokenParam().setValue(spiltStr[1]).setSystem("https://tools.ietf.org/html/rfc4122"),null, null, null); //,null
                    for (ScheduleEntity con : results) {
                        scheduleEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (scheduleEntity == null) {
            scheduleEntity = new ScheduleEntity();
        }

        if (schedule.hasActive()) {
            scheduleEntity.setActive(schedule.getActive());
        }


        em.persist(scheduleEntity);
        log.debug("Schedule.saveIdentifier");
        for (Identifier identifier : schedule.getIdentifier()) {
            ScheduleIdentifier scheduleIdentifier = null;

            for (ScheduleIdentifier orgSearch : scheduleEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    scheduleIdentifier = orgSearch;
                    break;
                }
            }
            if (scheduleIdentifier == null)  scheduleIdentifier = new ScheduleIdentifier();

            scheduleIdentifier.setValue(identifier.getValue());
            scheduleIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            scheduleIdentifier.setSchedule(scheduleEntity);
            em.persist(scheduleIdentifier);
        }

        for (Reference actorRef :schedule.getActor()) {
            ScheduleActor actor = new ScheduleActor();
            actor.setScheduleEntity(scheduleEntity);
            if (actorRef.getReference().contains("Practitioner")) {


                PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(actorRef.getReference()));
                if (practitionerEntity != null) {
                    actor.setPractitionerEntity(practitionerEntity);
                }
            }
            if (actorRef.getReference().contains("PractitonerRole")) {

                PractitionerRole practitionerRole = practitionerRoleDao.readEntity(ctx, new IdType(actorRef.getReference()));
                if (practitionerRole != null) {
                    actor.setPractitionerRole(practitionerRole);
                }
            }
            if (actorRef.getReference().contains("HealthcareService")) {


                HealthcareServiceEntity healthcareService = healthcareServiceDao.readEntity(ctx, new IdType(actorRef.getReference()));
                if (healthcareService != null) {
                    actor.setHealthcareServiceEntity(healthcareService);
                }
            }
            if (actorRef.getReference().contains("Location")) {


                LocationEntity location = locationDao.readEntity(ctx, new IdType(actorRef.getReference()));
                if (location != null) {
                    actor.setLocationEntity(location);
                }
            }



            em.persist(actor);

        }
        em.persist(scheduleEntity);
        log.info("Schedule.Transform");
        return scheduleEntityToFHIRScheduleTransformer.transform(scheduleEntity);

    }

    @Override
    public List<Schedule> searchSchedule(FhirContext ctx, TokenParam identifier, StringParam actor, TokenOrListParam codes, StringParam id) { // , ReferenceParam organisation
        List<ScheduleEntity> qryResults = searchScheduleEntity(ctx,identifier,actor, codes,id); //,organisation
        List<Schedule> results = new ArrayList<>();

        for (ScheduleEntity scheduleEntity : qryResults) {
            Schedule schedule = scheduleEntityToFHIRScheduleTransformer.transform(scheduleEntity);
            results.add(schedule);
        }

        return results;
    }

    @Override
    public List<ScheduleEntity> searchScheduleEntity(FhirContext ctx, TokenParam identifier, StringParam actor, TokenOrListParam codes, StringParam id) { // , ReferenceParam organisation
        List<ScheduleEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ScheduleEntity> criteria = builder.createQuery(ScheduleEntity.class);
        Root<ScheduleEntity> root = criteria.from(ScheduleEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Organization> results = new ArrayList<Organization>();

        if (identifier !=null)
        {
            Join<ScheduleEntity, ScheduleIdentifier> join = root.join("identifiers", JoinType.LEFT);

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
        cq.select(qb.count(cq.from(ScheduleEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }
}

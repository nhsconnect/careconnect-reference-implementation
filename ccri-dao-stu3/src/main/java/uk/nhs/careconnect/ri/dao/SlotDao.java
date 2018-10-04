package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Schedule;
import org.hl7.fhir.dstu3.model.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.transforms.SlotEntityToFHIRSlotTransformer;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleActor;
import uk.nhs.careconnect.ri.database.entity.schedule.ScheduleEntity;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;
import uk.nhs.careconnect.ri.database.entity.slot.SlotIdentifier;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class SlotDao implements SlotRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    SlotEntityToFHIRSlotTransformer slotEntityToFHIRSlotTransformer;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    ScheduleRepository scheduleDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(SlotDao.class);


    @Override
    public void save(FhirContext ctx, SlotEntity slotEntity) {
        em.persist(slotEntity);
    }

    @Override
    public Slot read(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            SlotEntity slotEntity =  em.find(SlotEntity.class, Long.parseLong(theId.getIdPart()));
            return slotEntity == null
                    ? null
                    : slotEntityToFHIRSlotTransformer.transform(slotEntity);

        } else {
            return null;
        }
    }

    @Override
    public SlotEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            SlotEntity slotEntity = (SlotEntity) em.find(SlotEntity.class, Long.parseLong(theId.getIdPart()));

            return slotEntity;

        } else {
            return null;
        }
    }

    @Override
    public Slot create(FhirContext ctx, Slot slot, IdType theId, String theConditional) throws OperationOutcomeException  {
        log.debug("Slot.save");

        SlotEntity slotEntity = null;

        if (slot.hasId()) slotEntity = readEntity(ctx, slot.getIdElement());

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

                    List<Slot> results = searchSlot(ctx,  new TokenParam().setValue(spiltStr[1]).setSystem("https://tools.ietf.org/html/rfc4122"),null, null,null,null,null); //,null
                    for (Slot con : results) {
                        slot = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (slotEntity == null) {
            slotEntity = new SlotEntity();
        }

        //em.persist(slotEntity);

         if (slot.hasServiceCategory()) {

             ConceptEntity code = conceptDao.findCode(slot.getServiceCategory().getCoding().get(0));

             if (code != null) {
                slotEntity.setServiceCategory(code);
             }
         }

        if(slot.hasAppointmentType()){

            ConceptEntity code = conceptDao.findCode(slot.getAppointmentType().getCoding().get(0));

            if(code != null){
                slotEntity.setAppointmentType(code);
            }

        }

        if(slot.hasSchedule()){

            ScheduleEntity scheduleEntity = (ScheduleEntity) scheduleDao.readEntity(ctx, new IdType(slot.getSchedule().getReference()));

            if(scheduleEntity != null){
                slotEntity.setSchedule(scheduleEntity);
            }

        }

        if(slot.hasStatus()) {
            log.debug("Slot.Status" + slot.getStatus());
            slotEntity.setStatus(slot.getStatus());
        } else{
            slotEntity.setStatus(null);
        }

        if(slot.hasStart()) {

            slotEntity.setStart(slot.getStart());
        } else{
            slotEntity.setStart(null);
        }

        if(slot.hasEnd()) {
            slotEntity.setEnd(slot.getEnd());
        } else{
            slotEntity.setEnd(null);
        }

        em.persist(slotEntity);

        log.debug("Slot.saveIdentifier");
        for (Identifier identifier : slot.getIdentifier()) {
            SlotIdentifier slotIdentifier = null;

            for (SlotIdentifier orgSearch : slotEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    slotIdentifier = orgSearch;
                    break;
                }
            }
            if (slotIdentifier == null)  slotIdentifier = new SlotIdentifier();

            slotIdentifier.setValue(identifier.getValue());
            slotIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            slotIdentifier.setSlot(slotEntity);
            em.persist(slotIdentifier);
            slotEntity.getIdentifiers().add(slotIdentifier); // KGM add the identifier to the slot so transform includes the slot 2/10/2018
        }

        log.info("Slot.Transform");
        return slotEntityToFHIRSlotTransformer.transform(slotEntity);

    }

    @Override
    public List<Slot> searchSlot(FhirContext ctx, TokenParam identifier, DateParam start, StringParam status, StringParam res_id, ReferenceParam schedule, ReferenceParam service) {
        List<SlotEntity> qryResults = searchSlotEntity(ctx,identifier,start,status,res_id,schedule, service);
        List<Slot> results = new ArrayList<>();

        for (SlotEntity slotEntity : qryResults) {
            Slot slot = slotEntityToFHIRSlotTransformer.transform(slotEntity);
            results.add(slot);
        }

        return results;
    }

    @Override
    public List<SlotEntity> searchSlotEntity(FhirContext ctx, TokenParam identifier, DateParam start, StringParam status, StringParam resid, ReferenceParam schedule,ReferenceParam service) {
        List<SlotEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<SlotEntity> criteria = builder.createQuery(SlotEntity.class);
        Root<SlotEntity> root = criteria.from(SlotEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Schedule> results = new ArrayList<Schedule>();

        if (identifier !=null)
        {
            Join<SlotEntity, SlotIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        //ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);

        if (start !=null)
        {

            Predicate p = builder.greaterThanOrEqualTo(root.get("Start"), parameterLower);
            predList.add(p);

        }

        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }

/*        if (status != null) {
            Predicate p = builder.equal(root.get("Status"),status.getValue());
            predList.add(p);
        }*/

        if (status != null) {

            System.out.println("root.get(\"Status\")" + root.get("Status"));
            System.out.println("Integer.valueOf(status.getValue()" + status.getValue());
            //System.out.println("Integer.valueOf(status.getValue()" + Integer.valueOf(status..getValue()));
            System.out.println("Status = " + status);

            String strStatus = null;


            switch (Integer.valueOf(status.getValue())) {


                case 1 :
                    strStatus = "busy";
                    break;
                case 0 :
                    strStatus = "free";
                    break;

                default:
                    strStatus = "free";

            }

            System.out.println("Strstatus = " + strStatus);

            System.out.println("root.get(\"Status\")" + root.get("Status"));

            Predicate p = builder.equal(root.get("Status"), strStatus);
            predList.add(p);

        }

        if (schedule != null) {

            if (daoutils.isNumeric(schedule.getIdPart())) {
                Join<SlotEntity, ScheduleEntity> join = root.join("schedule" , JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), schedule.getIdPart());
                predList.add(p);
            } else {
                Join<SlotEntity, ScheduleEntity> join = root.join("schedule", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (service != null) {
            if (daoutils.isNumeric(schedule.getIdPart())) {
                Join<SlotEntity, ScheduleEntity> join = root.join("schedule" , JoinType.LEFT);
                Join<ScheduleEntity, ScheduleActor> join1 = join.join("schedule" , JoinType.LEFT);
                Join<ScheduleActor, HealthcareServiceEntity> join2 = join1.join("healthcareServiceEntity" , JoinType.LEFT);

                Predicate p = builder.equal(join2.get("id"), service.getIdPart());
                predList.add(p);
            } else {
                Join<SlotEntity, ScheduleEntity> join = root.join("schedule", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
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

        TypedQuery<SlotEntity> typedQuery = em.createQuery(criteria);

        if (start != null) {

                typedQuery.setParameter(parameterLower, start.getValue(), TemporalType.TIMESTAMP);
        }

        qryResults = typedQuery.setMaxResults(daoutils.MAXROWS).getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(SlotEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }
}

package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.daoutils;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.slot.SlotIdentifier;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;

import uk.nhs.careconnect.ri.dao.transforms.SlotEntityToFHIRSlotTransformer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static uk.nhs.careconnect.ri.dao.daoutils.MAXROWS;
import uk.nhs.careconnect.ri.dao.transforms.*;
import uk.nhs.careconnect.ri.database.entity.slot.SlotEntity;
import uk.nhs.careconnect.ri.dao.transforms.*;

@Repository
@Transactional
public class SlotDao implements SlotRepository {

    @PersistenceContext
    EntityManager em;


    @Autowired
    SlotEntityToFHIRSlotTransformer slotEntityToFHIRSlotTransformer;

    @Autowired
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    LocationRepository locationDao;

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

                    List<Slot> results = searchSlot(ctx,  new TokenParam().setValue(spiltStr[1]).setSystem("https://tools.ietf.org/html/rfc4122"),null, null); //,null
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

        /* if (schedule.hasProvidedBy()) {

            OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(schedule.getProvidedBy().getReference()));
            if (organisationEntity != null) {
                scheduleEntity.setProvidedBy(organisationEntity);
            }
        } */
/*        if (slot.hasActive()) {
            slotEntity.setActive(slotEntity.getActive());
        }*/
        /*if (schedule.hasName()) {
            scheduleEntity.setName(schedule.getName());
        }*/
        /*log.debug("Slot.saveCategory");
        if (schedule.hasCategory()) {
            ConceptEntity code = conceptDao.findCode(schedule.getCategory().getCoding().get(0));
            if (code != null) { scheduleEntity.setCategory(code); }
            else {
                log.info("Category: Missing System/Code = "+ schedule.getCategory().getCoding().get(0).getSystem() +" code = "+schedule.getCategory().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ schedule.getCategory().getCoding().get(0).getSystem()
                        +" code = "+schedule.getCategory().getCoding().get(0).getCode());
            }
        }*/

        em.persist(slotEntity);
        /*log.debug("Slot.saveIdentifier");
        for (Identifier identifier : schedule.getIdentifier()) {
            SlotIdentifier scheduleIdentifier = null;

            for (SlotIdentifier orgSearch : slotEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    scheduleIdentifier = orgSearch;
                    break;
                }
            }
            if (scheduleIdentifier == null)  scheduleIdentifier = new SlotIdentifier();

            scheduleIdentifier.setValue(identifier.getValue());
            scheduleIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            scheduleIdentifier.setService(scheduleEntity);
            em.persist(scheduleIdentifier);
        }*/
        /*log.debug("Slot.saveLocation");
        for (Reference reference : schedule.getLocation()) {
            LocationEntity locationEntity = locationDao.readEntity(ctx, new IdType(reference.getReference()));
            if (locationEntity != null) {
                SlotLocation location = new SlotLocation();
                location.setLocation(locationEntity);
                location.setSlot(slotEntity);
                em.persist(location);
            }
        }*/
/*        for (CodeableConcept concept :schedule.getSpecialty()) {

            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    SlotSpecialty specialtyEntity = null;
                    // Look for existing categories
                    for (SlotSpecialty cat :scheduleEntity.getSpecialties()) {
                        if (cat.getSpecialty().getCode().equals(concept.getCodingFirstRep().getCode())) specialtyEntity = cat;
                    }
                    if (specialtyEntity == null) specialtyEntity = new SlotSpecialty();

                    specialtyEntity.setSpecialty(conceptEntity);
                    specialtyEntity.setSlot(scheduleEntity);
                    em.persist(specialtyEntity);
                    scheduleEntity.getSpecialties().add(specialtyEntity);
                }
                else {
                    log.info("Missing ServiceRequested. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }*/
        /*log.debug("Slot.saveType");
        for (CodeableConcept concept :schedule.getType()) {

            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    SlotType type = null;
                    // Look for existing categories
                    for (SlotType cat :scheduleEntity.getTypes()) {
                        if (cat.getType_().getCode().equals(concept.getCodingFirstRep().getCode())) type = cat;
                    }
                    if (type == null) type = new SlotType();

                    type.setType_(conceptEntity);
                    type.setSlot(scheduleEntity);
                    em.persist(type);
                    scheduleEntity.getTypes().add(type);
                }
                else {
                    log.info("Missing ServiceRequested. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }*/
        /*log.debug("Slot.saveTelecom");
        for (ContactPoint telecom : schedule.getTelecom()) {
            SlotTelecom scheduleTelecom = null;

            for (SlotTelecom orgSearch : scheduleEntity.getTelecoms()) {
                if (telecom.getValue().equals(orgSearch.getValue())) {
                    scheduleTelecom = orgSearch;
                    break;
                }
            }
            if (scheduleTelecom == null) {
                scheduleTelecom = new SlotTelecom();
                scheduleTelecom.setSlot(scheduleEntity);
            }

            scheduleTelecom.setValue(telecom.getValue());
            scheduleTelecom.setSystem(telecom.getSystem());
            if (telecom.hasUse()) { scheduleTelecom.setTelecomUse(telecom.getUse()); }

            em.persist(scheduleTelecom);
        }*/
        log.info("Slot.Transform");
        return slotEntityToFHIRSlotTransformer.transform(slotEntity);

    }

    @Override
    public List<Slot> searchSlot(FhirContext ctx, TokenParam identifier, StringParam schedule, TokenParam id) {

        List<SlotEntity> qryResults = searchSlotEntity(ctx,identifier,schedule, id); //,organisation
        List<Slot> results = new ArrayList<>();

        for (SlotEntity slotEntity : qryResults) {
            Slot slot = slotEntityToFHIRSlotTransformer.transform(slotEntity);
            results.add(slot);
        }

        return results;
    }

    @Override
    public List<SlotEntity> searchSlotEntity(FhirContext ctx, TokenParam identifier, StringParam schedule, TokenParam id) {

        List<SlotEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<SlotEntity> criteria = builder.createQuery(SlotEntity.class);
        Root<SlotEntity> root = criteria.from(SlotEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Organization> results = new ArrayList<Organization>();

        if (identifier !=null)
        {
            Join<SlotEntity, SlotIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (id != null) {
            Predicate p = builder.equal(root.get("id"),id.getValue());
            predList.add(p);
        }
        /* if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal(name.getValue()+"%"))
                    );

            predList.add(p);
        } */
        


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

        qryResults = em.createQuery(criteria).setMaxResults(MAXROWS).getResultList();

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

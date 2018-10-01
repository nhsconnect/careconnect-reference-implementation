package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.transforms.ListEntityToFHIRListResourceTransformer;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;
import uk.nhs.careconnect.ri.database.entity.list.ListEntity;
import uk.nhs.careconnect.ri.database.entity.list.ListIdentifier;
import uk.nhs.careconnect.ri.database.entity.list.ListItem;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class ListDao implements ListRepository {


    @PersistenceContext
    EntityManager em;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
            @Lazy
    CarePlanRepository carePlanDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    EpisodeOfCareRepository episodeDao;

    @Autowired
    QuestionnaireRepository questionnaireDao;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    ConditionRepository conditionDao;

    @Autowired
    ObservationRepository observationDao;

    @Autowired
    QuestionnaireResponseRepository formDao;

    @Autowired
    DocumentReferenceRepository documentDao;


    @Autowired
    private ListEntityToFHIRListResourceTransformer listEntityToFHIRListResourceTranslister;



    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(ListDao.class);


    @Override
    public void save(FhirContext ctx, ListEntity list) throws OperationOutcomeException {
        em.persist(list);
    }

    @Override
    public ListResource read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ListEntity listEntity = (ListEntity) em.find(ListEntity.class, Long.parseLong(theId.getIdPart()));

            return listEntity == null
                    ? null
                    : listEntityToFHIRListResourceTranslister.transform(listEntity);

        } else {
            return null;
        }
    }

    @Override
    public List<ListResource> searchListResource(FhirContext ctx, TokenParam identifier, StringParam id, ReferenceParam patient) {
        List<ListEntity> qryResults = searchListEntity(ctx, identifier, id,  patient);
        List<ListResource> results = new ArrayList<>();

        for (ListEntity list : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            ListResource questionnaireResponse = listEntityToFHIRListResourceTranslister.transform(list);
            results.add(questionnaireResponse);
        }

        return results;
    }

    @Override
    public List<ListEntity> searchListEntity(FhirContext ctx, TokenParam identifier, StringParam resid, ReferenceParam patient) {

            List<ListEntity> qryResults = null;

            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<ListEntity> criteria = builder.createQuery(ListEntity.class);

            Root<ListEntity> root = criteria.from(ListEntity.class);

            List<Predicate> predList = new LinkedList<Predicate>();
            List<Questionnaire> results = new ArrayList<Questionnaire>();

            if (identifier !=null)
            {
                Join<ListEntity, ListIdentifier> join = root.join("identifiers", JoinType.LEFT);

                Predicate p = builder.equal(join.get("value"),identifier.getValue());
                predList.add(p);
                // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

            }
            if (resid != null) {
                Predicate p = builder.equal(root.get("id"),resid.getValue());
                predList.add(p);
            }

            if (patient != null) {
                if (daoutils.isNumeric(patient.getIdPart())) {
                    Join<ListEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                    predList.add(p);
                } else {
                    Join<ListEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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

            qryResults = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS).getResultList();
            return qryResults;
        }


    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ListEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public ListEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ListEntity listEntity = (ListEntity) em.find(ListEntity.class, Long.parseLong(theId.getIdPart()));

            return listEntity;

        } else {
            return null;
        }
    }

    @Override
    public ListResource create(FhirContext ctx,ListResource list, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException {

        ListEntity listEntity = null;
        log.debug("Called ListResource Create Condition Url: "+theConditional);
        if (theId != null) {
                listEntity =  readEntity(ctx,theId);
        }

        if (listEntity == null) {
            listEntity = new ListEntity();
        }

        listEntity.setStatus(list.getStatus());

        if (list.hasMode()) {
            listEntity.setMode(list.getMode());
        }

        if (list.hasTitle()) {
            listEntity.setTitle(list.getTitle());
        }
        if (list.hasCode()) {
            ConceptEntity code = conceptDao.findAddCode(list.getCode().getCoding().get(0));
            if (code != null) { listEntity.setCode(code); }
        }


        if (list.hasSubject()) {
            if (list.getSubject().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(list.getSubject().getReference()));
                listEntity.setPatient(patientEntity);
            }
        }

        if (list.hasEncounter()) {

            EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(list.getEncounter().getReference()));
            listEntity.setContextEncounter(encounterEntity);

        }

        if (list.hasDate()) {
            listEntity.setDateTime(list.getDate());
        }



        if (list.hasSource()) {
            if (list.getSource().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(list.getSource().getReference()));
                listEntity.setSourcePatient(patientEntity);
            }
            if (list.getSource().getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = null;
                practitionerEntity = practitionerDao.readEntity(ctx, new IdType(list.getSource().getReference()));
                listEntity.setSourcePractitioner(practitionerEntity);
            }
        }

        if (list.hasNote()) {
            listEntity.setNote(list.getNote().get(0).getText());
        }


        em.persist(listEntity);

        for (Identifier ident : list.getIdentifier()) {

            log.debug("ListResource SDS = " + ident.getValue() + " System =" + ident.getSystem());
            ListIdentifier listIdentifier = null;

            for (ListIdentifier orgSearch : listEntity.getIdentifiers()) {
                if (ident.getSystem().equals(orgSearch.getSystemUri()) && ident.getValue().equals(orgSearch.getValue())) {
                    listIdentifier = orgSearch;
                    break;
                }
            }
            if (listIdentifier == null) {
                listIdentifier = new ListIdentifier();
                listIdentifier.setList(listEntity);

            }

            listIdentifier.setValue(ident.getValue());
            listIdentifier.setSystem(codeSystemSvc.findSystem(ident.getSystem()));
            log.debug("ListResource System Code: " + listIdentifier.getSystemUri());

            em.persist(listIdentifier);
        }


        for (ListItem item : listEntity.getItems()) {
            em.remove(item);
        }
        for (ListResource.ListEntryComponent item : list.getEntry()) {
            ListItem itemEntity = new ListItem();
            itemEntity.setList(listEntity);
            if (item.hasDate()) {
                itemEntity.setItemDateTime(item.getDate());
            }
            if (item.hasDeleted()) {
                itemEntity.setItemDeleted(item.getDeleted());
            }
            buildItem(ctx, item, itemEntity);
        }


       // log.info("Called PERSIST id="+listEntity.getId().toString());
        list.setId(listEntity.getId().toString());

        return list;
    }

    private void buildItem(FhirContext ctx,ListResource.ListEntryComponent item, ListItem itemEntity ) {

        if (item.getItem().getReference().contains("Condition")) {

            ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new IdType(item.getItem().getReference()));
            itemEntity.setReferenceCondition(conditionEntity);

        } else if (item.getItem().getReference().contains("Observation")) {

            ObservationEntity observationEntity = observationDao.readEntity(ctx, new IdType(item.getItem().getReference()));
            itemEntity.setReferenceObservation(observationEntity);
        } else if (item.getItem().getReference().contains("QuestionnaireResponse")) {

            QuestionnaireResponseEntity
                    questionnaireEntity = formDao.readEntity(ctx, new IdType(item.getItem().getReference()));
            itemEntity.setReferenceForm(questionnaireEntity);

        } else if (item.getItem().getReference().contains("List")) {

            ListEntity listEntity = readEntity(ctx, new IdType(item.getItem().getReference()));
            itemEntity.setReferenceListResource(listEntity);

        } else if (item.getItem().getReference().contains("List")) {

            DocumentReferenceEntity documentReferenceEntity = documentDao.readEntity(ctx, new IdType(item.getItem().getReference()));
            itemEntity.setReferenceDocumentReference(documentReferenceEntity);

        }
        em.persist(itemEntity);

    }


}

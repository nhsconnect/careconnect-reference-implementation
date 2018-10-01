package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.carePlan.CarePlanEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireIdentifier;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseIdentifier;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseItem;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseItemAnswer;

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
public class QuestionnaireResponseDao implements QuestionnaireResponseRepository {


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
    private QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer formEntityToFHIRQuestionnaireResponseTransformer;



    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireResponseDao.class);

    @Override
    public void save(FhirContext ctx, QuestionnaireResponseEntity
            form)
    {
        em.persist(form);
    }

    @Override
    public QuestionnaireResponse read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            QuestionnaireResponseEntity formEntity = (QuestionnaireResponseEntity) em.find(QuestionnaireResponseEntity.class, Long.parseLong(theId.getIdPart()));

            return formEntity == null
                    ? null
                    : formEntityToFHIRQuestionnaireResponseTransformer.transform(formEntity);

        } else {
            return null;
        }
    }

    @Override
    public List<QuestionnaireResponse> searchQuestionnaireResponse(FhirContext ctx, TokenParam identifier, StringParam id,ReferenceParam questionnaire, ReferenceParam patient) {
        List<QuestionnaireResponseEntity> qryResults = searchQuestionnaireResponseEntity(ctx, identifier, id, questionnaire, patient);
        List<QuestionnaireResponse> results = new ArrayList<>();

        for (QuestionnaireResponseEntity form : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            QuestionnaireResponse questionnaireResponse = formEntityToFHIRQuestionnaireResponseTransformer.transform(form);
            results.add(questionnaireResponse);
        }

        return results;
    }

    @Override
    public List<QuestionnaireResponseEntity> searchQuestionnaireResponseEntity(FhirContext ctx, TokenParam identifier, StringParam resid, ReferenceParam questionnaire, ReferenceParam patient) {

            List<QuestionnaireResponseEntity> qryResults = null;

            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<QuestionnaireResponseEntity> criteria = builder.createQuery(QuestionnaireResponseEntity.class);

            Root<QuestionnaireResponseEntity> root = criteria.from(QuestionnaireResponseEntity.class);

            List<Predicate> predList = new LinkedList<Predicate>();
            List<Questionnaire> results = new ArrayList<Questionnaire>();

            if (identifier !=null)
            {
                Join<QuestionnaireEntity, QuestionnaireIdentifier> join = root.join("identifiers", JoinType.LEFT);

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
                    Join<QuestionnaireResponseEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                    predList.add(p);
                } else {
                    Join<ObservationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), -1);
                    predList.add(p);
                }
            }

            if (questionnaire != null) {
                if (daoutils.isNumeric(questionnaire.getIdPart())) {
                    Join<QuestionnaireResponseEntity, QuestionnaireEntity> join = root.join("questionnaire", JoinType.LEFT);

                    Predicate p = builder.equal(join.get("id"), questionnaire.getIdPart());
                    predList.add(p);
                } else {
                    Join<QuestionnaireResponseEntity, PatientEntity> join = root.join("questionnaire", JoinType.LEFT);

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
        cq.select(qb.count(cq.from(QuestionnaireResponseEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public QuestionnaireResponseEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            QuestionnaireResponseEntity formEntity = (QuestionnaireResponseEntity) em.find(QuestionnaireResponseEntity.class, Long.parseLong(theId.getIdPart()));

            return formEntity;

        } else {
            return null;
        }
    }

    @Override
    public QuestionnaireResponse create(FhirContext ctx,QuestionnaireResponse form, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException {

        QuestionnaireResponseEntity formEntity = null;
        log.debug("Called QuestionnaireResponse Create Condition Url: "+theConditional);

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/form")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<QuestionnaireResponseEntity> results = searchQuestionnaireResponseEntity(ctx, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/risk"),null,null,null);
                    for (QuestionnaireResponseEntity con : results) {
                        formEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }
        if ((theId != null)) {
            formEntity =  readEntity(ctx,theId);
        }

        if (formEntity == null) {
            formEntity = new QuestionnaireResponseEntity();
        }

        formEntity.setStatus(form.getStatus());

        if (form.hasAuthored()) {
            formEntity.setAuthoredDateTime(form.getAuthored());
        }
        if (form.hasAuthor()) {
            if (form.getAuthor().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(form.getAuthor().getReference()));
                formEntity.setAuthorPatient(patientEntity);
            }
            if (form.getAuthor().getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = null;
                practitionerEntity = practitionerDao.readEntity(ctx, new IdType(form.getAuthor().getReference()));
                formEntity.setAuthorPractitioner(practitionerEntity);
            }
        }
        if (form.hasBasedOn()) {
            for (Reference reference : form.getBasedOn()) {
                if (form.getBasedOn().contains("CarePlan")) {
                    CarePlanEntity carePlanEntity = null;
                    carePlanEntity = carePlanDao.readEntity(ctx, new IdType(reference.getReference()));
                    formEntity.setCarePlan(carePlanEntity);
                }
            }
        }

        if (form.hasContext()) {
            if (form.getContext().getReference().contains("Encounter")) {
                EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(form.getContext().getReference()));
                formEntity.setContextEncounter(encounterEntity);
            }
            if (form.getContext().getReference().contains("EpisodeOfCare")) {
                EpisodeOfCareEntity episodeOfCareEntity = episodeDao.readEntity(ctx, new IdType(form.getContext().getReference()));
                formEntity.setContextEpisodeOfCare(episodeOfCareEntity);
            }
        }

        if (form.hasSubject()) {
            if (form.getSubject().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(form.getSubject().getReference()));
                formEntity.setPatient(patientEntity);
            }
        }

        if (form.hasQuestionnaire()) {
            QuestionnaireEntity questionnaireEntity = questionnaireDao.readEntity(ctx, new IdType(form.getQuestionnaire().getReference()));
            formEntity.setQuestionnaire(questionnaireEntity);
        }
        if (form.hasSource()) {
            if (form.getSource().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(form.getSource().getReference()));
                formEntity.setSourcePatient(patientEntity);
            }
            if (form.getSource().getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = null;
                practitionerEntity = practitionerDao.readEntity(ctx, new IdType(form.getSource().getReference()));
                formEntity.setSourcePractitioner(practitionerEntity);
            }
        }

        em.persist(formEntity);


        if (form.hasIdentifier()) {

            Identifier ident = form.getIdentifier();

            log.debug("QuestionnaireResponse SDS = " + ident.getValue() + " System =" + ident.getSystem());
            QuestionnaireResponseIdentifier formIdentifier = null;

            for (QuestionnaireResponseIdentifier orgSearch : formEntity.getIdentifiers()) {
                if (ident.getSystem().equals(orgSearch.getSystemUri()) && ident.getValue().equals(orgSearch.getValue())) {
                    formIdentifier = orgSearch;
                    break;
                }
            }
            if (formIdentifier == null) {
                formIdentifier = new QuestionnaireResponseIdentifier();
                formIdentifier.setQuestionnaireResponse(formEntity);

            }

            formIdentifier.setValue(ident.getValue());
            formIdentifier.setSystem(codeSystemSvc.findSystem(ident.getSystem()));
            log.debug("QuestionnaireResponse System Code: "+formIdentifier.getSystemUri());

            em.persist(formIdentifier);
        }
        for (QuestionnaireResponseItem itemSearch : formEntity.getItems()) {
           removeItem(itemSearch);
        }
        if (form.hasItem()) {
            for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : form.getItem()) {
                QuestionnaireResponseItem itemEntity = null;
                /*
                if (item.hasLinkId()) {
                    for (QuestionnaireResponseItem itemSearch : formEntity.getItems()) {
                        if (itemSearch.getLinkId() != null && itemSearch.getLinkId().contains(item.getLinkId())) {
                            itemEntity = itemSearch;
                        }
                    }
                }*/
                if (itemEntity == null) { itemEntity = new QuestionnaireResponseItem(); }
                itemEntity.setForm(formEntity);
                buildItem(ctx, item, itemEntity);
            }
        }

       // log.info("Called PERSIST id="+formEntity.getId().toString());
        form.setId(formEntity.getId().toString());

        return form;
    }

    private void removeItem(QuestionnaireResponseItem item) {
        for (QuestionnaireResponseItemAnswer answer : item.getAnswers()) {
            for (QuestionnaireResponseItem itemSearch : answer.getItems()) {
                removeItem(itemSearch);
            }
            em.remove(answer);
        }
        for (QuestionnaireResponseItem itemSearch : item.getItems()) {
            removeItem(itemSearch);
        }
        em.remove(item);
    }

    private void buildItem(FhirContext ctx,QuestionnaireResponse.QuestionnaireResponseItemComponent item, QuestionnaireResponseItem itemEntity ) {

        if (item.hasLinkId()) {
            itemEntity.setLinkId(item.getLinkId());
        }
        if (item.hasDefinition()) {
            itemEntity.setDefinition(item.getDefinition());
        }
        if (item.hasText()) {
            itemEntity.setText(item.getText());
        }
        if (item.hasSubject()) {
            if (item.getSubject().getReference().contains("Patient")) {
                PatientEntity patientEntity = null;
                patientEntity = patientDao.readEntity(ctx, new IdType(item.getSubject().getReference()));
                itemEntity.setPatient(patientEntity);
            }
        }
        em.persist(itemEntity);

        if (item.hasItem()) {
            for (QuestionnaireResponse.QuestionnaireResponseItemComponent subitem : item.getItem()) {

                QuestionnaireResponseItem subItemEntity = null;
                /*
                if (subitem.hasLinkId()) {
                    for (QuestionnaireResponseItem itemSearch : itemEntity.getItems()) {
                        if (itemSearch.getLinkId() != null && itemSearch.getLinkId().contains(subitem.getLinkId())) {
                            itemEntity = itemSearch;
                        }
                    }
                }
                */
                if (subItemEntity == null) { subItemEntity = new QuestionnaireResponseItem(); }

                subItemEntity.setParentItem(itemEntity);
                buildItem(ctx, subitem, subItemEntity);
            }
        }

        if (item.hasAnswer()) {
            for (QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer : item.getAnswer()) {
                QuestionnaireResponseItemAnswer answerEntity = new QuestionnaireResponseItemAnswer();
                answerEntity.setItem(itemEntity);
                try {
                    if (answer.hasValue()) {
                        if (answer.hasValueBooleanType()) {
                            answerEntity.setValueBoolean(answer.getValueBooleanType().booleanValue());
                        }
                        if (answer.hasValueStringType()) {
                            answerEntity.setValueString(answer.getValueStringType().asStringValue());
                        }
                        if (answer.hasValueIntegerType()) {
                            answerEntity.setValueInteger(answer.getValueIntegerType().getValue());
                        }
                        if (answer.hasValueCoding()) {
                            ConceptEntity concept = conceptDao.findAddCode(answer.getValueCoding());
                            if (concept != null) answerEntity.setValueCoding(concept);
                        }
                        if (answer.hasValueReference()) {
                            Reference reference = answer.getValueReference();
                            if (reference.getReference().contains("Condition")) {
                                ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new IdType(reference.getReference()));
                                answerEntity.setReferenceCondition(conditionEntity);
                            } else if (reference.getReference().contains("Observation")) {
                                ObservationEntity observationEntity = observationDao.readEntity(ctx, new IdType(reference.getReference()));
                                answerEntity.setReferenceObservation(observationEntity);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                    throw new InternalErrorException(ex);
                }
                em.persist(answerEntity);
                if (answer.hasItem()) {
                    for (QuestionnaireResponse.QuestionnaireResponseItemComponent subitem : answer.getItem()) {
                        QuestionnaireResponseItem subItemEntity = new QuestionnaireResponseItem();
                        subItemEntity.setParentAnswer(answerEntity);
                        buildItem(ctx, subitem, subItemEntity);
                    }
                }
            }
        }
    }

}

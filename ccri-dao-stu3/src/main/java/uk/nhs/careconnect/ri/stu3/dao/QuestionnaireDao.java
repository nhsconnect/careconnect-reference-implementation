package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.QuestionnaireRepository;
import uk.nhs.careconnect.ri.database.entity.questionnaire.*;
import uk.nhs.careconnect.ri.stu3.dao.transforms.QuestionnaireEntityToFHIRQuestionnaireTransformer;
import uk.nhs.careconnect.ri.database.entity.codeSystem.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class QuestionnaireDao implements QuestionnaireRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private QuestionnaireEntityToFHIRQuestionnaireTransformer questionnaireEntityToFHIRQuestionnaireTransformer;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;


    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private LibDao libDao;


    private static final Logger log = LoggerFactory.getLogger(QuestionnaireDao.class);

    @Override
    public void save(FhirContext ctx, QuestionnaireEntity questionnaire) {
        em.persist(questionnaire);
    }

    @Override
    public List<Questionnaire> search(FhirContext ctx,
                                      TokenParam identifier,
                                      StringParam id,
                                      TokenOrListParam codes,
                                      @OptionalParam(name = Questionnaire.SP_URL) UriParam url,
                                      @OptionalParam(name = Questionnaire.SP_NAME) StringParam name) {
        List<QuestionnaireEntity> qryResults = searchEntity(ctx, identifier, id, codes, url, name);
        List<Questionnaire> results = new ArrayList<>();

        for (QuestionnaireEntity form : qryResults) {

            if (form.getResource() != null) {
                results.add((Questionnaire) ctx.newJsonParser().parseResource(form.getResource()));
            } else {

                Questionnaire questionnaire = questionnaireEntityToFHIRQuestionnaireTransformer.transform(form);
                /*
                String resource = ctx.newJsonParser().encodeResourceToString(questionnaire);
                if (resource.length() < 10000) {
                    form.setResource(resource);
                    em.persist(form);
                }
                */
                results.add(questionnaire);

            }
        }

        return results;
    }

    @Override
    public List<QuestionnaireEntity> searchEntity(FhirContext ctx,
                                                  TokenParam identifier,
                                                  StringParam resid,
                                                  TokenOrListParam codes,
                                                  @OptionalParam(name = Questionnaire.SP_URL) UriParam url,
                                                  @OptionalParam(name = Questionnaire.SP_NAME) StringParam name) {
        List<QuestionnaireEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<QuestionnaireEntity> criteria = builder.createQuery(QuestionnaireEntity.class);
        Root<QuestionnaireEntity> root = criteria.from(QuestionnaireEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Questionnaire> results = new ArrayList<Questionnaire>();

        if (identifier != null) {
            Join<QuestionnaireEntity, QuestionnaireIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"), identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"), resid.getValue());
            predList.add(p);
        }

        if (name != null) {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal("%" + name.getValue() + "%"))
                    );

            predList.add(p);
        }

        if (codes != null) {

            List<Predicate> predOrList = new LinkedList<Predicate>();
            Join<QuestionnaireEntity, ConceptEntity> joinConcept = root.join("questionnaireCode", JoinType.LEFT);
            Join<ConceptEntity, CodeSystemEntity> joinCodeSystem = joinConcept.join("codeSystemEntity", JoinType.LEFT);

            for (TokenParam code : codes.getValuesAsQueryTokens()) {
                log.trace("Search on Questionnaire.code code = " + code.getValue());

                Predicate p = null;
                if (code.getSystem() != null) {
                    p = builder.and(builder.equal(joinCodeSystem.get("codeSystemUri"), code.getSystem()), builder.equal(joinConcept.get("code"), code.getValue()));
                } else {
                    p = builder.equal(joinConcept.get("code"), code.getValue());
                }
                predOrList.add(p);

            }
            if (predOrList.size() > 0) {
                Predicate p = builder.or(predOrList.toArray(new Predicate[0]));
                predList.add(p);
            }

        }

        if (url != null) {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("url").as(String.class)),
                            builder.upper(builder.literal(url.getValue()))
                    );

            predList.add(p);
        }

        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size() > 0) {
            criteria.select(root).where(predArray);
        } else {
            criteria.select(root);
        }

        qryResults = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS).getResultList();
        return qryResults;
    }

    @Override
    public Questionnaire read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            QuestionnaireEntity questionnaireEntity = (QuestionnaireEntity) em.find(QuestionnaireEntity.class, Long.parseLong(theId.getIdPart()));

            if (questionnaireEntity == null) return null;

            Questionnaire questionnaire = questionnaireEntityToFHIRQuestionnaireTransformer.transform(questionnaireEntity);
            if (questionnaireEntity.getResource() == null) {
                String resource = ctx.newJsonParser().encodeResourceToString(questionnaire);
                if (resource.length() < 10000) {
                    questionnaireEntity.setResource(resource);
                    em.persist(questionnaireEntity);
                }
            }
            return questionnaire;

        } else {
            return null;
        }
    }

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(QuestionnaireEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public QuestionnaireEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            QuestionnaireEntity questionnaireEntity = (QuestionnaireEntity) em.find(QuestionnaireEntity.class, Long.parseLong(theId.getIdPart()));

            return questionnaireEntity;

        } else {
            return null;
        }
    }

    @Override
    public Questionnaire create(FhirContext ctx, Questionnaire questionnaire, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException {

        QuestionnaireEntity questionnaireEntity = null;
        log.debug("Called Questionnaire Create Condition Url: " + theConditional);

        if (theId != null && daoutils.isNumeric(theId.getIdPart())) {
            log.debug("theId.getIdPart()=" + theId.getIdPart());
            questionnaireEntity = em.find(QuestionnaireEntity.class, Long.parseLong(theId.getIdPart()));
        }

        List<QuestionnaireEntity> entries = searchEntity(ctx, null, null, null, new UriParam().setValue(questionnaire.getUrl()), null);
        for (QuestionnaireEntity msg : entries) {
            if (questionnaire.getId() == null) {
                throw new ResourceVersionConflictException("Url " + msg.getUrl() + " is already present on the system " + msg.getId());
            }

            if (!msg.getId().toString().equals(questionnaire.getIdElement().getIdPart())) {
                throw new ResourceVersionConflictException("Questionnaire url " + msg.getUrl() + " is already present on the system " + msg.getId());
            }
        }
        /*

        Conditional Removed

         */


        if (questionnaireEntity == null) {
            questionnaireEntity = new QuestionnaireEntity();
        }

        questionnaireEntity.setResource(null);

        if (questionnaire.hasUrl()) {
            questionnaireEntity.setUrl(questionnaire.getUrl());
        }

        if (questionnaire.hasCode()) {
            ConceptEntity concept = conceptDao.findAddCode(questionnaire.getCodeFirstRep());
            if (concept != null) questionnaireEntity.setQuestionnaireCode(concept);
        }

        if (questionnaire.hasApprovalDate()) {
            questionnaireEntity.setApprovalDateTime(questionnaire.getApprovalDate());
        }
        if (questionnaire.hasDate()) {
            questionnaireEntity.setDateTime(questionnaire.getDate());
        }
        if (questionnaire.hasLastReviewDate()) {
            questionnaireEntity.setLastReviewDateTime(questionnaire.getLastReviewDate());
        }
        if (questionnaire.hasName()) {
            questionnaireEntity.setName(questionnaire.getName());
        }

        if (questionnaire.hasPurpose()) {
            questionnaireEntity.setPurpose(questionnaire.getPurpose());
        }
        if (questionnaire.hasStatus()) {
            questionnaireEntity.setStatus(questionnaire.getStatus());
        }
        if (questionnaire.hasSubjectType()) {
            for (CodeType code : questionnaire.getSubjectType()) {
                //      log.info(code.getValue());
                questionnaireEntity.setSubjectType(code.getValue());
            }
        }
        if (questionnaire.hasTitle()) {
            questionnaireEntity.setTitle(questionnaire.getTitle());
        }
        if (questionnaire.hasVersion()) {
            questionnaireEntity.setVersion(questionnaire.getVersion());
        }
        if (questionnaire.hasDescription()) {
            questionnaireEntity.setDescription(questionnaire.getDescription());
        }
        if (questionnaire.hasPublisher()) {
            questionnaireEntity.setPublisher(questionnaire.getPublisher());
        }
        if (questionnaire.hasCopyright()) {
            questionnaireEntity.setCopyright(questionnaire.getCopyright());
        }

        em.persist(questionnaireEntity);

        for (Identifier ident : questionnaire.getIdentifier()) {
            log.debug("Questionnaire SDS = " + ident.getValue() + " System =" + ident.getSystem());
            QuestionnaireIdentifier questionnaireIdentifier = null;

            for (QuestionnaireIdentifier orgSearch : questionnaireEntity.getIdentifiers()) {
                if (ident.getSystem().equals(orgSearch.getSystemUri()) && ident.getValue().equals(orgSearch.getValue())) {
                    questionnaireIdentifier = orgSearch;
                    break;
                }
            }
            if (questionnaireIdentifier == null) {
                questionnaireIdentifier = new QuestionnaireIdentifier();
                questionnaireIdentifier.setQuestionnaire(questionnaireEntity);

            }

            questionnaireIdentifier = (QuestionnaireIdentifier) libDao.setIdentifier(ident, questionnaireIdentifier);
            log.debug("Questionnaire System Code: " + questionnaireIdentifier.getSystemUri());

            em.persist(questionnaireIdentifier);
        }

        for (QuestionnaireItem item : questionnaireEntity.getItems()) {
            removeItem(item);
        }
        questionnaireEntity.setItems(new HashSet<>());

        if (questionnaire.hasItem()) {


            for (Questionnaire.QuestionnaireItemComponent itemComponent : questionnaire.getItem()) {
                QuestionnaireItem item = new QuestionnaireItem();
                item.setForm(questionnaireEntity);

                questionnaireEntity.getItems().add(buildItem(itemComponent, item));
            }
        }


        // log.info("Called PERSIST id="+questionnaireEntity.getId().toString());
        questionnaire.setId(questionnaireEntity.getId().toString());

        log.debug("Called PERSIST id=" + questionnaireEntity.getId().toString());
        questionnaire.setId(questionnaireEntity.getId().toString());

        Questionnaire newQuestionnaire = null;
        if (questionnaireEntity != null) {
            newQuestionnaire = questionnaireEntityToFHIRQuestionnaireTransformer.transform(questionnaireEntity);
            String resource = ctx.newJsonParser().encodeResourceToString(newQuestionnaire);
           /* if (resource.length() < 10000) {
                questionnaireEntity.setResource(resource);
                em.persist(questionnaireEntity);
            }*/

        }

        return newQuestionnaire;
    }

    private void removeItem(QuestionnaireItem item) {
        for (QuestionnaireItem itemChild : item.getChildItems()) {
            removeItem(itemChild);
        }
        for (QuestionnaireItemEnable enable : item.getEnabled()) {
            em.remove(enable);
        }
        for (QuestionnaireItemOptions options : item.getOptions()) {
            em.remove(options);
        }
        for (QuestionnaireItemCode code : item.getCodes()) {
            em.remove(code);
        }
        em.remove(item);
    }

    private QuestionnaireItem buildItem(Questionnaire.QuestionnaireItemComponent itemComponent, QuestionnaireItem item) {
        if (itemComponent.hasType()) {
            item.setItemType(itemComponent.getType());
        }
        if (itemComponent.hasLinkId()) {
            item.setLinkId(itemComponent.getLinkId());
        }
        if (itemComponent.hasPrefix()) {
            item.setPrefix(itemComponent.getPrefix());
        }
        if (itemComponent.hasReadOnly()) {
            item.setReadOnly(itemComponent.getReadOnly());
        }
        if (itemComponent.hasRepeats()) {
            item.setRepeats(itemComponent.getRepeats());
        }
        if (itemComponent.hasRequired()) {
            item.setRequired(itemComponent.getRequired());
        }
        if (itemComponent.hasText()) {
            item.setItemText(itemComponent.getText());
        }
        if (itemComponent.hasOptions()) {
            item.setValueSetOptions(itemComponent.getOptions().getReference());
        }


        if (itemComponent.hasDefinition()) {
            item.setDefinitionUri(itemComponent.getDefinition());
        }

        for (Extension extension : itemComponent.getExtension()) {

            if (extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedProfile")) {
                // TO_DONE KGM 5/2/2019
                //  log.info(extension.getUrl());
                if (extension.getValue() instanceof Reference) {
                    item.setAllowedProfile(((Reference) extension.getValue()).getReference());
                }

            }

            if (extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")) {
                // TO_DONE KGM 5/2/2019
                //   log.info(extension.getUrl());
                if (extension.getValue() instanceof CodeType) {
                    item.setAllowedResource(((CodeType) extension.getValue()).getValue());
                }

            }
            if (extension.getUrl().equals("http://hl7.org/fhir/StructureDefinition/designNote")) {
                item.setDesignNote(((MarkdownType) extension.getValue()).toString());
            }
        }

        em.persist(item);

        if (itemComponent.hasEnableWhen()) {
            for (Questionnaire.QuestionnaireItemEnableWhenComponent when : itemComponent.getEnableWhen()) {
                QuestionnaireItemEnable enable = new QuestionnaireItemEnable();
                enable.setQuestionnaireItem(item);
                if (when.hasHasAnswer()) {
                    enable.setHasAnswer(when.getHasAnswer());
                }
                if (when.hasQuestion()) {
                    enable.setQuestion(when.getQuestion());
                }
                if (when.hasAnswerCoding()) {
                    ConceptEntity concept = conceptDao.findAddCode(when.getAnswerCoding());
                    if (concept != null) enable.setAnswerCode(concept);
                }
                if (when.hasAnswerStringType()) {
                    enable.setAnswerString(when.getAnswerStringType().toString());
                }
                if (when.hasAnswerDateType()) {
                    enable.setAnswerDateTime(when.getAnswerDateType().getValueAsString());
                }
                if (when.hasAnswerIntegerType()) {
                    enable.setAnswerInteger(when.getAnswerIntegerType().getValueAsString());
                }
                em.persist(enable);
            }
        }

        if (itemComponent.hasOption()) {
            for (Questionnaire.QuestionnaireItemOptionComponent option : itemComponent.getOption()) {
                QuestionnaireItemOptions optionEntity = new QuestionnaireItemOptions();
                optionEntity.setQuestionnaireItem(item);
                if (option.hasValueCoding()) {
                    ConceptEntity concept = conceptDao.findAddCode(option.getValueCoding());
                    if (concept != null) optionEntity.setValueCode(concept);
                }
                if (option.hasValueStringType()) {
                    optionEntity.setValueString(option.getValueStringType().toString());
                }
                if (option.hasValueDateType()) {
                    optionEntity.setValueDateTime(option.getValueDateType().getValueAsString());
                }
                if (option.hasValueIntegerType()) {
                    optionEntity.setValueInteger(option.getValueIntegerType().getValueAsString());
                }
                em.persist(optionEntity);
            }
        }

        if (itemComponent.hasCode()) {
            for (Coding code : itemComponent.getCode()) {
                QuestionnaireItemCode itemCode = new QuestionnaireItemCode();
                itemCode.setQuestionnaireItem(item);

                ConceptEntity concept = conceptDao.findAddCode(code);
                if (concept != null) itemCode.setCode(concept);

                em.persist(itemCode);
            }
        }

        for (Questionnaire.QuestionnaireItemComponent subItem : itemComponent.getItem()) {
            QuestionnaireItem subItemEntity = new QuestionnaireItem();
            subItemEntity.setQuestionnaireParentItem(item);
            item.getChildItems().add(buildItem(subItem, subItemEntity));
        }
        return item;
    }

}

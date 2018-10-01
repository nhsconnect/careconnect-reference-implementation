package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
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
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.QuestionnaireRepository;
import uk.nhs.careconnect.ri.dao.transforms.QuestionnaireEntityToFHIRQuestionnaireTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireIdentifier;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireItem;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
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



    private static final Logger log = LoggerFactory.getLogger(QuestionnaireDao.class);

    @Override
    public void save(FhirContext ctx, QuestionnaireEntity questionnaire)
    {
        em.persist(questionnaire);
    }

    @Override
    public List<Questionnaire> searchQuestionnaire(FhirContext ctx, TokenParam identifier, StringParam id, TokenOrListParam codes) {
        List<QuestionnaireEntity> qryResults = searchQuestionnaireEntity(ctx, identifier, id, codes);
        List<Questionnaire> results = new ArrayList<>();

        for (QuestionnaireEntity form : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Questionnaire questionnaireResponse = questionnaireEntityToFHIRQuestionnaireTransformer.transform(form);
            results.add(questionnaireResponse);
        }

        return results;
    }

    @Override
    public List<QuestionnaireEntity> searchQuestionnaireEntity(FhirContext ctx, TokenParam identifier, StringParam resid, TokenOrListParam codes) {
        List<QuestionnaireEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<QuestionnaireEntity> criteria = builder.createQuery(QuestionnaireEntity.class);
        Root<QuestionnaireEntity> root = criteria.from(QuestionnaireEntity.class);

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
        
        if (codes != null) {
            
                List<Predicate> predOrList = new LinkedList<Predicate>();
                Join<QuestionnaireEntity, ConceptEntity> joinConcept = root.join("questionnaireCode", JoinType.LEFT);
                Join<ConceptEntity, CodeSystemEntity> joinCodeSystem = joinConcept.join("codeSystemEntity", JoinType.LEFT);

                for (TokenParam code : codes.getValuesAsQueryTokens()) {
                    log.trace("Search on Questionnaire.code code = " + code.getValue());

                    Predicate p = null;
                    if (code.getSystem() != null) {
                        p = builder.and(builder.equal(joinCodeSystem.get("codeSystemUri"), code.getSystem()),builder.equal(joinConcept.get("code"), code.getValue()));
                    } else {
                        p = builder.equal(joinConcept.get("code"), code.getValue());
                    }
                    predOrList.add(p);

                }
                if (predOrList.size()>0) {
                    Predicate p = builder.or(predOrList.toArray(new Predicate[0]));
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
    public Questionnaire read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            QuestionnaireEntity questionnaireEntity = (QuestionnaireEntity) em.find(QuestionnaireEntity.class, Long.parseLong(theId.getIdPart()));

            return questionnaireEntity == null
                    ? null
                    : questionnaireEntityToFHIRQuestionnaireTransformer.transform(questionnaireEntity);

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
    public Questionnaire create(FhirContext ctx,Questionnaire questionnaire, @IdParam IdType theId, @ConditionalUrlParam String theConditional) throws OperationOutcomeException {

        QuestionnaireEntity questionnaireEntity = null;
        log.debug("Called Questionnaire Create Condition Url: "+theConditional);
        if (questionnaire.hasId()) {
            questionnaireEntity =  (QuestionnaireEntity) em.find(QuestionnaireEntity.class,Long.parseLong(questionnaire.getId()));
        }

        /*

        Conditional Removed

         */

        if (questionnaireEntity == null) {
            questionnaireEntity = new QuestionnaireEntity();
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

        if (questionnaire.hasStatus()) {
            questionnaireEntity.setStatus(questionnaire.getStatus());
        }
        if (questionnaire.hasSubjectType()) {
            questionnaireEntity.setSubjectType(questionnaire.getResourceType());
        }
        if (questionnaire.hasTitle()) {
            questionnaireEntity.setTitle(questionnaire.getTitle());
        }
        if (questionnaire.hasVersion()) {
            questionnaireEntity.setVersion(questionnaire.getVersion());
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

            questionnaireIdentifier.setValue(ident.getValue());
            questionnaireIdentifier.setSystem(codeSystemSvc.findSystem(ident.getSystem()));
            log.debug("Questionnaire System Code: "+questionnaireIdentifier.getSystemUri());

            em.persist(questionnaireIdentifier);
        }
        if (questionnaire.hasItem()) {
            for (Questionnaire.QuestionnaireItemComponent itemComponent : questionnaire.getItem()) {
                QuestionnaireItem item = new QuestionnaireItem();
                item.setForm(questionnaireEntity);
                buildItem(itemComponent,item);

            }
        }


       // log.info("Called PERSIST id="+questionnaireEntity.getId().toString());
        questionnaire.setId(questionnaireEntity.getId().toString());

        return questionnaire;
    }

    private void buildItem(Questionnaire.QuestionnaireItemComponent itemComponent, QuestionnaireItem item ) {
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
        for (Extension extension : itemComponent.getExtension()) {
            if (extension.getUrl().contains("http://hl7.org/fhir/StructureDefinition/questionnaire-allowedResource")) {
                // TODO
            }
        }

        em.persist(item);

        for (Questionnaire.QuestionnaireItemComponent subItem : itemComponent.getItem()) {
            QuestionnaireItem subItemEntity = new QuestionnaireItem();
            subItemEntity.setQuestionnaireParentItem(item);
            buildItem(subItem, subItemEntity);
        }

    }



}

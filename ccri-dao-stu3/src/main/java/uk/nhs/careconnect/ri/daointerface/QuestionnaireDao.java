package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.daointerface.transforms.QuestionnaireEntityToFHIRQuestionnaireTransformer;
import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireEntity;
import uk.nhs.careconnect.ri.entity.questionnaire.QuestionnaireIdentifier;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;

@Repository
@Transactional
public class QuestionnaireDao implements QuestionnaireRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private QuestionnaireEntityToFHIRQuestionnaireTransformer questionnaireEntityToFHIRQuestionnaireTransformer;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private ConceptRepository codeSvc;


    private static final Logger log = LoggerFactory.getLogger(QuestionnaireDao.class);

    @Override
    public void save(FhirContext ctx,QuestionnaireEntity questionnaire)
    {
        em.persist(questionnaire);
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
        questionnaireEntity.setName(questionnaire.getName());
        questionnaireEntity.setStatus(questionnaire.getStatus());


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



       // log.info("Called PERSIST id="+questionnaireEntity.getId().toString());
        questionnaire.setId(questionnaireEntity.getId().toString());

        return questionnaire;
    }




}

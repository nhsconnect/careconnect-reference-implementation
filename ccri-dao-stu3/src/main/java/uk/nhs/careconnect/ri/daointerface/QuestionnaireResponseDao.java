package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.daointerface.transforms.QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer;
import uk.nhs.careconnect.ri.entity.questionnaireResponse.QuestionnaireResponseEntity;
import uk.nhs.careconnect.ri.entity.questionnaireResponse.QuestionnaireResponseIdentifier;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;

@Repository
@Transactional
public class QuestionnaireResponseDao implements QuestionnaireResponseRepository {


    @PersistenceContext
    EntityManager em;

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
        if (form.hasId()) {
            formEntity =  (QuestionnaireResponseEntity) em.find(QuestionnaireResponseEntity.class,Long.parseLong(form.getId()));
        }

        /*

        Conditional Removed

         */

        if (formEntity == null) {
            formEntity = new QuestionnaireResponseEntity();
        }

        formEntity.setStatus(form.getStatus());


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



       // log.info("Called PERSIST id="+formEntity.getId().toString());
        form.setId(formEntity.getId().toString());

        return form;
    }




}

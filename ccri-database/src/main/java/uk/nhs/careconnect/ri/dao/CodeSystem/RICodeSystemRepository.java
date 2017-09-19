package uk.nhs.careconnect.ri.dao.CodeSystem;

import ca.uhn.fhir.rest.method.RequestDetails;
import org.hl7.fhir.instance.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import uk.nhs.careconnect.ri.dao.ValueSet.RIValueSetRepository;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptParentChildLink;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
public class RICodeSystemRepository implements CodeSystemRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private PlatformTransactionManager myTransactionMgr;

    @Autowired
    private TransactionTemplate transactionTemplate;

    Integer flushCount = 1000;
    Integer flushNumber = 0;
    Integer level = 0;

    private static final Logger log = LoggerFactory.getLogger(RIValueSetRepository.class);

    private boolean myProcessDeferred = true;


    private List<ConceptEntity> myConceptsToSaveLater = new ArrayList<ConceptEntity>();

    private List<ConceptParentChildLink> myConceptLinksToSaveLater = new ArrayList<ConceptParentChildLink>();

    // What we need to do is process concepts coming from ValueSets in a transactional mode (@Transactional)
    // For CodeSystem inserts we need to get the codes into the database as without storing them in massive memory objects
    // Other resources should run as transactional

    @Override
    public void setProcessDeferred(boolean theProcessDeferred) {
        myProcessDeferred = theProcessDeferred;
    }


    private void emPersist(Object object) {
        TransactionTemplate tt = new TransactionTemplate(myTransactionMgr);
        tt.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                em.persist(object);
                em.flush();
            }

        });
    }


    @Override
    public void storeNewCodeSystemVersion(String theSystem, CodeSystemEntity theCodeSystem, RequestDetails theRequestDetails) {
        em.setFlushMode(FlushModeType.AUTO);
        CodeSystemEntity worker = findBySystem(theSystem);
        log.info("Starting Code Processing CodeSystem.id = "+worker.getId());
        for (ConceptEntity conceptEntity : theCodeSystem.getConcepts()) {
            findAddCode(worker, conceptEntity);
        }
        log.info("Finished Code Processing");
    }

    @Override
    public CodeSystemEntity findBySystem(String system) {

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CodeSystemEntity codeSystemEntity = null;
        CriteriaQuery<CodeSystemEntity> criteria = builder.createQuery(CodeSystemEntity.class);

        Root<CodeSystemEntity> root = criteria.from(CodeSystemEntity.class);
        List<Predicate> predList = new LinkedList<Predicate>();
        log.info("Looking for CodeSystem = " + system);
        log.info("FlushMode = "+em.getFlushMode());
        log.info("Entity Manager Properties = "+ em.getProperties().toString());
        Predicate p = builder.equal(root.<String>get("codeSystemUri"),system);
        predList.add(p);
        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            log.info("Found CodeSystem "+system);
            criteria.select(root).where(predArray);

            List<CodeSystemEntity> qryResults = em.createQuery(criteria).getResultList();

            for (CodeSystemEntity cme : qryResults) {
                codeSystemEntity = cme;
                break;
            }
        }
        if (codeSystemEntity == null) {
            log.info("Not found adding CodeSystem = "+system);
            codeSystemEntity = new CodeSystemEntity();
            codeSystemEntity.setCodeSystemUri(system);
            emPersist(codeSystemEntity);
        }
        return codeSystemEntity;
    }

    public void saveAndFlush(Object entity) {
        flushCount--;
        if (flushCount<1) {
            flushNumber++;
            log.info("Flush: "+flushNumber+ " @level: "+level);

            flushCount=1000;
        }
        emPersist(entity);

    }


    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ConceptEntity concept) {
        // This inspects codes already present and if not found it adds the code... CRUDE at present
        level++;

        ConceptEntity conceptEntity = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ConceptEntity> criteria = builder.createQuery(ConceptEntity.class);
        Root<ConceptEntity> root = criteria.from(ConceptEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<ConceptEntity> results = new ArrayList<ConceptEntity>();
        List<ConceptEntity> qryResults = null;
        log.info("Looking for code ="+concept.getCode()+" in "+codeSystemEntity.getId());
        Predicate pcode = builder.equal(root.get("code"), concept.getCode());
        predList.add(pcode);

        Predicate psystem = builder.equal(root.get("codeSystemEntity"), codeSystemEntity.getId());
        predList.add(psystem);

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
        TypedQuery<ConceptEntity> qry = em.createQuery(criteria);
        qry.setHint("javax.persistence.cache.storeMode", "REFRESH");
        qryResults = qry.getResultList();

        if (qryResults.size() > 0) {
            conceptEntity = qryResults.get(0);
            log.info("Found for code="+concept.getCode()+" ConceptEntity.Id="+conceptEntity.getId());
        } else {
            log.info("Not found existing entry for code="+concept.getCode());
        }


        if (conceptEntity == null) {
            log.info("Add new code =" + concept.getCode());
            conceptEntity = new ConceptEntity()
                    .setCode(concept.getCode())
                    .setCodeSystem(codeSystemEntity)
                    .setDisplay(concept.getDisplay()
                    );


            saveAndFlush(conceptEntity);

        } else {
            if (conceptEntity.getDisplay() == null || conceptEntity.getDisplay().isEmpty()) {
                conceptEntity.setDisplay(conceptEntity.getDisplay());
                saveAndFlush(conceptEntity);
            }
        }

        // call child code
      //  if ((concept.getChildren().size() > 0) && (level < 6)) {
        if ((concept.getChildren().size() > 0) ) {
            processChildConcepts(concept,conceptEntity);
        }

        level--;
        return conceptEntity;
    }


    private void processChildConcepts(ConceptEntity concept, ConceptEntity parentConcept) {
        String lastConcept = null;
        // Simple check to ensure codes are being repeated
        for (ConceptParentChildLink conceptChild : concept.getChildren()) {
            ConceptParentChildLink childLink = null;

            if (conceptChild.getChild().getCode() != null  && !conceptChild.getChild().getCode().equals(lastConcept)) {
                lastConcept = conceptChild.getChild().getCode();
                // Look in the parentConcept for existing link
                for (ConceptParentChildLink conceptChildLink : parentConcept.getChildren()) {
                    if (conceptChildLink.getChild().getCode().equals(concept.getCode())) {
                        childLink = conceptChildLink;
                    }
                }
                if (childLink == null) {
                    // TODO We are assuming child code doesn't exist, so just inserts.
                    childLink = new ConceptParentChildLink();
                    childLink.setParent(parentConcept);
                    childLink.setRelationshipType(ConceptParentChildLink.RelationshipTypeEnum.ISA);
                    childLink.setCodeSystem(parentConcept.getCodeSystem());

                    ConceptEntity childConcept = findAddCode(parentConcept.getCodeSystem(), conceptChild.getChild());


                    childLink.setChild(childConcept);
                    saveAndFlush(childLink);

                    // ensure link add to object
                    parentConcept.getChildren().add(childLink);

                }
            }
        }
    }



    @Override
    @Transactional
    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ValueSet.ConceptDefinitionComponent concept) {
        // This inspects codes already present and if not found it adds the code... CRUDE at present



        ConceptEntity conceptEntity = null;
        for (ConceptEntity codeSystemConcept : codeSystemEntity.getConcepts()) {
            if (codeSystemConcept.getCode().equals(concept.getCode())) {

                conceptEntity =codeSystemConcept;
            }

        }
        if (conceptEntity == null) {
            log.info("Add new code = " + concept.getCode());
            conceptEntity = new ConceptEntity()
                    .setCode(concept.getCode()).setCodeSystem(codeSystemEntity)
                    .setDisplay(concept.getDisplay())
                    .setAbstractCode(concept.getAbstract());


            em.persist(conceptEntity);
            // Need to ensure the local version has a copy of the data
            codeSystemEntity.getConcepts().add(conceptEntity);
        }
        // call child code
        if (concept.getConcept().size() > 0) {
            processChildConcepts(concept,conceptEntity);
        }

        return conceptEntity;
    }

    @Transactional
    private void processChildConcepts(ValueSet.ConceptDefinitionComponent concept, ConceptEntity parentConcept) {
        String lastConcept="";
        for (ValueSet.ConceptDefinitionComponent conceptChild : concept.getConcept()) {
            ConceptParentChildLink childLink = null;

            if (conceptChild.getCode() != null && !conceptChild.getCode().equals(lastConcept)) {
                // To cope with repeating entries
                lastConcept = conceptChild.getCode();
                for (ConceptParentChildLink conceptChildLink : parentConcept.getChildren()) {
                    if (conceptChildLink.getChild().getCode().equals(concept.getCode())) {
                        childLink = conceptChildLink;
                    }
                }
                if (childLink == null) {
                    // TODO We are assuming child code doesn't exist, so just inserts.
                    childLink = new ConceptParentChildLink();
                    childLink.setParent(parentConcept);
                    childLink.setRelationshipType(ConceptParentChildLink.RelationshipTypeEnum.ISA);
                    childLink.setCodeSystem(parentConcept.getCodeSystem());


                    ConceptEntity childConcept = findAddCode(parentConcept.getCodeSystem(), conceptChild);


                    childLink.setChild(childConcept);
                    em.persist(childLink);
                    // ensure link add to object
                    parentConcept.getChildren().add(childLink);

                }
            }
        }
    }
}

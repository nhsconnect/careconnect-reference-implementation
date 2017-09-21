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
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptParentChildLink;
import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.*;

@Repository
public class RICodeSystemRepository implements CodeSystemRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private PlatformTransactionManager myTransactionMgr;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ConceptRepository conceptRepository;


    Integer flushCount = 1000;
    Integer flushNumber = 0;
    Integer level = 0;

    private static final Logger log = LoggerFactory.getLogger(RICodeSystemRepository.class);

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


    private void codeSystemSave(CodeSystemEntity object) {
        TransactionTemplate tt = new TransactionTemplate(myTransactionMgr);
        tt.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                em.persist(object);
                em.flush();
                log.info("Saved CodeSystemId = "+object.getId());
            }

        });
    }

    private void childLinkSave(ConceptParentChildLink object) {
        TransactionTemplate tt = new TransactionTemplate(myTransactionMgr);
        tt.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                em.persist(object);
                em.flush();
                log.info("Saved CodeSystemId = "+object.getId());
            }

        });
    }



    @Override
    public void storeNewCodeSystemVersion(String theSystem, CodeSystemEntity theCodeSystem, RequestDetails theRequestDetails) {
        em.setFlushMode(FlushModeType.AUTO);
        CodeSystemEntity worker = findBySystem(theSystem);
        log.info("Starting Code Processing CodeSystem.id = "+worker.getId());
        log.info("Adding Concepts - Number of Concepts CodeSystem.id = "+theCodeSystem.getConcepts().size());
        for (ConceptEntity conceptEntity : theCodeSystem.getConcepts()) {
            findAddCode(worker, conceptEntity, true );
        }
        log.info("Finished Code Processing");
    }

    @Override
    public void storeConcepts(Map<String, ConceptEntity> code2concept, String codeSystemUri, RequestDetails theRequestDetails) {
        log.info("ConceptRepository storeConcepts");
        CodeSystemEntity codeSystemEntity = findBySystem(codeSystemUri);
        log.info("Store Concepts Processing: Number of concepts"+code2concept.size());

        for ( ConceptEntity currentConcept : code2concept.values() ) {
            ConceptEntity concept = conceptRepository.findCode(codeSystemEntity, currentConcept.getCode());
            if (concept == null) {
                conceptRepository.addCode(currentConcept.getCode(),currentConcept.getDisplay(),codeSystemEntity);
            }
        }
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

            codeSystemSave(codeSystemEntity);
        }
        return codeSystemEntity;
    }
    @Override
    public SystemEntity findSystem(String system) {

        CriteriaBuilder builder = em.getCriteriaBuilder();

        SystemEntity systemEntity = null;
        CriteriaQuery<SystemEntity> criteria = builder.createQuery(SystemEntity.class);

        Root<SystemEntity> root = criteria.from(SystemEntity.class);
        List<Predicate> predList = new LinkedList<Predicate>();
        log.debug("Looking for System = " + system);

        Predicate p = builder.equal(root.<String>get("codeSystemUri"),system);
        predList.add(p);
        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            log.debug("Found System "+system);
            criteria.select(root).where(predArray);

            List<SystemEntity> qryResults = em.createQuery(criteria).getResultList();

            for (SystemEntity cme : qryResults) {
                systemEntity = cme;
                break;
            }
        }
        if (systemEntity == null) {
            log.info("Not found. Adding SystemEntity = "+system);
            systemEntity = new SystemEntity();
            systemEntity.setUri(system);

            em.persist(systemEntity);
        }
        return systemEntity;
    }


    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntityDb, ConceptEntity conceptModel, boolean processChildren) {
        // This inspects codes already present and if not found it adds the code... CRUDE at present
        level++;

        ConceptEntity conceptEntity = conceptRepository.findCode(codeSystemEntityDb.getCodeSystemUri(),conceptModel.getCode());

        if (conceptEntity == null) {
            conceptEntity = conceptRepository.addCode(conceptModel.getCode(),conceptModel.getDisplay(),codeSystemEntityDb);

        } else {
            if (conceptEntity.getDisplay() == null || conceptEntity.getDisplay().isEmpty()) {
                conceptEntity.setDisplay(conceptEntity.getDisplay());
                conceptRepository.save(conceptEntity);
            }
        }

        if ((processChildren) && (conceptModel.getChildren().size() > 0) ) {
            processChildConcepts(conceptModel,conceptEntity);
        }

        level--;
        return conceptEntity;
    }

    private ConceptParentChildLink findChildLinks(ConceptEntity conceptModel, ConceptEntity parentConceptDb) {
        ConceptParentChildLink childLink = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ConceptParentChildLink> criteria = builder.createQuery(ConceptParentChildLink.class);
        Root<ConceptParentChildLink> root = criteria.from(ConceptParentChildLink.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<ConceptParentChildLink> results = new ArrayList<ConceptParentChildLink>();

        Join<ConceptParentChildLink,ConceptEntity> join = root.join("child", JoinType.LEFT);

        Predicate parentid = builder.equal(root.get("parent"), parentConceptDb.getId());
        predList.add(parentid);
        Predicate pchildcode = builder.equal(join.get("code"), conceptModel.getCode());
        predList.add(pchildcode);

        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);

        criteria.select(root).where(predArray);

        List<ConceptParentChildLink> qryResults = em.createQuery(criteria).getResultList();

        for (ConceptParentChildLink child :qryResults) {
            log.info("Found for childcodelink code="+conceptModel.getCode()+" childLink.Id="+childLink.getId());
            childLink = child;
            break;
        }
        return childLink;
    }

    private void processChildConcepts(ConceptEntity conceptModel, ConceptEntity parentConceptDb) {
        String lastConcept = null;
        // Simple check to ensure codes are being repeated
        for (ConceptParentChildLink conceptChild : conceptModel.getChildren()) {

            if (conceptChild.getChild().getCode() != null  && !conceptChild.getChild().getCode().equals(lastConcept)) {
                lastConcept = conceptChild.getChild().getCode();
                // Look in the parentConcept for existing link
                ConceptParentChildLink childLink = null;

                for (ConceptParentChildLink conceptChildLink : parentConceptDb.getChildren()) {
                    if (conceptChildLink.getChild().getCode().equals(conceptModel.getCode())) {
                        childLink = conceptChildLink;
                    }
                }

                if (childLink == null) {
                    childLink = findChildLinks(conceptModel,parentConceptDb);
                }

                if (childLink == null) {
                    // TODO We are assuming child code doesn't exist, so just inserts.
                    childLink = new ConceptParentChildLink();
                    childLink.setParent(parentConceptDb);
                    childLink.setRelationshipType(ConceptParentChildLink.RelationshipTypeEnum.ISA);
                    childLink.setCodeSystem(parentConceptDb.getCodeSystem());

                    ConceptEntity childConcept = findAddCode(parentConceptDb.getCodeSystem(), conceptChild.getChild(), true);

                    childLink.setChild(childConcept);
                    childLinkSave(childLink);

                    // ensure link add to object
                    parentConceptDb.getChildren().add(childLink);

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
            log.debug("Add new code = " + concept.getCode());
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

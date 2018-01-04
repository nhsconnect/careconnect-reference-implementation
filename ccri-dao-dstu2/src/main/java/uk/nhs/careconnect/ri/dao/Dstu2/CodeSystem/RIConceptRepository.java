package uk.nhs.careconnect.ri.dao.Dstu2.CodeSystem;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptDesignation;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptParentChildLink;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
public class RIConceptRepository implements ConceptRepository {

    @PersistenceContext
    EntityManager em;

    private EntityManager sessionEntityManager;

    @Autowired
    private PlatformTransactionManager myTransactionMgr;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    CodeSystemRepository codeSystemRepository;

    private static final Logger log = LoggerFactory.getLogger(RIConceptRepository.class);


    public Session getSession(){
        sessionEntityManager = em.getEntityManagerFactory().createEntityManager();

        Session session = (Session) sessionEntityManager.unwrap(Session.class);
        return session;
    }


    @Override
    public Transaction getTransaction(Session session) {

        return session.beginTransaction();
    }

    public void beginTransaction(Transaction tx) {
       // tx.begin();
    }

    public void commitTransaction(Transaction tx) {
      tx.commit();
    }



    @Override

    public void save(ConceptParentChildLink conceptParentChildLink) {
        sessionEntityManager.persist(conceptParentChildLink);
        sessionEntityManager.flush();
        /*
        TransactionTemplate tt = new TransactionTemplate(myTransactionMgr);
        tt.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                em.persist(object);
                em.flush();
                log.info("Saved ConceptParentChildLink.Id = "+object.getId());
            }

        });
        */
    }

    @Override

    public ConceptEntity save(ConceptEntity conceptEntity){
        sessionEntityManager.persist(conceptEntity);
        for (ConceptParentChildLink child: conceptEntity.getChildren()) {
            child.setParent(conceptEntity);
        }
        sessionEntityManager.flush();
        return conceptEntity;
    }

    @Override
    public ConceptDesignation save(ConceptDesignation conceptDesignation){
        sessionEntityManager.persist(conceptDesignation);
        sessionEntityManager.flush();
        return conceptDesignation;
    }

    @Override
    public void persistLinks(ConceptEntity conceptEntity) {

        for (ConceptParentChildLink childLink : conceptEntity.getChildren()) {
            if (childLink.getId() == null) {
                childLink.setCodeSystem(childLink.getChild().getCodeSystem());

                save(childLink);
            }
        }
        for (ConceptParentChildLink childLink : conceptEntity.getChildren()) {
            persistLinks(childLink.getChild());
        }
    }

    @Override
    @Transactional
    public void storeNewCodeSystemVersion(CodeSystemEntity theCodeSystem, RequestDetails theRequestDetails) {



        log.info("Starting Code Processing CodeSystem.id = "+theCodeSystem.getId());
        log.info("Adding Concepts - Number of Concepts CodeSystem.id = "+theCodeSystem.getConcepts().size());
        for (ConceptEntity conceptEntity : theCodeSystem.getConcepts()) {
            save(conceptEntity);
            saveChildConcepts(conceptEntity);
        }
        for (ConceptEntity conceptEntity : theCodeSystem.getConcepts()) {
            persistLinks(conceptEntity);
        }
        log.info("Finished Code Processing");
    }

    private void saveChildConcepts(ConceptEntity conceptEntity) {
        for (ConceptParentChildLink childLink : conceptEntity.getChildren()) {
            save(childLink.getChild());
            saveChildConcepts(childLink.getChild());
        }
    }



    @Override
    public CodeSystemEntity findBySystem(String system) {

        CriteriaBuilder builder = sessionEntityManager.getCriteriaBuilder();

        CodeSystemEntity codeSystemEntity = null;
        CriteriaQuery<CodeSystemEntity> criteria = builder.createQuery(CodeSystemEntity.class);

        Root<CodeSystemEntity> root = criteria.from(CodeSystemEntity.class);
        List<Predicate> predList = new LinkedList<Predicate>();

        Predicate p = builder.equal(root.<String>get("codeSystemUri"),system);
        predList.add(p);
        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            log.debug("Found CodeSystem "+system);
            criteria.select(root).where(predArray);

            List<CodeSystemEntity> qryResults = sessionEntityManager.createQuery(criteria).getResultList();

            for (CodeSystemEntity cme : qryResults) {
                codeSystemEntity = cme;
                break;
            }
        }
        if (codeSystemEntity == null) {
            log.info("Not found adding CodeSystem = "+system);
            codeSystemEntity = new CodeSystemEntity();
            codeSystemEntity.setCodeSystemUri(system);

            sessionEntityManager.persist(codeSystemEntity);

        }
        return codeSystemEntity;
    }

    @Override
    public ConceptEntity findCode(CodeSystemEntity codeSystem, String code) {


        ConceptEntity conceptEntity = null;
        CriteriaBuilder builder = sessionEntityManager.getCriteriaBuilder();

        CriteriaQuery<ConceptEntity> criteria = builder.createQuery(ConceptEntity.class);
        Root<ConceptEntity> root = criteria.from(ConceptEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<ConceptEntity> results = new ArrayList<ConceptEntity>();


        log.debug("Looking for code ="+code+" in "+codeSystem.getCodeSystemUri());
        Predicate pcode = builder.equal(root.get("code"), code);
        predList.add(pcode);

        Predicate psystem = builder.equal(root.get("codeSystemEntity"), codeSystem.getId());
        predList.add(psystem);

        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);

        criteria.select(root).where(predArray);

        TypedQuery<ConceptEntity> qry = sessionEntityManager.createQuery(criteria);

        List<ConceptEntity> qryResults = qry.getResultList();

        for (ConceptEntity concept : qryResults) {
            conceptEntity = concept;
            log.debug("Found for code="+code+" ConceptEntity.Id="+conceptEntity.getId());
            break;
        }

        return conceptEntity;
    }

    @Override
    public ConceptEntity findCode(String codeSystemUri, String code) {


        ConceptEntity conceptEntity = null;
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ConceptEntity> criteria = builder.createQuery(ConceptEntity.class);
        Root<ConceptEntity> root = criteria.from(ConceptEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<ConceptEntity> results = new ArrayList<ConceptEntity>();
        Join<ConceptEntity,CodeSystemRepository> join = root.join("codeSystemEntity");

        log.debug("Looking for code ="+code+" in "+codeSystemUri);
        Predicate pcode = builder.equal(root.get("code"), code);
        predList.add(pcode);

        Predicate psystem = builder.equal(join.get("codeSystemUri"), codeSystemUri);
        predList.add(psystem);

        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);

        criteria.select(root).where(predArray);

        TypedQuery<ConceptEntity> qry = em.createQuery(criteria);
        qry.setHint("javax.persistence.cache.storeMode", "REFRESH");
        List<ConceptEntity> qryResults = qry.getResultList();

        for (ConceptEntity concept : qryResults) {
            conceptEntity = concept;
            log.debug("Found for code="+code+" ConceptEntity.Id="+conceptEntity.getId());
            break;
        }

        return conceptEntity;
    }
}

package uk.nhs.careconnect.ri.dao.CodeSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

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

    @Autowired
    private PlatformTransactionManager myTransactionMgr;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    CodeSystemRepository codeSystemRepository;

    private static final Logger log = LoggerFactory.getLogger(RIConceptRepository.class);




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
    public ConceptEntity save(ConceptEntity conceptEntity){
        emPersist(conceptEntity);

        return conceptEntity;
    }
    @Override
    public ConceptEntity addCode(String code, String display, CodeSystemEntity codeSystemEntity)
    {
        log.debug("Add new code =" + code);
        ConceptEntity conceptEntity = new ConceptEntity()
                .setCode(code)
                .setCodeSystem(codeSystemEntity)
                .setDisplay(display);
       emPersist(conceptEntity);

        return conceptEntity;
    }

    @Override
    public ConceptEntity findCode(CodeSystemEntity codeSystem, String code) {


        ConceptEntity conceptEntity = null;
        CriteriaBuilder builder = em.getCriteriaBuilder();

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

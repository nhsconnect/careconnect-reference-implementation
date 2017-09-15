package uk.nhs.careconnect.ri.dao.CodeSystem;

import org.hl7.fhir.instance.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.dao.ValueSet.RIValueSetRepository;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptParentChildLink;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class RICodeSystemRepository implements CodeSystemRepository {

    @PersistenceContext
    EntityManager em;


    private static final Logger log = LoggerFactory.getLogger(RIValueSetRepository.class);

    @Override
    public CodeSystemEntity findBySystem(String system) {

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CodeSystemEntity codeSystemEntity = null;
        CriteriaQuery<CodeSystemEntity> criteria = builder.createQuery(CodeSystemEntity.class);
        Root<CodeSystemEntity> root = criteria.from(CodeSystemEntity.class);
        List<Predicate> predList = new LinkedList<Predicate>();
        log.info("Looking for CodeSystem = " + system);
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
            em.persist(codeSystemEntity);
        }
        return codeSystemEntity;
    }

    @Override
    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ValueSet.ConceptDefinitionComponent concept) {
        // This inspects codes already present and if not found it adds the code... CRUDE at present


        ConceptEntity conceptEntity = null;
        for (ConceptEntity codeSystemConcept : codeSystemEntity.getContents()) {
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
        }
        // call child code
        if (concept.getConcept().size() > 0) {
            processChildConcepts(concept,conceptEntity);
        }
        return conceptEntity;
    }

    private void processChildConcepts(ValueSet.ConceptDefinitionComponent concept, ConceptEntity parentConcept) {
        for (ValueSet.ConceptDefinitionComponent conceptChild : concept.getConcept()) {
            ConceptParentChildLink childLink = null;

            if (conceptChild.getCode() != null) {
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
                    // }
                    // if (!childLink.getChild().getCode().equals(conceptChild.getCode())) {

                    ConceptEntity childConcept = findAddCode(parentConcept.getCodeSystem(), conceptChild);

                    /*ConceptEntity childConcept = new ConceptEntity();
                    childConcept.setCodeSystem(parentConcept.getCodeSystem());
                    childConcept.setCode(conceptChild.getCode());
                    childConcept.setDisplay(conceptChild.getDisplay());

                    em.persist(childConcept);
                     */
                    childLink.setChild(childConcept);
                    em.persist(childLink);

                    /* recursion on child nodes. Now done by recursion call
                    if (concept.getConcept().size() > 0) {
                        processChildConcepts(conceptChild,childConcept);
                    }
                    */
                }
            }
        }
    }
}

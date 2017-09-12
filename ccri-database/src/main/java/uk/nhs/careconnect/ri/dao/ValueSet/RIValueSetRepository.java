package uk.nhs.careconnect.ri.dao.ValueSet;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptParentChildLink;
import uk.nhs.careconnect.ri.entity.Terminology.ValueSetEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class RIValueSetRepository implements ValueSetRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private ValueSetEntityToFHIRValueSetTransformer valuesetEntityToFHIRValuesetTransformer;

    private static final Logger log = LoggerFactory.getLogger(RIValueSetRepository.class);


    public void save(ValueSetEntity valueset)
    {
        em.persist(valueset);
    }

    public boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    @Transactional
    @Override
    public ValueSet create(ValueSet valueSet) {
        ValueSetEntity valueSetEntity = null;

        if (valueSet.hasId()) {
            valueSetEntity = findValueSetEntity(valueSet.getIdElement());
        }

        if (valueSetEntity == null)
        {
            valueSetEntity = new ValueSetEntity();
        }

        if (valueSet.hasCodeSystem())
        {
            CriteriaBuilder builder = em.getCriteriaBuilder();

            CodeSystemEntity codeSystemEntity = null;

            CriteriaQuery<CodeSystemEntity> criteria = builder.createQuery(CodeSystemEntity.class);
            Root<CodeSystemEntity> root = criteria.from(CodeSystemEntity.class);
            List<Predicate> predList = new LinkedList<Predicate>();
            log.info("Looking for CodeSystem = " + valueSet.getCodeSystem().getSystem());
            Predicate p = builder.equal(root.<String>get("codeSystemUri"),valueSet.getCodeSystem().getSystem());
            predList.add(p);
            Predicate[] predArray = new Predicate[predList.size()];
            predList.toArray(predArray);
            if (predList.size()>0)
            {
                log.info("Found CodeSystem");
                criteria.select(root).where(predArray);
                List<CodeSystemEntity> qryResults = em.createQuery(criteria).getResultList();

                for (CodeSystemEntity cme : qryResults) {
                    codeSystemEntity = cme;
                    break;
                }
            }
            if (codeSystemEntity == null) {
                log.info("Not found adding CodeSystem = "+valueSet.getCodeSystem().getSystem());
                codeSystemEntity = new CodeSystemEntity();
                codeSystemEntity.setCodeSystemUri(valueSet.getCodeSystem().getSystem());
                em.persist(codeSystemEntity);
            }

            // This inspects codes already present and if not found it adds the code... CRUDE at present
            for (ValueSet.ConceptDefinitionComponent concept : valueSet.getCodeSystem().getConcept()) {

                ConceptEntity newConcept = null;
                for (ConceptEntity codeSystemConcept : codeSystemEntity.getContents()) {
                    if (codeSystemConcept.getCode().equals(concept.getCode())) {

                        newConcept =codeSystemConcept;
                    }

                }
                if (newConcept == null) {
                    log.info("Add new code = " + concept.getCode());
                    newConcept = new ConceptEntity();
                    newConcept.setCodeSystem(codeSystemEntity);
                    newConcept.setCode(concept.getCode());
                    newConcept.setDisplay(concept.getDisplay());

                    newConcept.setAbstractCode(concept.getAbstract());


                    em.persist(newConcept);
                }
                // call child code
                if (concept.getConcept().size() > 0) {
                    processChildConcepts(concept,newConcept);
                }

            }


            valueSetEntity.setCodeSystem(codeSystemEntity);

        }


        if (valueSet.hasId())
        {
            valueSetEntity.setStrId(valueSet.getId());
        }
        if (valueSet.hasUrl())
        {
            valueSetEntity.setUrl(valueSet.getUrl());
        }
        if (valueSet.hasName())
        {
            valueSetEntity.setName(valueSet.getName());
        }
        if (valueSet.hasStatus())
        {
            valueSetEntity.setStatus(valueSet.getStatus());
        }
        if (valueSet.hasDescription())
        {
            valueSetEntity.setDescription(valueSet.getDescription());
        }

        log.info("Call em.persist ValueSetEntity");
        em.persist(valueSetEntity);


        log.info("Called PERSIST id="+valueSetEntity.getId().toString());
        valueSet.setId(valueSetEntity.getId().toString());

        return valueSet;
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
                    ConceptEntity childConcept = new ConceptEntity();
                    childConcept.setCodeSystem(parentConcept.getCodeSystem());
                    childConcept.setCode(conceptChild.getCode());
                    childConcept.setDisplay(conceptChild.getDisplay());

                    em.persist(childConcept);
                    childLink.setChild(childConcept);
                    em.persist(childLink);

                    // recursion on child nodes.
                    if (concept.getConcept().size() > 0) {
                        processChildConcepts(conceptChild,childConcept);
                    }
                }
            }
        }
    }
    private ValueSetEntity findValueSetEntity(IdType theId) {

        ValueSetEntity valueSetEntity = null;
        // Only look up if the id is numeric else need to do a search
        if (isNumeric(theId.getValue())) {
            valueSetEntity =(ValueSetEntity) em.find(ValueSetEntity.class, theId.getValue());
        }

        // if null try a search on strId
        if (valueSetEntity == null)
        {
            CriteriaBuilder builder = em.getCriteriaBuilder();

            CriteriaQuery<ValueSetEntity> criteria = builder.createQuery(ValueSetEntity.class);
            Root<ValueSetEntity> root = criteria.from(ValueSetEntity.class);
            List<Predicate> predList = new LinkedList<Predicate>();
            Predicate p = builder.equal(root.<String>get("strId"),theId.getValue());
            predList.add(p);
            Predicate[] predArray = new Predicate[predList.size()];
            predList.toArray(predArray);
            if (predList.size()>0)
            {
                criteria.select(root).where(predArray);

                List<ValueSetEntity> qryResults = em.createQuery(criteria).getResultList();

                for (ValueSetEntity cme : qryResults)
                {
                    valueSetEntity = cme;
                    break;
                }
            }
        }
        return valueSetEntity;
    }


    public ValueSet read(IdType theId) {

        ValueSetEntity valueSetEntity = findValueSetEntity(theId);

        return valueSetEntity == null
                ? null
                : valuesetEntityToFHIRValuesetTransformer.transform(valueSetEntity);

    }
    public List<ValueSet> searchValueset (
            @OptionalParam(name = ValueSet.SP_IDENTIFIER) TokenParam identifier
    )
    {
        List<ValueSetEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ValueSetEntity> criteria = builder.createQuery(ValueSetEntity.class);
        Root<ValueSetEntity> root = criteria.from(ValueSetEntity.class);
       

        List<Predicate> predList = new LinkedList<Predicate>();
        List<ValueSet> results = new ArrayList<ValueSet>();

        if (identifier !=null)
        {
           /* TODO Join<ValueSetEntity, ValueSetIdentifier> join = root.join("identifiers", JoinType.LEFT);
            
            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            */
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

        qryResults = em.createQuery(criteria).getResultList();

        for (ValueSetEntity valuesetEntity : qryResults)
        {
            ValueSet valueset = valuesetEntityToFHIRValuesetTransformer.transform(valuesetEntity);
            results.add(valueset);
        }

        return results;
    }


}

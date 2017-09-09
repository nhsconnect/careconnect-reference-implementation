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

    public ValueSet create(ValueSet valueSet) {
        ValueSetEntity valueSetEntity = null;
        em.getTransaction().begin();

        if (valueSet.hasId()) {
    // Only look up if the id is numeric else need to do a search
            if (isNumeric(valueSet.getId())) {
                valueSetEntity = (ValueSetEntity) em.find(ValueSetEntity.class, valueSet.getId());
            }
            // if null try a search on strId
            if (valueSetEntity == null)
            {
                CriteriaBuilder builder = em.getCriteriaBuilder();

                CriteriaQuery<ValueSetEntity> criteria = builder.createQuery(ValueSetEntity.class);
                Root<ValueSetEntity> root = criteria.from(ValueSetEntity.class);
                List<Predicate> predList = new LinkedList<Predicate>();
                Predicate p = builder.equal(root.<String>get("strId"),valueSet.getId());
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
                Boolean found = false;
                for (ConceptEntity codeSystemConcept : codeSystemEntity.getContents()) {
                    if (codeSystemConcept.getCode().equals(concept.getCode())) {
                        found = true;
                    }

                }
                if (!found) {
                    log.info("Add new code = " + concept.getCode());
                    ConceptEntity newConcept = new ConceptEntity();
                    newConcept.setCodeSystem(codeSystemEntity);
                    newConcept.setCode(concept.getCode());
                    newConcept.setDisplay(concept.getDisplay());

                    em.persist(newConcept);
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


        em.getTransaction().commit();

        log.info("Called PERSIST id="+valueSetEntity.getId().toString());
        valueSet.setId(valueSetEntity.getId().toString());

        em.close();
        return valueSet;
    }


    public ValueSet read(IdType theId) {

        ValueSetEntity valuesetEntity = (ValueSetEntity) em.find(ValueSetEntity.class,Integer.parseInt(theId.getIdPart()));

        return valuesetEntity == null
                ? null
                : valuesetEntityToFHIRValuesetTransformer.transform(valuesetEntity);

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
           // log.trace("HAPI Custom = "+doc.getId());
            ValueSet valueset = valuesetEntityToFHIRValuesetTransformer.transform(valuesetEntity);
            results.add(valueset);
        }

        return results;
    }


}

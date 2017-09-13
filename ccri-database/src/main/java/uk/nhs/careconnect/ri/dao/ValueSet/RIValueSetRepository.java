package uk.nhs.careconnect.ri.dao.ValueSet;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.aspectj.apache.bcel.classfile.Code;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.dao.CodeSystem.CodeSystemRepository;
import uk.nhs.careconnect.ri.entity.Terminology.*;

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

    @Autowired
    CodeSystemRepository codeSystemRepository;

    public void save(ValueSetEntity valueset)
    {
        em.persist(valueset);
    }

    public boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    ValueSet valueSet;

    @Transactional
    @Override
    public ValueSet create(ValueSet valueSet) {
        this.valueSet = valueSet;

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


            CodeSystemEntity codeSystemEntity = codeSystemRepository.findBySystem(valueSet.getCodeSystem().getSystem());

            //// add me

            for (ValueSet.ConceptDefinitionComponent concept : valueSet.getCodeSystem().getConcept()) {
                codeSystemRepository.findAddCode(codeSystemEntity,concept);
            }


            valueSetEntity.setCodeSystem(codeSystemEntity);

        }
        if (valueSet.hasCompose()) {
            for (ValueSet.ConceptSetComponent component :valueSet.getCompose().getInclude()) {

                CodeSystemEntity codeSystemEntity = codeSystemRepository.findBySystem(component.getSystem());

                ValueSetInclude includeValueSetEntity = null;

                for (ValueSetInclude include : valueSetEntity.getIncludes()) {
                    if (include.getSystem().equals(component.getSystem())) {
                        includeValueSetEntity = include;
                    }
                }
                if (includeValueSetEntity == null) {
                    includeValueSetEntity = new ValueSetInclude();
                    includeValueSetEntity.setCodeSystem(codeSystemEntity);
                    includeValueSetEntity.setValueSetEntity(valueSetEntity);
                    em.persist(includeValueSetEntity);
                    valueSetEntity.getIncludes().add(includeValueSetEntity);
                }
                for (ValueSet.ConceptReferenceComponent conceptReferenceComponent : component.getConcept()) {
                    ConceptEntity conceptEntity = null;
                    for (ConceptEntity conceptSearch :includeValueSetEntity.getConcepts()) {
                        if (conceptSearch.getCode().equals(conceptReferenceComponent.getCode())) {
                            conceptEntity = conceptSearch;
                        }
                    }
                    if (conceptEntity == null) {
                        // Code may already be in the code System but not in the ValueSet. So we need to search the CodeSystem
                        ValueSet.ConceptDefinitionComponent concept = new ValueSet.ConceptDefinitionComponent();
                        // Only supporting simple list
                        concept.setCode(conceptReferenceComponent.getCode())
                                .setDisplay(conceptReferenceComponent.getDisplay());
                        // This is not the ideal way to add a Concept but works for the RI
                        conceptEntity = codeSystemRepository.findAddCode(codeSystemEntity,concept);
                        includeValueSetEntity.getConcepts().add(conceptEntity);
                    }
                }
                em.persist(includeValueSetEntity);
            }
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

        log.info("Retrieving ValueSet = " + theId.getValue());

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

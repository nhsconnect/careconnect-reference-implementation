package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;
import uk.nhs.careconnect.ri.dao.transforms.ValueSetEntityToFHIRValueSetTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.*;
import uk.nhs.careconnect.ri.database.entity.Terminology.*;

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
public class ValueSetDao implements ValueSetRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private ValueSetEntityToFHIRValueSetTransformer valuesetEntityToFHIRValuesetTransformer;

    private static final Logger log = LoggerFactory.getLogger(ValueSetDao.class);

    @Autowired
    CodeSystemRepository codeSystemRepository;

    public void save(ValueSetEntity valueset)
    {
        em.persist(valueset);
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

        /*
        Not supported in STU3

        if (valueSet. hasCodeSystem())
        {
            CodeSystemEntity codeSystemEntity = codeSystemRepository.findBySystem(valueSet.getCodeSystem().getSystem());

            //// add me

            for (ValueSet.ConceptDefinitionComponent concept : valueSet.getCodeSystem().getConcept()) {
                codeSystemRepository.findAddCode(codeSystemEntity,concept);
            }
            valueSetEntity.setCodeSystem(codeSystemEntity);
        }
        */

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

        log.trace("Call em.persist ValueSetEntity");
        em.persist(valueSetEntity);

        //Created the ValueSet so add the sub concepts

        log.trace("ValueSet = "+valueSet.getUrl());
        if (valueSet.hasCompose()) {
            for (ValueSet.ConceptSetComponent component :valueSet.getCompose().getInclude()) {

                CodeSystemEntity codeSystemEntity = codeSystemRepository.findBySystem(component.getSystem());
                log.trace("CodeSystem Id = "+ codeSystemEntity.getId()+ " Uri = " + codeSystemEntity.getCodeSystemUri());

                ValueSetInclude includeValueSetEntity = null;

                // Search for existing entries
                for (ValueSetInclude include : valueSetEntity.getIncludes()) {
                    if (include.getSystem().equals(component.getSystem())) {
                        includeValueSetEntity = include;
                    }
                }
                if (includeValueSetEntity == null) {
                    includeValueSetEntity = new ValueSetInclude();
                    includeValueSetEntity.setCodeSystem(codeSystemEntity);
                    includeValueSetEntity.setValueSetEntity(valueSetEntity);
                   // valueSetEntity.getIncludes().add(includeValueSetEntity);
                    em.persist(includeValueSetEntity);
                    valueSetEntity.getIncludes().add(includeValueSetEntity);
                }
                log.trace("ValueSet include Id ="+includeValueSetEntity.getId());

                for (ValueSet.ConceptSetFilterComponent filter : component.getFilter()) {
                    ValueSetIncludeFilter filterEntity = null;
                    for (ValueSetIncludeFilter filterSearch : includeValueSetEntity.getFilters()) {
                        if (filterSearch.getValue().getCode().equals(filter.getValue())) {
                            filterEntity = filterSearch;
                        }
                    }
                    if (filterEntity == null) {
                        ValueSet.ConceptReferenceComponent concept = new ValueSet.ConceptReferenceComponent ();
                        concept.setCode(filter.getValue());

                        filterEntity = new ValueSetIncludeFilter();
                        filterEntity.setValue(codeSystemRepository.findAddCode(codeSystemEntity,concept));
                        filterEntity.setOperator(filter.getOp());
                        filterEntity.setInclude(includeValueSetEntity);

                        em.persist(filterEntity);
                        includeValueSetEntity.getFilters().add(filterEntity);
                    }
                }
                for (ValueSet.ConceptReferenceComponent conceptReferenceComponent : component.getConcept()) {
                    ValueSetIncludeConcept includeConcept = null;
                    for (ValueSetIncludeConcept conceptSearch :includeValueSetEntity.getConcepts()) {
                        if (conceptSearch.getConcept().getCode().equals(conceptReferenceComponent.getCode())) {
                            includeConcept = conceptSearch;
                            log.trace("Found Concept in include = "+ conceptSearch.getConcept().getCode());
                        }
                    }
                    if (includeConcept == null) {
                        // Code may already be in the code System but not in the ValueSet. So we need to search the CodeSystem
                        log.debug("NOT Found concept in include = "+conceptReferenceComponent.getCode());
                        ValueSet.ConceptReferenceComponent concept = new ValueSet.ConceptReferenceComponent();
                        concept.setCode(conceptReferenceComponent.getCode())
                                .setDisplay(conceptReferenceComponent.getDisplay());

                        // This is not the ideal way to add a Concept but works for the RI
                        includeConcept = new ValueSetIncludeConcept();
                        //ConceptEntity conceptEntity
                        includeConcept.setConcept(codeSystemRepository.findAddCode(codeSystemEntity,concept));
                        log.trace("codeSystem returned "+includeConcept.getConcept().getCode()+" Id = "+includeConcept.getConcept().getId());
                        includeConcept.setInclude(includeValueSetEntity);

                        em.persist(includeConcept);
                        // Need to ensure the value is availabe for next search 
                        includeValueSetEntity.getConcepts().add(includeConcept);
                    }
                }
            }
        }



        log.debug("Called PERSIST id="+valueSetEntity.getId().toString());
        valueSet.setId(valueSetEntity.getId().toString());

        return valueSet;
    }




    private ValueSetEntity findValueSetEntity(IdType theId) {

        ValueSetEntity valueSetEntity = null;
        // Only look up if the id is numeric else need to do a search
        if (daoutils.isNumeric(theId.getValue())) {
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

        log.trace("Retrieving ValueSet = " + theId.getValue());

        ValueSetEntity valueSetEntity = findValueSetEntity(theId);

        return valueSetEntity == null
                ? null
                : valuesetEntityToFHIRValuesetTransformer.transform(valueSetEntity);

    }
    public List<ValueSet> searchValueset (
            @OptionalParam(name = ValueSet.SP_NAME) StringParam name
    )
    {
        List<ValueSetEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ValueSetEntity> criteria = builder.createQuery(ValueSetEntity.class);
        Root<ValueSetEntity> root = criteria.from(ValueSetEntity.class);
       

        List<Predicate> predList = new LinkedList<Predicate>();
        List<ValueSet> results = new ArrayList<ValueSet>();

        if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal(name.getValue()+"%"))
                    );

            predList.add(p);
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

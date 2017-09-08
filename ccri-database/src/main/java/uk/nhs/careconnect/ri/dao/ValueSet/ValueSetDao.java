package uk.nhs.careconnect.ri.dao.ValueSet;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.Terminology.ValueSetEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
public class ValueSetDao {

    @Autowired
    EntityManagerFactory entityManagerFactory;


    @Autowired
    private ValueSetEntityToFHIRValueSetTransformer valuesetEntityToFHIRValuesetTransformer;

    public void save(ValueSetEntity valueset)
    {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.persist(valueset);
    }

    public ValueSet create(ValueSet valueSet) {
        ValueSetEntity valueSetEntity = null;
        EntityManager em = entityManagerFactory.createEntityManager();

        if (valueSet.hasId())
        {
            valueSetEntity = (ValueSetEntity) em.find(ValueSetEntity.class,valueSet.getId());
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

        //log.info("Call em.persist ValueSetEntity");
        em.persist(valueSetEntity);

        return valueSet;
    }


    public ValueSet read(IdType theId) {
        EntityManager em = entityManagerFactory.createEntityManager();

        ValueSetEntity valuesetEntity = (ValueSetEntity) em.find(ValueSetEntity.class,Integer.parseInt(theId.getIdPart()));

        return valuesetEntity == null
                ? null
                : valuesetEntityToFHIRValuesetTransformer.transform(valuesetEntity);

    }
    public List<ValueSet> searchValueset (
            @OptionalParam(name = ValueSet.SP_IDENTIFIER) TokenParam identifier
    )
    {
        EntityManager em = entityManagerFactory.createEntityManager();
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

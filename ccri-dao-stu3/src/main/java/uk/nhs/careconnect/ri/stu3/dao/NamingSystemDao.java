package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.NamingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.database.daointerface.NamingSystemRepository;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemEntity;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemUniqueId;
import uk.nhs.careconnect.ri.stu3.dao.transforms.NamingSystemEntityToFHIRNamingSystemTransformer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class NamingSystemDao implements NamingSystemRepository {

    @PersistenceContext
    EntityManager em;


    @Autowired
    private NamingSystemEntityToFHIRNamingSystemTransformer namingSystemEntityToFHIRValuesetTransformer;

    private static final Logger log = LoggerFactory.getLogger(NamingSystemDao.class);


    public void save(FhirContext ctx, NamingSystemEntity namingSystem)
    {
        em.persist(namingSystem);
    }


    NamingSystem namingSystem;

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(NamingSystemEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Transactional
    @Override
    public NamingSystem create(FhirContext ctx, NamingSystem namingSystem) {
        this.namingSystem = namingSystem;

        NamingSystemEntity namingSystemEntity = null;



        if (namingSystem.hasId()) {
            namingSystemEntity = readEntity(ctx,namingSystem.getIdElement());
        }

           /*

        Need to check that the incoming id's don't already exist. If they do, we should only proceed if the id
        the incoming resource is the same.

         */

       for (NamingSystem.NamingSystemUniqueIdComponent component: namingSystem.getUniqueId()) {
           List<NamingSystemEntity> entries = searchEntity(ctx, null, null, new TokenParam().setValue(component.getValue()));
           for (NamingSystemEntity nameSys : entries) {
               if (namingSystem.getId() == null) {
                   throw new ResourceVersionConflictException("NamingSystem Unique identifier "+component.getValue()+ " is already present on the system "+ nameSys.getId());
               }

               if (!nameSys.getId().equals(namingSystemEntity.getId())) {
                   throw new ResourceVersionConflictException("NamingSystem Unique identifier "+component.getValue()+ " is already present on the system "+ nameSys.getId());
               }
           }
       }


        if (namingSystemEntity == null)
        {
            namingSystemEntity = new NamingSystemEntity();
        }

        namingSystemEntity.setResource(null);


        if (namingSystem.hasName())
        {
            namingSystemEntity.setName(namingSystem.getName());
        }

        if (namingSystem.hasStatus())
        {
            namingSystemEntity.setStatus(namingSystem.getStatus());
        }
        if (namingSystem.hasKind())
        {
            namingSystemEntity.setKind(namingSystem.getKind());
        }

        if (namingSystem.hasDate()) {
            namingSystemEntity.setChangedDate(namingSystem.getDate());
        }



        if (namingSystem.hasReplacedBy()) {
            NamingSystemEntity replacedBy = readEntity(ctx, new IdType(namingSystem.getReplacedBy().getReference()));
            namingSystemEntity.setReplacedBy(replacedBy);
        }

        String resource = ctx.newJsonParser().encodeResourceToString(namingSystem);
        namingSystemEntity.setResource(resource);


        log.trace("Call em.persist NamingSystemEntity");
        em.persist(namingSystemEntity);

        //Created the NamingSystem so add the sub concepts

        for (NamingSystemUniqueId id : namingSystemEntity.getNamingSystemUniqueIds()) {
            em.remove(id);
        }

        namingSystemEntity.setNamingSystemUniqueIds(new ArrayList<>());
        log.trace("NamingSystem = "+namingSystem.getUrl());
        if (namingSystem.hasUniqueId()) {
            for (NamingSystem.NamingSystemUniqueIdComponent component :namingSystem.getUniqueId()) {

                NamingSystemUniqueId namingSystemUniqueId = new NamingSystemUniqueId();
                namingSystemUniqueId.setNamingSystem(namingSystemEntity);

                if (component.hasType()) {
                    namingSystemUniqueId.setIdentifierType(component.getType());
                }
                if (component.hasValue()) {
                    namingSystemUniqueId.setValue(component.getValue());
                }
                if (component.hasPreferred()) {
                    namingSystemUniqueId.setPreferred(component.getPreferred());
                }
                if (component.hasComment()) {
                    namingSystemUniqueId.setComment(component.getComment());
                }
                if (component.hasPeriod()) {
                    if (component.getPeriod().hasStart()) {
                        namingSystemUniqueId.setPeriodStart(component.getPeriod().getStart());
                    }
                    if (component.getPeriod().hasEnd()) {
                        namingSystemUniqueId.setPeriodEnd(component.getPeriod().getEnd());
                    }
                }
                namingSystemEntity.getNamingSystemUniqueIds().add(namingSystemUniqueId);
                em.persist(namingSystemUniqueId);
            }
        }


        log.debug("Called PERSIST id="+namingSystemEntity.getId().toString());
        namingSystem.setId(namingSystemEntity.getId().toString());

        return namingSystem;
    }




    public NamingSystemEntity readEntity(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            return em.find(NamingSystemEntity.class, Long.parseLong(theId.getIdPart()));
        } else {
            return null;
        }
    }


    @Override
    public NamingSystem read(FhirContext ctx, IdType theId) {

        log.trace("Retrieving NamingSystem = " + theId.getValue());
        NamingSystemEntity namingSystemEntity;

        if (daoutils.isNumeric(theId.getIdPart())) {
            namingSystemEntity = em.find(NamingSystemEntity.class, Long.parseLong(theId.getIdPart()));
        } else {
            return null;
        }

        return namingSystemEntity == null
                ? null
                : namingSystemEntityToFHIRValuesetTransformer.transform(namingSystemEntity, ctx);

    }

    @Override
    public List<NamingSystem> search (FhirContext ctx,
                                      @OptionalParam(name = NamingSystem.SP_NAME) StringParam name,
                                      @OptionalParam(name = NamingSystem.SP_PUBLISHER) StringParam publisher,
                                      @OptionalParam(name = NamingSystem.SP_VALUE) TokenParam unique
    ) {

        List<NamingSystemEntity> qryResults = searchEntity(ctx, name,publisher,unique);
        List<NamingSystem> results = new ArrayList<>();

        for (NamingSystemEntity namingSystemEntity : qryResults)
        {
            if (namingSystemEntity.getResource() != null) {
                results.add(namingSystemEntityToFHIRValuesetTransformer.transform(namingSystemEntity, ctx));
            } else {

                NamingSystem namingSystem = namingSystemEntityToFHIRValuesetTransformer.transform(namingSystemEntity, ctx);
                String resource = ctx.newJsonParser().encodeResourceToString(namingSystem);
                if (resource.length() < 10000) {
                    namingSystemEntity.setResource(resource);
                    em.persist(namingSystemEntity);
                }
                results.add(namingSystem);
            }
        }
        return results;
    }

    @Override
    public List<NamingSystemEntity> searchEntity(FhirContext ctx, StringParam name, StringParam publisher, TokenParam unique) {


        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<NamingSystemEntity> criteria = builder.createQuery(NamingSystemEntity.class);
        Root<NamingSystemEntity> root = criteria.from(NamingSystemEntity.class);
       

        List<Predicate> predList = new LinkedList<Predicate>();


        if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal("%" + name.getValue() + "%"))
                    );

            predList.add(p);
        }
        if (publisher !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("publisher").as(String.class)),
                            builder.upper(builder.literal( publisher.getValue() + "%"))
                    );

            predList.add(p);
        }
        if (unique !=null)
        {

            Join<NamingSystem, NamingSystemUniqueId> join = root.join("namingSystemUniqueIds", JoinType.LEFT);
            Predicate p =
                    builder.equal(join.get("value"),unique.getValue());

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

        return em.createQuery(criteria).setMaxResults(100).getResultList();


    }


}

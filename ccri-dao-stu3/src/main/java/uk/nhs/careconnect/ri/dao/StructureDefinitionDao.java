package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;

import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.transforms.StructureDefinitionEntityToFHIRStructureDefinitionTransformer;
import uk.nhs.careconnect.ri.database.daointerface.StructureDefinitionRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;
import uk.nhs.careconnect.ri.database.entity.structureDefinition.StructureDefinitionEntity;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
public class StructureDefinitionDao implements StructureDefinitionRepository{

	 @PersistenceContext
	    EntityManager em;
 
	 @Autowired
	    private StructureDefinitionEntityToFHIRStructureDefinitionTransformer structureDefinitionEntityToFHIRStructureDefinitionTransformer;
	    @Autowired
        @Lazy
ConceptRepository conceptDao;

	@Autowired
	@Lazy
	ValueSetRepository valueSetDao;

	    private static final Logger log = LoggerFactory.getLogger(StructureDefinitionDao.class);

	@Override
	public Long count() {
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		cq.select(qb.count(cq.from(StructureDefinitionEntity.class)));
		//cq.where(/*your stuff*/);
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public StructureDefinitionEntity readEntity(FhirContext ctx, IdType theId) {
		return null;
	}

	@Override
	public void save(FhirContext ctx, StructureDefinitionEntity resource) throws OperationOutcomeException {

	}

	StructureDefinition structureDefinition;
	    
	    @Transactional
	    @Override
	    public StructureDefinition create(FhirContext ctx, StructureDefinition structureDefinition) {
	    	
	    //	System.out.println("call came to save Concept MAP : " + structureDefinition.getUrlElement().getValue() );
	        this.structureDefinition = structureDefinition;
	        StructureDefinitionEntity structureDefinitionEntity = null;

	        System.out.println("id is" + structureDefinition.getIdElement());
	        long newStructureDefinitionId = -1;
	        if (structureDefinition.hasId()) {
	            structureDefinitionEntity = findStructureDefinitionEntity(structureDefinition.getIdElement());
	        }

			if (structureDefinition.hasUrl()) {
				List<StructureDefinitionEntity> entries = searchEntity(ctx, null, null, new UriParam().setValue(structureDefinition.getUrl()));
				for (StructureDefinitionEntity nameSys : entries) {
					if (structureDefinition.getId() == null) {
						throw new ResourceVersionConflictException("Url "+structureDefinition.getUrl()+ " is already present on the system "+ nameSys.getId());
					}

					if (!nameSys.getId().equals(structureDefinitionEntity.getId())) {
						throw new ResourceVersionConflictException("Url "+structureDefinition.getUrl()+ " is already present on the system "+ nameSys.getId());
					}
				}
			}
			structureDefinitionEntity.setResource(null);
	        
	     //   if(getExistingStructureDefinitionEntity(structureDefinition.getUrlElement() )==0)
	       // 	{
			        if (structureDefinitionEntity == null)
			        {
			            structureDefinitionEntity = new StructureDefinitionEntity();
			        }
			        // Removed Id
			        if (structureDefinition.hasUrl())
			        {
			        	structureDefinitionEntity.setUrl(structureDefinition.getUrl());
			        }
			        if (structureDefinition.hasName())
			        {
			        	structureDefinitionEntity.setName(structureDefinition.getName());
			        }
			        if (structureDefinition.hasStatus())
			        {
			        	structureDefinitionEntity.setStatus(structureDefinition.getStatus());
			        }
			        if (structureDefinition.hasDescription())
			        {
			        	structureDefinitionEntity.setDescription(structureDefinition.getDescription());
			        }

					if (structureDefinition.hasPublisher())
					{
						structureDefinitionEntity.setPublisher(structureDefinition.getPublisher());
					}
					if (structureDefinition.hasCopyright())
					{
						structureDefinitionEntity.setCopyright(structureDefinition.getCopyright());
					}
					if (structureDefinition.hasVersion())
					{
						structureDefinitionEntity.setVersion(structureDefinition.getVersion());
					}


			        
			        
		       	log.trace("Call em.persist StructureDefinitionEntity");
		       	em.persist(structureDefinitionEntity); // persisting Concept Maps conceptmap
		       	log.info("Called PERSIST id="+structureDefinitionEntity.getId().toString());
		        structureDefinition.setId(structureDefinitionEntity.getId().toString());
		        newStructureDefinitionId = structureDefinitionEntity.getId();
		   //   }
		 //     else
		//      {
		//       	newStructureDefinitionId = getExistingStructureDefinitionEntity(structureDefinition.getUrlElement() );
		//      }
			        
			  System.out.println("newStructureDefinitionId = " + newStructureDefinitionId);    
			  structureDefinitionEntity.setResource(null);


			  
			 // structureDefinitionElementGroupEntity.setStructureDefinitionGroup(structureDefinitionGroupEntity);
			  return structureDefinition;
	    }   
	    
	    private long getExistingStructureDefinitionEntity(UriType url)
	    {
	    	
	    	CriteriaBuilder cb = em.getCriteriaBuilder();
	    	CriteriaQuery<StructureDefinitionEntity> cq = cb.createQuery(StructureDefinitionEntity.class);
	    	
	    	Root<StructureDefinitionEntity> conceptmap = cq.from(StructureDefinitionEntity.class) ;
	    	cq.where(cb.equal(conceptmap.get("url"),url.getValue() ) );
	    	cq.select(conceptmap);
	    	TypedQuery<StructureDefinitionEntity> q = em.createQuery(cq);
	    	
	    	List<StructureDefinitionEntity> conceptmaps = q.getResultList();
	    	if(conceptmaps.size()>0) return conceptmaps.get(0).getId() ; 
	    	else return 0;
	    	
	    }
	    
	    private StructureDefinitionEntity findStructureDefinitionEntity(IdType theId) {

	    	System.out.println("the id is " + theId.getIdPart());
	    	
	    	StructureDefinitionEntity structureDefinitionEntity = null;
	        // Only look up if the id is numeric else need to do a search
	/*        if (daoutils.isNumeric(theId.getIdPart())) {
	            structureDefinitionEntity =(StructureDefinitionEntity) em.find(StructureDefinitionEntity.class, theId.getIdPart());
	        } */
	        StructureDefinitionEntity.class.getName();
	        // if null try a search on strId
	        
	            CriteriaBuilder builder =  em.getCriteriaBuilder();

			if (daoutils.isNumeric(theId.getIdPart())) {

				CriteriaQuery<StructureDefinitionEntity> criteria = builder.createQuery(StructureDefinitionEntity.class);
				Root<StructureDefinitionEntity> root = criteria.from(StructureDefinitionEntity.class);
				List<Predicate> predList = new LinkedList<Predicate>();
				Predicate p = builder.equal(root.<String>get("id"), theId.getIdPart());
				predList.add(p);
				Predicate[] predArray = new Predicate[predList.size()];
				predList.toArray(predArray);
				if (predList.size() > 0) {
					criteria.select(root).where(predArray);

					List<StructureDefinitionEntity> qryResults = em.createQuery(criteria).getResultList();

					for (StructureDefinitionEntity cme : qryResults) {
						structureDefinitionEntity = cme;
						break;
					}
				}
			}
	       // }
	        return structureDefinitionEntity;
	    }
	    
	    public StructureDefinition read(FhirContext ctx, IdType theId) {

	        log.trace("Retrieving ValueSet = " + theId.getValue());

	        StructureDefinitionEntity structureDefinitionEntity = findStructureDefinitionEntity(theId);

	        if (structureDefinitionEntity == null) return null;

	        StructureDefinition structureDefinition = structureDefinitionEntityToFHIRStructureDefinitionTransformer.transform(structureDefinitionEntity);

	        if (structureDefinitionEntity.getResource() == null) {
	            String resource = ctx.newJsonParser().encodeResourceToString(structureDefinition);
	            if (resource.length() < 10000) {
	            	structureDefinitionEntity.setResource(resource);
	                em.persist(structureDefinitionEntity);
	            }
	        }
	        return structureDefinition;
	      

	    }

	public List<StructureDefinition> search (FhirContext ctx,
										  @OptionalParam(name = StructureDefinition.SP_NAME) StringParam name,
										  @OptionalParam(name = StructureDefinition.SP_PUBLISHER) StringParam publisher,
										  @OptionalParam(name = StructureDefinition.SP_URL) UriParam url
	) {
		List<StructureDefinitionEntity> qryResults = searchEntity(ctx,name,publisher, url);
		List<StructureDefinition> results = new ArrayList<>();

		for (StructureDefinitionEntity conceptmapEntity : qryResults)
		{
			if (conceptmapEntity.getResource() != null) {
				results.add((StructureDefinition) ctx.newJsonParser().parseResource(conceptmapEntity.getResource()));
			} else {

				StructureDefinition structureDefinition = structureDefinitionEntityToFHIRStructureDefinitionTransformer.transform(conceptmapEntity);
				String resource = ctx.newJsonParser().encodeResourceToString(structureDefinition);
				if (resource.length() < 10000) {
					conceptmapEntity.setResource(resource);
					em.persist(conceptmapEntity);
				}
				results.add(structureDefinition);
			}
		}
		return results;
	}

	    public List<StructureDefinitionEntity> searchEntity (FhirContext ctx,
	            @OptionalParam(name = StructureDefinition.SP_NAME) StringParam name,
	            @OptionalParam(name = StructureDefinition.SP_PUBLISHER) StringParam publisher,
	            @OptionalParam(name = StructureDefinition.SP_URL) UriParam url
	    )
	    {
	        List<StructureDefinitionEntity> qryResults = null;

	        CriteriaBuilder builder = em.getCriteriaBuilder();

	        CriteriaQuery<StructureDefinitionEntity> criteria = builder.createQuery(StructureDefinitionEntity.class);
	        Root<StructureDefinitionEntity> root = criteria.from(StructureDefinitionEntity.class);

	        List<Predicate> predList = new LinkedList<>();


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
	        if (url !=null)
	        {

	            Predicate p =
	                    builder.like(
	                            builder.upper(root.get("url").as(String.class)),
	                            builder.upper(builder.literal( url.getValue()))
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

	        qryResults = em.createQuery(criteria).setMaxResults(100).getResultList();

	       return qryResults;
	    }
	    
	    
	    
}

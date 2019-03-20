package uk.nhs.careconnect.ri.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.Transactional;

import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.dstu3.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.dstu3.model.ConceptMap.TargetElementComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptMapRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;
import uk.nhs.careconnect.ri.database.entity.TranslationMatches;
import uk.nhs.careconnect.ri.database.entity.TranslationRequests;
import uk.nhs.careconnect.ri.database.entity.TranslationResults;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapEntity;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroup;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroupElement;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroupTarget;

import uk.nhs.careconnect.ri.dao.transforms.ConceptMapEntityToFHIRConceptMapTransformer;

@Repository
@Transactional
public class ConceptMapDao implements ConceptMapRepository{

	 @PersistenceContext
	    EntityManager em;
 
	 @Autowired
	    private ConceptMapEntityToFHIRConceptMapTransformer conceptMapEntityToFHIRConceptMapTransformer;
	    @Autowired
        @Lazy
ConceptRepository conceptDao;

	@Autowired
	@Lazy
	ValueSetRepository valueSetDao;

	    private static final Logger log = LoggerFactory.getLogger(ConceptMapDao.class);

	@Override
	public Long count() {
		CriteriaBuilder qb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		cq.select(qb.count(cq.from(ConceptMapEntity.class)));
		//cq.where(/*your stuff*/);
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public ConceptMapEntity readEntity(FhirContext ctx, IdType theId) {
		return null;
	}

	@Override
	public void save(FhirContext ctx, ConceptMapEntity resource) throws OperationOutcomeException {

	}

	ConceptMap conceptMap;
	    
	    @Transactional
	    @Override
	    public ConceptMap create(FhirContext ctx, ConceptMap conceptMap) {
	    	
	    //	System.out.println("call came to save Concept MAP : " + conceptMap.getUrlElement().getValue() );
	        this.conceptMap = conceptMap;
	        ConceptMapEntity conceptMapEntity = null;

	        System.out.println("id is" + conceptMap.getIdElement());
	        long newConceptMapId = -1;
	        if (conceptMap.hasId()) {
	            conceptMapEntity = findConceptMapEntity(conceptMap.getIdElement());
	        }

			if (conceptMap.hasUrl()) {
				List<ConceptMapEntity> entries = searchEntity(ctx, null, null, new UriParam().setValue(conceptMap.getUrl()));
				for (ConceptMapEntity nameSys : entries) {
					if (conceptMap.getId() == null) {
						throw new ResourceVersionConflictException("Url "+conceptMap.getUrl()+ " is already present on the system "+ nameSys.getId());
					}

					if (!nameSys.getId().equals(conceptMapEntity.getId())) {
						throw new ResourceVersionConflictException("Url "+conceptMap.getUrl()+ " is already present on the system "+ nameSys.getId());
					}
				}
			}
			conceptMapEntity.setResource(null);
	        
	     //   if(getExistingConceptMapEntity(conceptMap.getUrlElement() )==0)
	       // 	{
			        if (conceptMapEntity == null)
			        {
			            conceptMapEntity = new ConceptMapEntity();
			        }
			        // Removed Id
			        if (conceptMap.hasUrl())
			        {
			        	conceptMapEntity.setUrl(conceptMap.getUrl());
			        }
			        if (conceptMap.hasName())
			        {
			        	conceptMapEntity.setName(conceptMap.getName());
			        }
			        if (conceptMap.hasStatus())
			        {
			        	conceptMapEntity.setStatus(conceptMap.getStatus());
			        }
			        if (conceptMap.hasDescription())
			        {
			        	conceptMapEntity.setDescription(conceptMap.getDescription());
			        }

					if (conceptMap.hasPublisher())
					{
						conceptMapEntity.setPublisher(conceptMap.getPublisher());
					}
					if (conceptMap.hasCopyright())
					{
						conceptMapEntity.setCopyright(conceptMap.getCopyright());
					}
					if (conceptMap.hasVersion())
					{
						conceptMapEntity.setVersion(conceptMap.getVersion());
					}

			        if (conceptMap.hasTargetReference()) {
						conceptMapEntity.setTargetValueset(conceptMap.getTargetReference().getReference());
					}
					if (conceptMap.hasSourceReference()) {
						conceptMapEntity.setSourceValueset(conceptMap.getSourceReference().getReference());
					}
			        
			        
		       	log.trace("Call em.persist ConceptMapEntity");
		       	em.persist(conceptMapEntity); // persisting Concept Maps conceptmap
		       	log.info("Called PERSIST id="+conceptMapEntity.getId().toString());
		        conceptMap.setId(conceptMapEntity.getId().toString());
		        newConceptMapId = conceptMapEntity.getId();
		   //   }
		 //     else
		//      {
		//       	newConceptMapId = getExistingConceptMapEntity(conceptMap.getUrlElement() );
		//      }
			        
			  System.out.println("newConceptMapId = " + newConceptMapId);    
			  conceptMapEntity.setResource(null);

			  for(ConceptMapGroup grp : conceptMapEntity.getGroups()) {
			  	for (ConceptMapGroupElement el :grp.getElements()) {
			  		for (ConceptMapGroupTarget tgt : el.getTargets()) {
			  			em.remove(tgt);
					}
			  		em.remove(el);
				}
			  	em.remove(grp);
			  }

			  for (ConceptMapGroupComponent cmGroup : conceptMap.getGroup()) 
			  {
				  ConceptMapGroup conceptMapGroupEntity =  new ConceptMapGroup();


					conceptMapGroupEntity.setConceptMap(conceptMapEntity);
					conceptMapGroupEntity.setSource(cmGroup.getSource());
					conceptMapGroupEntity.setTarget(cmGroup.getTarget());
					em.persist(conceptMapGroupEntity);      // Persisting ConceptMapGroup conceptmapgroup

				  for (SourceElementComponent cmElement : cmGroup.getElement())
				  {
					  ConceptMapGroupElement conceptMapElementGroupEntity = new ConceptMapGroupElement();
					  conceptMapElementGroupEntity.setConceptMapGroup(conceptMapGroupEntity);
					  Coding element_code = new Coding().setCode(cmElement.getCode()).setDisplay(cmElement.getDisplay()).setSystem(cmGroup.getTarget());
					  ConceptEntity element_concept = conceptDao.findAddCode(element_code);
					  conceptMapElementGroupEntity.setSourceCode(element_concept);   
					  em.persist(conceptMapElementGroupEntity);  // Persisting Elements conceptmapelement
					  
					  for (TargetElementComponent cmTarget : cmElement.getTarget() )
					  {
						  ConceptMapGroupTarget conceptMapGroupTargetEntity = new ConceptMapGroupTarget();
						  conceptMapGroupTargetEntity.setConceptMapGroupElement(conceptMapElementGroupEntity);
						  Coding code = new Coding().setCode(cmTarget.getCode()).setDisplay(cmTarget.getDisplay()).setSystem(cmGroup.getTarget());
						  ConceptEntity concept = conceptDao.findAddCode(code);
						  conceptMapGroupTargetEntity.setTargetCode( concept);
						  conceptMapGroupTargetEntity.setEquivalenceCode(cmTarget.getEquivalence());
						  conceptMapGroupTargetEntity.setConceptMapGroupElement(conceptMapElementGroupEntity);
						  em.persist(conceptMapGroupTargetEntity);  // Persisting Targets  conceptmaptarget
					  }					  
				  }				 				  
		        }
			  
			 // conceptMapElementGroupEntity.setConceptMapGroup(conceptMapGroupEntity);
			  return conceptMap;
	    }   
	    
	    private long getExistingConceptMapEntity(UriType url)
	    {
	    	
	    	CriteriaBuilder cb = em.getCriteriaBuilder();
	    	CriteriaQuery<ConceptMapEntity> cq = cb.createQuery(ConceptMapEntity.class);
	    	
	    	Root<ConceptMapEntity> conceptmap = cq.from(ConceptMapEntity.class) ;
	    	cq.where(cb.equal(conceptmap.get("url"),url.getValue() ) );
	    	cq.select(conceptmap);
	    	TypedQuery<ConceptMapEntity> q = em.createQuery(cq);
	    	
	    	List<ConceptMapEntity> conceptmaps = q.getResultList();
	    	if(conceptmaps.size()>0) return conceptmaps.get(0).getId() ; 
	    	else return 0;
	    	
	    }
	    
	    private ConceptMapEntity findConceptMapEntity(IdType theId) {

	    	System.out.println("the id is " + theId.getIdPart());
	    	
	    	ConceptMapEntity conceptMapEntity = null;
	        // Only look up if the id is numeric else need to do a search
	/*        if (daoutils.isNumeric(theId.getIdPart())) {
	            conceptMapEntity =(ConceptMapEntity) em.find(ConceptMapEntity.class, theId.getIdPart());
	        } */
	        ConceptMapEntity.class.getName();
	        // if null try a search on strId
	        
	            CriteriaBuilder builder =  em.getCriteriaBuilder();

			if (daoutils.isNumeric(theId.getIdPart())) {

				CriteriaQuery<ConceptMapEntity> criteria = builder.createQuery(ConceptMapEntity.class);
				Root<ConceptMapEntity> root = criteria.from(ConceptMapEntity.class);
				List<Predicate> predList = new LinkedList<Predicate>();
				Predicate p = builder.equal(root.<String>get("id"), theId.getIdPart());
				predList.add(p);
				Predicate[] predArray = new Predicate[predList.size()];
				predList.toArray(predArray);
				if (predList.size() > 0) {
					criteria.select(root).where(predArray);

					List<ConceptMapEntity> qryResults = em.createQuery(criteria).getResultList();

					for (ConceptMapEntity cme : qryResults) {
						conceptMapEntity = cme;
						break;
					}
				}
			}
	       // }
	        return conceptMapEntity;
	    }
	    
	    public ConceptMap read(FhirContext ctx, IdType theId) {

	        log.trace("Retrieving ValueSet = " + theId.getValue());

	        ConceptMapEntity conceptMapEntity = findConceptMapEntity(theId);

	        if (conceptMapEntity == null) return null;

	        ConceptMap conceptMap = conceptMapEntityToFHIRConceptMapTransformer.transform(conceptMapEntity);

	        if (conceptMapEntity.getResource() == null) {
	            String resource = ctx.newJsonParser().encodeResourceToString(conceptMap);
	            if (resource.length() < 10000) {
	            	conceptMapEntity.setResource(resource);
	                em.persist(conceptMapEntity);
	            }
	        }
	        return conceptMap;
	      

	    }

	public List<ConceptMap> search (FhirContext ctx,
										  @OptionalParam(name = ConceptMap.SP_NAME) StringParam name,
										  @OptionalParam(name = ConceptMap.SP_PUBLISHER) StringParam publisher,
										  @OptionalParam(name = ConceptMap.SP_URL) UriParam url
	) {
		List<ConceptMapEntity> qryResults = searchEntity(ctx,name,publisher, url);
		List<ConceptMap> results = new ArrayList<>();

		for (ConceptMapEntity conceptmapEntity : qryResults)
		{
			if (conceptmapEntity.getResource() != null) {
				results.add((ConceptMap) ctx.newJsonParser().parseResource(conceptmapEntity.getResource()));
			} else {

				ConceptMap conceptMap = conceptMapEntityToFHIRConceptMapTransformer.transform(conceptmapEntity);
				String resource = ctx.newJsonParser().encodeResourceToString(conceptMap);
				if (resource.length() < 10000) {
					conceptmapEntity.setResource(resource);
					em.persist(conceptmapEntity);
				}
				results.add(conceptMap);
			}
		}
		return results;
	}

	    public List<ConceptMapEntity> searchEntity (FhirContext ctx,
	            @OptionalParam(name = ConceptMap.SP_NAME) StringParam name,
	            @OptionalParam(name = ConceptMap.SP_PUBLISHER) StringParam publisher,
	            @OptionalParam(name = ConceptMap.SP_URL) UriParam url
	    )
	    {
	        List<ConceptMapEntity> qryResults = null;

	        CriteriaBuilder builder = em.getCriteriaBuilder();

	        CriteriaQuery<ConceptMapEntity> criteria = builder.createQuery(ConceptMapEntity.class);
	        Root<ConceptMapEntity> root = criteria.from(ConceptMapEntity.class);

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
	    
	    
	    @Autowired
		private CodeSystemRepository myHapiTerminologySvc;

		@Override
		public TranslationResults translate(TranslationRequests theTranslationRequest, RequestDetails theRequestDetails) {
		//	if (theTranslationRequest.hasReverse() && theTranslationRequest.getReverseAsBoolean()) {
		//		return buildReverseTranslationResult(myHapiTerminologySvc.translateWithReverse(theTranslationRequest));
		//	}

			return buildTranslationResult(myHapiTerminologySvc.translate(theTranslationRequest));
		}
		
		private TranslationResults buildTranslationResult(List<ConceptMapGroupTarget> theTargets) {
			TranslationResults retVal = new TranslationResults();

			String msg;
			if (theTargets.isEmpty()) {

				retVal.setResult(new BooleanType(false));

				msg = "empty";
				/*getContext().getLocalizer().getMessage(
					FhirResourceDaoConceptMapDstu3.class,
					"noMatchesFound");
				*/
				retVal.setMessage(new StringType(msg));

			} else {

				retVal.setResult(new BooleanType(true));

				msg = "empty";
				//msg = getContext().getLocalizer().getMessage(
			//		FhirResourceDaoConceptMapDstu3.class,
			//		"matchesFound");

				retVal.setMessage(new StringType(msg));

				TranslationMatches translationMatch;
				Set<ConceptMapGroupTarget> targetsToReturn = new HashSet<>();
				for (ConceptMapGroupTarget target : theTargets) {
					if (targetsToReturn.add(target)) {
						translationMatch = new TranslationMatches();

						if (target.getEquivalenceCode() != null) {
							translationMatch.setEquivalence((new CodeType(target.getEquivalenceCode().toCode())));
						}

						translationMatch.setConcept(
							new Coding()
								.setCode(target.getTargetCode().getCode() )
								.setSystem(target.getTargetCode().getSystem())
								.setDisplay(target.getTargetCode().getDisplay()));

						translationMatch.setSource((new UriType(target.getConceptMapGroupElement().getConceptMapGroup().getConceptMap().getUrl())));

						retVal.addMatch(translationMatch);
					}
				}
			}

			return retVal;
		}
	    
}

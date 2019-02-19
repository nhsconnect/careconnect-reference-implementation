package uk.nhs.careconnect.ri.dao;

import java.util.LinkedList;
import java.util.List;

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

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ConceptMap;
import org.hl7.fhir.dstu3.model.ConceptMap.ConceptMapGroupComponent;
import org.hl7.fhir.dstu3.model.ConceptMap.SourceElementComponent;
import org.hl7.fhir.dstu3.model.ConceptMap.TargetElementComponent;
import org.hl7.fhir.dstu3.model.IdType;

import org.hl7.fhir.dstu3.model.UriType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import uk.nhs.careconnect.ri.database.daointerface.ConceptMapRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapEntity;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroup;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroupElement;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroupTarget;

import uk.nhs.careconnect.ri.dao.transforms.ConceptMapEntityToFHIRConceptMapTransformer;
//import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;
//import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetInclude;
//import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetIncludeConcept;
//import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetIncludeFilter;
import uk.nhs.careconnect.ri.dao.transforms.PatientEntityToFHIRPatientTransformer;


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

	    private static final Logger log = LoggerFactory.getLogger(ConceptMapDao.class);
	    
	    
	    ConceptMap conceptMap;
	    
	    @Transactional
	    @Override
	    public ConceptMap create(ConceptMap conceptMap) {
	    	
	    //	System.out.println("call came to save Concept MAP : " + conceptMap.getUrlElement().getValue() );
	        this.conceptMap = conceptMap;
	        ConceptMapEntity conceptMapEntity = null;
	        ConceptMapGroup conceptMapGroupEntity =  new ConceptMapGroup();	        
	        ConceptMapGroupElement conceptMapElementGroupEntity = new ConceptMapGroupElement();	        
	        ConceptMapGroupTarget conceptMapGroupTargetEntity = new ConceptMapGroupTarget();
	        System.out.println("id is" + conceptMap.getIdElement());
	        long newConceptMapId = -1;
	        if (conceptMap.hasId()) {
	            conceptMapEntity = findConceptMapEntity(conceptMap.getIdElement());
	        }
	        if(getExistingConceptMapEntity(conceptMap.getUrlElement() )==0)
	        	{
			        if (conceptMapEntity == null)
			        {
			            conceptMapEntity = new ConceptMapEntity();
			        }
		
			        if (conceptMap.hasId())
			        {
			            conceptMapEntity.setId(Long.parseLong(conceptMap.getId()));
			        }
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
			        
			        
		       	log.trace("Call em.persist ConceptMapEntity");
		       	em.persist(conceptMapEntity); // persisting Concept Maps conceptmap
		       	log.info("Called PERSIST id="+conceptMapEntity.getId().toString());
		        conceptMap.setId(conceptMapEntity.getId().toString());
		        newConceptMapId = conceptMapEntity.getId();
		      }
		      else
		      {
		       	newConceptMapId = getExistingConceptMapEntity(conceptMap.getUrlElement() );
		      }
			        
			  System.out.println("newConceptMapId = " + newConceptMapId);    
			  
			  for (ConceptMapGroupComponent cmGroup : conceptMap.getGroup()) 
			  {
				  conceptMapGroupEntity.setSource(cmGroup.getSource());
				  conceptMapGroupEntity.setTarget(cmGroup.getTarget());
				  em.persist(conceptMapGroupEntity);      // Persisting ConceptMapGroup conceptmapgroup
				 
				  for (SourceElementComponent cmElement : cmGroup.getElement())
				  {
				     
					  Coding element_code = new Coding().setCode(cmElement.getCode()).setDisplay(cmElement.getDisplay()).setSystem(cmGroup.getTarget());
					  ConceptEntity element_concept = conceptDao.findAddCode(element_code);
					  conceptMapElementGroupEntity.setSourceCode(element_concept);   
					  em.persist(conceptMapElementGroupEntity);  // Persisting Elements conceptmapelement
					  
					  for (TargetElementComponent cmTarget : cmElement.getTarget() )
					  {						  
						  Coding code = new Coding().setCode(cmTarget.getCode()).setDisplay(cmTarget.getDisplay()).setSystem(cmGroup.getTarget());
						  ConceptEntity concept = conceptDao.findAddCode(code);
						  conceptMapGroupTargetEntity.setTargetCode( concept);
						  conceptMapGroupTargetEntity.setEquivalenceCode(cmTarget.getEquivalence());
						  conceptMapGroupTargetEntity.setConceptMapGroupElement(conceptMapElementGroupEntity);
						  em.persist(conceptMapGroupTargetEntity);  // Persisting Targets  conceptmaptarget
					  }					  
				  }				 				  
		        }
			  
			  conceptMapElementGroupEntity.setConceptMapGroup(conceptMapGroupEntity);
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
	            
	            

	            CriteriaQuery<ConceptMapEntity> criteria = builder.createQuery(ConceptMapEntity.class);
	            Root<ConceptMapEntity> root = criteria.from(ConceptMapEntity.class);
	            List<Predicate> predList = new LinkedList<Predicate>();
	            Predicate p = builder.equal(root.<String>get("id"),theId.getIdPart());
	            predList.add(p);
	            Predicate[] predArray = new Predicate[predList.size()];
	            predList.toArray(predArray);
	            if (predList.size()>0)
	            {
	                criteria.select(root).where(predArray);

	                List<ConceptMapEntity> qryResults = em.createQuery(criteria).getResultList();

	                for (ConceptMapEntity cme : qryResults)
	                {
	                	conceptMapEntity = cme;
	                    break;
	                }
	            }
	       // }
	        return conceptMapEntity;
	    }
	    
}

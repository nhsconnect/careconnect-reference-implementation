package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.jpa.entity.TermConceptMap;
//import ca.uhn.fhir.jpa.entity.TermConceptMapGroup;
//import ca.uhn.fhir.jpa.entity.TermConceptMapGroupElement;
//import ca.uhn.fhir.jpa.entity.TermConceptMapGroupElementTarget;
//import ca.uhn.fhir.jpa.term.TranslationQuery;
//import ca.uhn.fhir.jpa.term.TranslationRequest;
//import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.ScrollableResultsIterator;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.stu3.dao.transforms.CodeSystemEntityToFHIRCodeSystemTransformer;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.entity.TranslationQueries;
import uk.nhs.careconnect.ri.database.entity.TranslationRequests;
import uk.nhs.careconnect.ri.database.entity.codeSystem.*;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroup;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroupElement;
import uk.nhs.careconnect.ri.database.entity.conceptMap.ConceptMapGroupTarget;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.github.benmanes.caffeine.cache.Cache;

@Repository
@Transactional
public class CodeSystemDao implements CodeSystemRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;


    @Autowired
    private PlatformTransactionManager myTransactionMgr;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
            private CodeSystemEntityToFHIRCodeSystemTransformer codeSystemEntityToFHIRCodeSystemTransformer;


    CodeSystem codeSystem;

    private static final Logger log = LoggerFactory.getLogger(CodeSystemDao.class);

    private boolean myProcessDeferred = true;


    private List<ConceptEntity> myConceptsToSaveLater = new ArrayList<ConceptEntity>();

    private List<ConceptParentChildLink> myConceptLinksToSaveLater = new ArrayList<ConceptParentChildLink>();
    private static boolean ourLastResultsFromTranslationCache;

    // What we need to do is process concepts coming from CodeSystems in a transactional mode (@Transactional)
    // For CodeSystem inserts we need to get the codes into the database as without storing them in massive memory objects
    // Other resources should run as transactional
    //TermConceptMapGroupElementTarget
    private Cache<TranslationQueries, List<ConceptMapGroupTarget>> myTranslationCache;

    @Override
    public CodeSystemEntity readEntity(FhirContext ctx, IdType theId) {
        return null;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(CodeSystemEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public void setProcessDeferred(boolean theProcessDeferred) {
        myProcessDeferred = theProcessDeferred;
    }


    @Override
    public CodeSystem read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            CodeSystemEntity codeSystemEntity = em.find(CodeSystemEntity.class, Long.parseLong(theId.getIdPart()));
            return codeSystemEntityToFHIRCodeSystemTransformer.transform(codeSystemEntity);
        }
        return null;
    }

    @Override
    public CodeSystem create(FhirContext ctx, CodeSystem codeSystem) {

        this.codeSystem = codeSystem;

        CodeSystemEntity codeSystemEntity = null;

        /* KGM removed as was adding the codeSystem 9/4/2019
        if (codeSystem.hasId()) {
            codeSystemEntity = findBySystem(codeSystem.getUrl());
        }
         */

        List<CodeSystemEntity> entries = searchEntity(ctx, null, null, new UriParam().setValue(codeSystem.getUrl()));
        for (CodeSystemEntity msg : entries) {
            if (codeSystem.getId() == null) {
                throw new ResourceVersionConflictException("Url "+ msg.getCodeSystemUri()+ " is already present on the system "+ msg.getId());
            }

            if (!msg.getId().toString().equals(codeSystem.getIdElement().getIdPart())) {
                throw new ResourceVersionConflictException("CodeSystem Url "+msg.getCodeSystemUri()+ " is already present on the system "+ msg.getId());
            } else {
                codeSystemEntity = msg;
            }
        }

        if (codeSystemEntity == null)
        {
            codeSystemEntity = new CodeSystemEntity();
        }

        codeSystemEntity.setResource(null);

        if (codeSystem.hasUrl())
        {
            codeSystemEntity.setCodeSystemUri(codeSystem.getUrl());
        }
        if (codeSystem.hasVersion()) {
            codeSystemEntity.setVersion(codeSystem.getVersion());
        }
        if (codeSystem.hasName())
        {
            codeSystemEntity.setName(codeSystem.getName());
        }
        if (codeSystem.hasTitle()) {
            codeSystemEntity.setTitle(codeSystem.getTitle());
        }
        if (codeSystem.hasStatus())
        {
            codeSystemEntity.setStatus(codeSystem.getStatus());
        }
        if (codeSystem.hasExperimental()) {
            codeSystemEntity.setExperimental(codeSystem.getExperimental());
        }
        if (codeSystem.hasDate()) {
            codeSystemEntity.setChangeDateTime(codeSystem.getDate());
        }
        if (codeSystem.hasPublisher()) {
            codeSystemEntity.setPublisher(codeSystem.getPublisher());
        }

        if (codeSystem.hasDescription())
        {
            codeSystemEntity.setDescription(codeSystem.getDescription());
        }


        if (codeSystem.hasPurpose()) {
            codeSystemEntity.setPurpose(codeSystem.getPurpose());
        }
        if (codeSystem.hasCopyright()) {
            codeSystemEntity.setCopyright(codeSystem.getCopyright());
        }



        log.trace("Call em.persist CodeSystemEntity");
        em.persist(codeSystemEntity);

        //Created the CodeSystem so add the sub concepts

        for (CodeSystemTelecom
                telcom : codeSystemEntity.getContacts()) {
            em.remove(telcom);
        }

        for (ContactDetail contact : codeSystem.getContact()) {
            for (ContactPoint contactPoint : contact.getTelecom()) {
                CodeSystemTelecom telecom = new CodeSystemTelecom();
                telecom.setCodeSystem(codeSystemEntity);
                if (contactPoint.hasSystem()) {
                    telecom.setSystem(contactPoint.getSystem());
                }
                if (contactPoint.hasValue()) {
                    telecom.setValue(contactPoint.getValue());
                }
                if (contactPoint.hasUse()) {
                    telecom.setTelecomUse(contactPoint.getUse());
                }
                em.persist(telecom);
            }
        }


        for (CodeSystem.ConceptDefinitionComponent concept : codeSystem.getConcept()) {
            conceptDao.findAddCode(new Coding().setSystem(codeSystemEntity.getCodeSystemUri()).setCode(concept.getCode()).setDisplay(concept.getDisplay()));
        }

        log.debug("Called PERSIST id="+codeSystemEntity.getId().toString());
        codeSystem.setId(codeSystemEntity.getId().toString());

        CodeSystem newCodeSystem = null;
        if (codeSystemEntity != null) {
            newCodeSystem = codeSystemEntityToFHIRCodeSystemTransformer.transform(codeSystemEntity);
            String resource = ctx.newJsonParser().encodeResourceToString(newCodeSystem);
            if (resource.length() < 10000) {
                codeSystemEntity.setResource(resource);
                em.persist(codeSystemEntity);
            }

        }
        return newCodeSystem;
        
    }

    @Override
    public List<CodeSystem> search(FhirContext ctx,
                                   @OptionalParam(name = CodeSystem.SP_NAME) StringParam name,
                                   @OptionalParam(name = CodeSystem.SP_PUBLISHER) StringParam publisher,
                                   @OptionalParam(name = CodeSystem.SP_URL) UriParam url
    ) {
        List<CodeSystemEntity> qryResults = searchEntity(ctx, name, publisher, url);
        List<CodeSystem> results = new ArrayList<>();

        for (CodeSystemEntity valuesetEntity : qryResults)
        {
            if (valuesetEntity.getResource() != null) {
                results.add((CodeSystem) ctx.newJsonParser().parseResource(valuesetEntity.getResource()));
            } else {

                CodeSystem codeSystem = codeSystemEntityToFHIRCodeSystemTransformer.transform(valuesetEntity);
                String resource = ctx.newJsonParser().encodeResourceToString(codeSystem);
                if (resource.length() < 10000) {
                    valuesetEntity.setResource(resource);
                    em.persist(valuesetEntity);
                }
                results.add(codeSystem);
            }
        }
        return results;
    }


    public List<CodeSystemEntity> searchEntity(FhirContext ctx,
                                   @OptionalParam(name = CodeSystem.SP_NAME) StringParam name,
                                   @OptionalParam(name = CodeSystem.SP_PUBLISHER) StringParam publisher,
                                   @OptionalParam(name = CodeSystem.SP_URL) UriParam url
    ) {


        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<CodeSystemEntity> criteria = builder.createQuery(CodeSystemEntity.class);
        Root<CodeSystemEntity> root = criteria.from(CodeSystemEntity.class);

        List<Predicate> predList = new LinkedList<>();


        if (name != null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal("%" + name.getValue() + "%"))
                    );

            predList.add(p);
        }
        if (publisher != null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("publisher").as(String.class)),
                            builder.upper(builder.literal( publisher.getValue() + "%"))
                    );

            predList.add(p);
        }
        if (url != null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("codeSystemUri").as(String.class)),
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

        return em.createQuery(criteria).setMaxResults(100).getResultList();


    }

    @Override
    public void save(FhirContext ctx, CodeSystemEntity codeSystemEntity) {
        em.persist(codeSystemEntity);
    }



    @Override
    @Transactional
    public CodeSystemEntity findBySystem(String system) {

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CodeSystemEntity codeSystemEntity = null;
        CriteriaQuery<CodeSystemEntity> criteria = builder.createQuery(CodeSystemEntity.class);

        Root<CodeSystemEntity> root = criteria.from(CodeSystemEntity.class);
        List<Predicate> predList = new LinkedList<Predicate>();
        log.trace("FlushMode = "+em.getFlushMode());
        log.trace("Entity Manager Properties = "+ em.getProperties().toString());
        Predicate p = builder.equal(root.<String>get("codeSystemUri"),system);
        predList.add(p);
        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            log.trace("Found CodeSystem "+system);
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

            save(null, codeSystemEntity);

        }
        return codeSystemEntity;
    }

    @Override
    @Transactional
    public SystemEntity findSystem(String system) throws OperationOutcomeException {

        if (system==null || system.isEmpty()) {
            throw new OperationOutcomeException("System is required","System is required",OperationOutcome.IssueType.INVALID);
        }
        CriteriaBuilder builder = em.getCriteriaBuilder();

        SystemEntity systemEntity = null;
        CriteriaQuery<SystemEntity> criteria = builder.createQuery(SystemEntity.class);

        Root<SystemEntity> root = criteria.from(SystemEntity.class);
        List<Predicate> predList = new LinkedList<Predicate>();
        log.debug("Looking for System = " + system);

        Predicate p = builder.equal(root.<String>get("codeSystemUri"),system);
        predList.add(p);
        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            log.debug("Found System "+system);
            criteria.select(root).where(predArray);

            List<SystemEntity> qryResults = em.createQuery(criteria).getResultList();

            for (SystemEntity cme : qryResults) {
                systemEntity = cme;
                break;
            }
        }
        if (systemEntity == null) {
            log.info("Not found. Adding SystemEntity = "+system);
            systemEntity = new SystemEntity();
            systemEntity.setUri(system);

            em.persist(systemEntity);
        }
        return systemEntity;
    }
    
    @Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<ConceptMapGroupTarget> translate(TranslationRequests theTranslationRequests) {
		List<ConceptMapGroupTarget> retVal = new ArrayList<>();

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<ConceptMapGroupTarget> query = criteriaBuilder.createQuery(ConceptMapGroupTarget.class);
		//TermConceptMapGroupElementTarget.class);
		Root<ConceptMapGroupTarget> root = query.from(ConceptMapGroupTarget.class);
		//TermConceptMapGroupElement
		Join<ConceptMapGroupTarget, ConceptMapGroupElement> elementJoin = root.join("conceptMapGroupElement");
		//TermConceptMapGroup
		Join<ConceptMapGroupElement, ConceptMapGroup> groupJoin = elementJoin.join("conceptMapGroup");
		//ConceptMapGroup
		Join<ConceptMapGroup, ConceptMap> conceptMapJoin = groupJoin.join("conceptMap");

		List<TranslationQueries> translationQueries = theTranslationRequests.getTranslationQueries();
		List<ConceptMapGroupTarget> cachedTargets;
		ArrayList<Predicate> predicates;
		Coding coding;
		System.out.println("trans query");
		
		
		for (TranslationQueries translationQuery : translationQueries) {
			System.out.println("get coding " + translationQuery.getCoding().getCode() + " get display " + translationQueries.get(0)
			+ translationQuery.getCoding().getSystem()
					);
		cachedTargets = null; //myTranslationCache .getIfPresent(translationQuery);
			if (cachedTargets == null) {
				final List<ConceptMapGroupTarget> targets = new ArrayList<>();

				predicates = new ArrayList<>();

				//coding =  VersionConvertor_30_40.convertCoding(translationQuery.getCoding());
				coding = translationQuery.getCoding();
				
			//				Coding element_code = new Coding().setCode(translationQuery.getCoding().getCode()).setDisplay(translationQuery.getCoding().getDisplay());
				
				//Coding element_code = new Coding().setCode(value) coding) translationQuery.getCoding() );						
						//cmElement.getDisplay()).setSystem(cmGroup.getTarget());
				System.out.println(coding);
				//System.out.println(element_code);
				
				ConceptEntity ce = conceptDao.findCode( translationQuery.getCoding());
						
					//	(coding.getCode());
				//ce.fi
				System.out.println( translationQuery.getCoding().getId() );
				//Coding code = new Coding().setCode(translationQueries.get .getCode()).setDisplay(cmTarget.getDisplay()).setSystem(cmGroup.getTarget());
				
				if (coding.hasCode()) {
					System.out.println("code is " + coding.getCode() + " id is "  +ce.getId());
					//predicates.add(criteriaBuilder.equal(elementJoin.get("sourceCode"), ce.getId()));// .getCode() ));
					predicates.add(criteriaBuilder.equal(elementJoin.get("sourceCode"), 32994));// .getCode() ));
				} else {
					throw new InvalidRequestException("A code must be provided for translation to occur.");
				}

				if (coding.hasSystem()) {
					System.out.println("has system");
					predicates.add(criteriaBuilder.equal(groupJoin.get("source"), coding.getSystem()));
				}

				if (coding.hasVersion()) {
					System.out.println("has version");
					predicates.add(criteriaBuilder.equal(groupJoin.get("mySourceVersion"), coding.getVersion()));
				}

				if (translationQuery.hasTargetSystem()) {
					System.out.println("has target system");
					predicates.add(criteriaBuilder.equal(groupJoin.get("myTarget"), translationQuery.getTargetSystem().getValueAsString()));
				}

				if (translationQuery.hasSource()) {
					System.out.println("has source");
					predicates.add(criteriaBuilder.equal(conceptMapJoin.get("mySource"), translationQuery.getSource().getValueAsString()));
				}

				if (translationQuery.hasTarget()) {
					System.out.println("has target");
					predicates.add(criteriaBuilder.equal(conceptMapJoin.get("myTarget"), translationQuery.getTarget().getValueAsString()));
				}

				if (translationQuery.hasResourceId()) {
					System.out.println("has resource pid");
					predicates.add(criteriaBuilder.equal(conceptMapJoin.get("myResourcePid"), translationQuery.getResourceId()));
				}

				Predicate outerPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
				query.where(outerPredicate);

				// Use scrollable results.
				final TypedQuery<ConceptMapGroupTarget> typedQuery = em.createQuery(query.select(root));
				org.hibernate.query.Query<ConceptMapGroupTarget> hibernateQuery = (org.hibernate.query.Query<ConceptMapGroupTarget>) typedQuery;
			//	hibernateQuery.setFetchSize(myFetchSize);
				hibernateQuery.setFetchSize(20);
				ScrollableResults scrollableResults = hibernateQuery.scroll(ScrollMode.FORWARD_ONLY);
				Iterator<ConceptMapGroupTarget> scrollableResultsIterator = new ScrollableResultsIterator<>(scrollableResults);
				
				System.out.println("scrollableResultsIterator : " + scrollableResultsIterator.hasNext() ); 
				
				while (scrollableResultsIterator.hasNext()) {
					targets.add(scrollableResultsIterator.next());
				}

				ourLastResultsFromTranslationCache = false; // For testing.
			//	myTranslationCache.get(translationQuery, k -> targets);
				
				retVal.addAll(targets);
			} else {
				ourLastResultsFromTranslationCache = true; // For testing.
				retVal.addAll(cachedTargets);
			}
		}

		return retVal;
	}

}

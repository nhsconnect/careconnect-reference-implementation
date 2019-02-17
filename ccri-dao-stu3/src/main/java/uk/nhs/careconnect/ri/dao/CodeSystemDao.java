package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.transforms.CodeSystemEntityToFHIRCodeSystemTransformer;
import uk.nhs.careconnect.ri.database.daointerface.CodeSystemRepository;
import uk.nhs.careconnect.ri.database.entity.Terminology.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class CodeSystemDao implements CodeSystemRepository {

    @PersistenceContext
    EntityManager em;

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

    // What we need to do is process concepts coming from CodeSystems in a transactional mode (@Transactional)
    // For CodeSystem inserts we need to get the codes into the database as without storing them in massive memory objects
    // Other resources should run as transactional

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

        if (codeSystem.hasId()) {
            codeSystemEntity = findBySystem(codeSystem.getUrl());
        }

        if (codeSystemEntity == null)
        {
            codeSystemEntity = new CodeSystemEntity();
        }



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
            findAddCode(codeSystemEntity, concept);
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
        List<CodeSystemEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<CodeSystemEntity> criteria = builder.createQuery(CodeSystemEntity.class);
        Root<CodeSystemEntity> root = criteria.from(CodeSystemEntity.class);

        List<Predicate> predList = new LinkedList<>();
        List<CodeSystem> results = new ArrayList<>();

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

        qryResults = em.createQuery(criteria).setMaxResults(100).getResultList();

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


    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, CodeSystem.ConceptDefinitionComponent concept) {
        // This inspects codes already present and if not found it adds the code... CRUDE at present

        ConceptEntity conceptEntity = null;
        for (ConceptEntity codeSystemConcept : codeSystemEntity.getConcepts()) {
            if (codeSystemConcept.getCode().equals(concept.getCode())) {

                conceptEntity =codeSystemConcept;
            }

        }
        if (conceptEntity == null) {
            log.debug("Add new code = " + concept.getCode());
            conceptEntity = new ConceptEntity()
                    .setCode(concept.getCode())
                    .setCodeSystem(codeSystemEntity)
                    .setDisplay(concept.getDisplay());


            em.persist(conceptEntity);
            // Need to ensure the local version has a copy of the data
            codeSystemEntity.getConcepts().add(conceptEntity);
        }

        return conceptEntity;
    }

    @Override
    @Transactional
    public ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ValueSet.ConceptReferenceComponent concept) {
        // This inspects codes already present and if not found it adds the code... CRUDE at present



        ConceptEntity conceptEntity = null;
        for (ConceptEntity codeSystemConcept : codeSystemEntity.getConcepts()) {
            if (codeSystemConcept.getCode().equals(concept.getCode())) {

                conceptEntity =codeSystemConcept;
            }

        }
        if (conceptEntity == null) {
            log.debug("Add new code = " + concept.getCode());
            conceptEntity = new ConceptEntity()
                    .setCode(concept.getCode())
                    .setCodeSystem(codeSystemEntity)
                    .setDisplay(concept.getDisplay());
                // TODO STU3    .setAbstractCode(concept.getAbstract());


            em.persist(conceptEntity);
            // Need to ensure the local version has a copy of the data
            codeSystemEntity.getConcepts().add(conceptEntity);
        }
        // call child code
        // TODO STU3 Refactor
        /*
        if (concept.getConcept().size() > 0) {
            processChildConcepts(concept,conceptEntity);
        }
        */

        return conceptEntity;
    }

    /*
    @Transactional
    private void processChildConcepts(CodeSystem.ConceptReferenceComponent concept, ConceptEntity parentConcept) {
        String lastConcept="";
        // TODO STU3
        /*
        for (CodeSystem.ConceptReferenceComponent conceptChild : concept.getConcept()) {
            ConceptParentChildLink childLink = null;

            if (conceptChild.getCode() != null && !conceptChild.getCode().equals(lastConcept)) {
                // To cope with repeating entries
                lastConcept = conceptChild.getCode();
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


                    ConceptEntity childConcept = findAddCode(parentConcept.getCodeSystem(), conceptChild);


                    childLink.setChild(childConcept);
                    em.persist(childLink);
                    // ensure link add to object
                    parentConcept.getChildren().add(childLink);

                }
            }
        }
    }
    */
}

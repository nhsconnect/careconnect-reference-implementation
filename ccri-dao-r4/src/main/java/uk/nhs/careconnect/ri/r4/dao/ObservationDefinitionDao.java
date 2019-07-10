package uk.nhs.careconnect.ri.r4.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ObservationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.ValueSetRepository;
import uk.nhs.careconnect.ri.database.entity.codeSystem.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.observationDefinition.ObservationDefinitionCategory;
import uk.nhs.careconnect.ri.database.entity.observationDefinition.ObservationDefinitionEntity;
import uk.nhs.careconnect.ri.database.entity.observationDefinition.ObservationDefinitionIdentifier;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;
import uk.nhs.careconnect.ri.database.daointerface.ObservationDefinitionRepository;
import uk.nhs.careconnect.ri.r4.dao.transform.ObservationDefinitionEntityToFHIRObservationDefinitionTransformer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class ObservationDefinitionDao implements ObservationDefinitionRepository {

    @PersistenceContext
    EntityManager em;


    @Autowired
    private LibDaoR4 libDao;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    @Lazy
    ValueSetRepository valueSetDao;

    @Autowired
    private ObservationDefinitionEntityToFHIRObservationDefinitionTransformer observationDefinitionEntityToFHIRObservationDefinitionTransformer;

    //ObservationDefinition observationDefinition;


    private static final Logger log = LoggerFactory.getLogger(ObservationDefinitionDao.class);

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ObservationDefinitionEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }


    @Override
    public OperationOutcome delete(FhirContext ctx, IdType theId) {
        log.trace("Delete OperationDefinition = " + theId.getValue());

        ObservationDefinitionEntity observationDefinitionEntity = readEntity(ctx, theId);

        if (observationDefinitionEntity == null) return null;

        for (ObservationDefinitionIdentifier identifier : observationDefinitionEntity.getIdentifiers()) {
            em.remove(identifier);
        }
        em.remove(observationDefinitionEntity);
        return new OperationOutcome();

    }


    @Override
    public ObservationDefinition create(FhirContext ctx, ObservationDefinition observationDefinition, IdType theId) throws OperationOutcomeException {


        log.info("ObservationDefinition Create");
        if (observationDefinition.hasCode() && observationDefinition.getCode().hasCoding()) {
            System.out.println("call came to save ObservationDefinition : system=" + observationDefinition.getCode().getCodingFirstRep().getSystem() + " code=" + observationDefinition.getCode().getCodingFirstRep().getCode());
        }
        // this.observationDefinition = observationDefinition;
        ObservationDefinitionEntity observationDefinitionEntity = null;

        System.out.println("id is" + observationDefinition.getIdElement());
        long newObservationDefinitionId = -1;
        if (observationDefinition.hasId()) {
            observationDefinitionEntity = readEntity(ctx, observationDefinition.getIdElement());
        }

        // Prevent duplicate entries for codes
        if (observationDefinition.hasCode() && observationDefinition.getCode().hasCoding()) {
            List<ObservationDefinitionEntity> entries = searchEntity(ctx, null,
                    new TokenOrListParam().add(new TokenParam().setSystem(observationDefinition.getCode().getCodingFirstRep().getSystem()).setValue(observationDefinition.getCode().getCodingFirstRep().getCode()))
                    , null
                    , null
            ,null);
            for (ObservationDefinitionEntity nameSys : entries) {
                if (observationDefinition.getId() == null) {
                    throw new ResourceVersionConflictException(observationDefinition.getCode().getCodingFirstRep().getSystem() + " code=" + observationDefinition.getCode().getCodingFirstRep().getCode() + " is already present on the system " + nameSys.getId());
                }

                if (!nameSys.getId().equals(observationDefinitionEntity.getId())) {
                    throw new ResourceVersionConflictException(observationDefinition.getCode().getCodingFirstRep().getSystem() + " code=" + observationDefinition.getCode().getCodingFirstRep().getCode() + " is already present on the system " + nameSys.getId());
                }
            }
        }


        if (observationDefinitionEntity == null) {
            observationDefinitionEntity = new ObservationDefinitionEntity();
        }


        if (observationDefinition.hasCode()) {
            if (observationDefinition.getCode().hasCoding()) {
                ConceptEntity code = conceptDao.findAddCode(observationDefinition.getCode().getCoding().get(0));
                if (code != null) {
                    observationDefinitionEntity.setCode(code);
                } else {
                    log.info("Code: Missing System/Code = " + observationDefinition.getCode().getCoding().get(0).getSystem() + " code = " + observationDefinition.getCode().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = " + observationDefinition.getCode().getCoding().get(0).getSystem() + " code = " + observationDefinition.getCode().getCoding().get(0).getCode());
                }
            }
            if (observationDefinition.getCode().hasText()) {
                observationDefinitionEntity.setCodeText(observationDefinition.getCode().getText());
            }

        }
        ValueSetEntity valueSetEntity = null;

        if (observationDefinition.hasNormalCodedValueSet()) {
            org.hl7.fhir.dstu3.model.IdType idType = new org.hl7.fhir.dstu3.model.IdType();
            idType.setValueAsString(observationDefinition.getNormalCodedValueSet().getReference());
            valueSetEntity = valueSetDao.readEntity(ctx, idType);
            if (valueSetEntity != null ) {
                observationDefinitionEntity.setNormalValueSet(valueSetEntity);
            } else {
                observationDefinitionEntity.setNormalValueSet(null);
               // throw new ResourceNotFoundException("Normal ValueSet reference was not found");
            }
        }

        if (observationDefinition.hasAbnormalCodedValueSet()) {
            org.hl7.fhir.dstu3.model.IdType idType = new org.hl7.fhir.dstu3.model.IdType();
            idType.setValueAsString(observationDefinition.getAbnormalCodedValueSet().getReference());
            valueSetEntity = valueSetDao.readEntity(ctx, idType);
            if (valueSetEntity != null ) {
                observationDefinitionEntity.setAbnormalValueSet(valueSetEntity);

            } else {
                observationDefinitionEntity.setAbnormalValueSet(null);
               // throw new ResourceNotFoundException("Abnormal ValueSet reference was not found");
            }
        }

        if (observationDefinition.hasValidCodedValueSet()) {
            log.info("Has ValidCodedValueSet");
            org.hl7.fhir.dstu3.model.IdType idType = new org.hl7.fhir.dstu3.model.IdType();
            idType.setValueAsString(observationDefinition.getValidCodedValueSet().getReference());
            valueSetEntity = valueSetDao.readEntity(ctx, idType);

            if (valueSetEntity != null ) {
                observationDefinitionEntity.setValidValueSet(valueSetEntity);
            } else {
                observationDefinitionEntity.setValidValueSet(null);
               // throw new ResourceNotFoundException("Valid ValueSet reference was not found");
            }
        }

        if (observationDefinition.hasCriticalCodedValueSet()) {
            org.hl7.fhir.dstu3.model.IdType idType = new org.hl7.fhir.dstu3.model.IdType();
            idType.setValueAsString(observationDefinition.getCriticalCodedValueSet().getReference());
            valueSetEntity = valueSetDao.readEntity(ctx, idType);

            if (valueSetEntity != null ) {
                observationDefinitionEntity.setCriticalValueSet(valueSetEntity);
            } else {
                observationDefinitionEntity.setCriticalValueSet(null);
               // throw new ResourceNotFoundException("Critical ValueSet reference was not found");
            }
        }

        observationDefinitionEntity.setResource(null);
        // Removed Id

        observationDefinitionEntity.setResource(ctx.newJsonParser().encodeResourceToString(observationDefinition));

        log.trace("Call em.persist ObservationDefinitionEntity");
        em.persist(observationDefinitionEntity); // persisting Concept Maps observationDefinition

        log.info("Called PERSIST id=" + observationDefinitionEntity.getId().toString());

        if (observationDefinition.hasIdentifier()) {
            for (ObservationDefinitionIdentifier identifier : observationDefinitionEntity.getIdentifiers()) {
                em.remove(identifier);
            }
            for (Identifier identifier : observationDefinition.getIdentifier()) {
                ObservationDefinitionIdentifier observationDefinitionIdentifier = new ObservationDefinitionIdentifier();
                observationDefinitionIdentifier.setObservationDefinition(observationDefinitionEntity);
                observationDefinitionIdentifier = (ObservationDefinitionIdentifier) libDao.setIdentifier(identifier, observationDefinitionIdentifier);
                em.persist(observationDefinitionIdentifier);
            }
        }

        for (ObservationDefinitionCategory observationCategory : observationDefinitionEntity.getCategories()) {
            em.remove(observationCategory);
        }

        for (CodeableConcept concept :observationDefinition.getCategory()) {
            // Category must have a code 15/Jan/2018 testing with Synthea examples

            ObservationDefinitionCategory category = null;
            // Look for existing categories
            for (ObservationDefinitionCategory cat :observationDefinitionEntity.getCategories()) {
                category= cat;
            }
            if (category == null) category = new ObservationDefinitionCategory();

            category.setObservationDefinition(observationDefinitionEntity);

            if (concept.hasCoding()) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    category.setConceptCode(conceptEntity);
                }
                else {
                    log.info("Missing Category. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
            if (concept.hasText()) {
                category.setConceptText(concept.getText());
            }
            em.persist(category);
            observationDefinitionEntity.getCategories().add(category);

        }

        observationDefinition.setId(observationDefinitionEntity.getId().toString());


        return observationDefinitionEntityToFHIRObservationDefinitionTransformer.transform(observationDefinitionEntity, ctx);
    }


    @Override
    public ObservationDefinitionEntity readEntity(FhirContext ctx, IdType theId) {

        System.out.println("the id is " + theId.getIdPart());

        ObservationDefinitionEntity observationDefinitionEntity = null;
        // Only look up if the id is numeric else need to do a search
	/*        if (daoutils.isNumeric(theId.getIdPart())) {
	            observationDefinitionEntity =(ObservationDefinitionEntity) em.find(ObservationDefinitionEntity.class, theId.getIdPart());
	        } */
        ObservationDefinitionEntity.class.getName();
        // if null try a search on strId

        CriteriaBuilder builder = em.getCriteriaBuilder();

        if (daoutilsR4.isNumeric(theId.getIdPart())) {

            CriteriaQuery<ObservationDefinitionEntity> criteria = builder.createQuery(ObservationDefinitionEntity.class);
            Root<ObservationDefinitionEntity> root = criteria.from(ObservationDefinitionEntity.class);
            List<Predicate> predList = new LinkedList<Predicate>();
            Predicate p = builder.equal(root.<String>get("id"), theId.getIdPart());
            predList.add(p);
            Predicate[] predArray = new Predicate[predList.size()];
            predList.toArray(predArray);
            if (predList.size() > 0) {
                criteria.select(root).where(predArray);

                List<ObservationDefinitionEntity> qryResults = em.createQuery(criteria).getResultList();

                for (ObservationDefinitionEntity cme : qryResults) {
                    observationDefinitionEntity = cme;
                    break;
                }
            }
        }
        // }
        return observationDefinitionEntity;
    }

    @Override
    public void save(FhirContext ctx, ObservationDefinitionEntity resource) throws OperationOutcomeException {
        return;
    }

    public ObservationDefinition read(FhirContext ctx, IdType theId) {

        log.trace("Retrieving ValueSet = " + theId.getValue());

        ObservationDefinitionEntity observationDefinitionEntity = readEntity(ctx, theId);

        if (observationDefinitionEntity == null) return null;

        ObservationDefinition observationDefinition = observationDefinitionEntityToFHIRObservationDefinitionTransformer.transform(observationDefinitionEntity, ctx);

        if (observationDefinitionEntity.getResource() == null) {
            String resource = ctx.newJsonParser().encodeResourceToString(observationDefinition);
            if (resource.length() < 10000) {
                observationDefinitionEntity.setResource(resource);
                em.persist(observationDefinitionEntity);
            }
        }
        return observationDefinition;


    }


    @Override
    public List<ObservationDefinition> search(FhirContext ctx, TokenParam category, TokenOrListParam code, TokenParam identifier, StringParam name, StringParam id) {

        List<ObservationDefinitionEntity> qryResults = searchEntity(ctx, category, code, identifier, name, id);
        List<ObservationDefinition> results = new ArrayList<>();

        for (ObservationDefinitionEntity observationDefinitionEntity : qryResults) {

            ObservationDefinition observationDefinition = observationDefinitionEntityToFHIRObservationDefinitionTransformer.transform(observationDefinitionEntity, ctx);

            results.add(observationDefinition);

        }
        return results;
    }

    @Override
    public List<ObservationDefinitionEntity> searchEntity(FhirContext ctx, TokenParam category, TokenOrListParam codes, TokenParam identifier, StringParam name,  StringParam id) {

        List<ObservationDefinitionEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ObservationDefinitionEntity> criteria = builder.createQuery(ObservationDefinitionEntity.class);
        Root<ObservationDefinitionEntity> root = criteria.from(ObservationDefinitionEntity.class);

        List<Predicate> predList = new LinkedList<>();

        if (id != null) {
            Predicate p = builder.equal(root.get("id"),id.getValue());
            predList.add(p);
        }
        if (category!=null) {
            log.trace("Search on ObservationDefinition.category code = "+category.getValue());
            Join<ObservationDefinitionEntity, ObservationDefinitionCategory> join = root.join("categories", JoinType.LEFT);
            Join<ObservationDefinitionCategory, ConceptEntity> joinConcept = join.join("category", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),category.getValue());
            predList.add(p);
        }
        if (name!=null) {
            log.trace("Search on ObservationDefinition name = "+name.getValue());
            Join<ObservationDefinitionEntity,
                    ConceptEntity> joinConcept = root.join("code", JoinType.LEFT);
            Predicate p =   builder.like(
                    builder.upper(joinConcept.get("myDisplay").as(String.class)),
                    builder.upper(builder.literal("%" + name.getValue() + "%"))
            );

            predList.add(p);
        }
        if (codes!=null) {
            List<Predicate> predOrList = new LinkedList<>();
            Join<ObservationDefinitionEntity, ConceptEntity> joinConcept = root.join("code", JoinType.LEFT);
            Join<ConceptEntity, CodeSystemEntity> joinCodeSystem = joinConcept.join("codeSystemEntity", JoinType.LEFT);

            for (TokenParam code : codes.getValuesAsQueryTokens()) {
                log.trace("Search on Observation.code code = " + code.getValue());

                Predicate p = null;
                if (code.getSystem() != null) {
                    p = builder.and(builder.equal(joinCodeSystem.get("codeSystemUri"), code.getSystem()),builder.equal(joinConcept.get("code"), code.getValue()));
                } else {
                    p = builder.equal(joinConcept.get("code"), code.getValue());
                }
                predOrList.add(p);

            }
            if (predOrList.size()>0) {
                Predicate p = builder.or(predOrList.toArray(new Predicate[0]));
                predList.add(p);
            }
        }
        if (identifier !=null)
        {
            Join<ObservationDefinitionEntity, ObservationDefinitionIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }


        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size() > 0) {
            criteria.select(root).where(predArray);
        } else {
            criteria.select(root);
        }

        qryResults = em.createQuery(criteria).setMaxResults(100).getResultList();

        return qryResults;
    }

}

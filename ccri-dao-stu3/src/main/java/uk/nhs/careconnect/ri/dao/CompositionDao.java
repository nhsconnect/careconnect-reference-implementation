package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.CompositionEntityToFHIRCompositionTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.composition.CompositionEntity;
import uk.nhs.careconnect.ri.database.entity.composition.CompositionIdentifier;
import uk.nhs.careconnect.ri.database.entity.composition.CompositionSection;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.relatedPerson.RelatedPersonEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class CompositionDao implements CompositionRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    RelatedPersonRepository personDao;

    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    CompositionEntityToFHIRCompositionTransformer compositionEntityToFHIRCompositionTransformer;


    private static final Logger log = LoggerFactory.getLogger(CompositionDao.class);
    @Override
    public void save(FhirContext ctx, CompositionEntity composition) {

    }

    @Override
    public Composition read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            CompositionEntity composition = em.find(CompositionEntity.class, Long.parseLong(theId.getIdPart()));

            return composition != null ? compositionEntityToFHIRCompositionTransformer.transform(composition) : null;
        } else {
            return null;
        }
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(CompositionEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public CompositionEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            CompositionEntity composition = em.find(CompositionEntity.class, Long.parseLong(theId.getIdPart()));

            return composition;
        } else {
            return null;
        }
    }

    @Override
    public Composition create(FhirContext ctx, Composition composition, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("Composition.save");

        CompositionEntity compositionEntity = null;

        if (composition.hasId()) compositionEntity = readEntity(ctx, composition.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/composition")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<CompositionEntity> results = searchEntity(ctx, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/composition"),null, null, null);
                    for (CompositionEntity con : results) {
                        compositionEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (compositionEntity == null) {
            compositionEntity = new CompositionEntity();
        }



        if (composition.hasStatus()) {
            compositionEntity.setStatus(composition.getStatus());
        }

        if (composition.hasType()) {
            ConceptEntity code = conceptDao.findAddCode(composition.getType().getCoding().get(0));
            if (code != null) { compositionEntity.setType(code); }
            else {
                log.info("Type: Missing System/Code = "+ composition.getType().getCoding().get(0).getSystem() +" code = "+composition.getType().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ composition.getType().getCoding().get(0).getSystem()
                        +" code = "+composition.getType().getCoding().get(0).getCode());
            }
        }
        if (composition.hasClass_()) {
            ConceptEntity code = conceptDao.findAddCode(composition.getClass_().getCoding().get(0));
            if (code != null) { compositionEntity.setClass_(code); }
            else {
                log.info("Class: Missing System/Code = "+ composition.getClass_().getCoding().get(0).getSystem() +" code = "+composition.getClass_().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ composition.getClass_().getCoding().get(0).getSystem()
                        +" code = "+composition.getClass_().getCoding().get(0).getCode());
            }
        } else {
            // Check extension for service type.
            for (Extension extension : composition.getExtension()) {
                if (extension.getUrl().contains("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-ITK-CareSettingType-1")) {
                    CodeableConcept concept = (CodeableConcept) extension.getValue();
                    ConceptEntity code = conceptDao.findAddCode(concept.getCoding().get(0));
                    if (code != null) { compositionEntity.setClass_(code); }
                }
            }
        }
        PatientEntity patientEntity = null;
        if (composition.hasSubject()) {
            log.trace(composition.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(composition.getSubject().getReference()));
            compositionEntity.setPatient(patientEntity);
        }

        if (composition.hasEncounter()) {
            EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(composition.getEncounter().getReference()));
            compositionEntity.setEncounter(encounterEntity);
        }
        if (composition.hasDate()) {
            compositionEntity.setDate(composition.getDate());
        }
        for (Reference reference : composition.getAuthor()) {
            if (reference.getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(reference.getReference()));
                compositionEntity.setAuthorPractitioner(practitionerEntity);
            }
            if (reference.getReference().contains("RelatedPerson")) {
                RelatedPersonEntity personEntity = personDao.readEntity(ctx, new IdType(reference.getReference()));
                compositionEntity.setAuthorPerson(personEntity);
            }
        }
        if (composition.hasTitle()) {
            compositionEntity.setTitle(composition.getTitle());
        }
        if (composition.hasConfidentiality()) {
            compositionEntity.setConfidentiality(composition.getConfidentiality());
        }
        if (composition.hasCustodian()) {
            OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(composition.getCustodian().getReference()));
            compositionEntity.setCustodianOrganisation(organisationEntity);
        }

        em.persist(compositionEntity);

        if (composition.getIdentifier() != null) {
            Identifier identifier = composition.getIdentifier();
            CompositionIdentifier compositionIdentifier = null;

            for (CompositionIdentifier orgSearch : compositionEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    compositionIdentifier = orgSearch;
                    break;
                }
            }
            if (compositionIdentifier == null)  compositionIdentifier = new CompositionIdentifier();

            compositionIdentifier.setValue(identifier.getValue());
            compositionIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            compositionIdentifier.setComposition(compositionEntity);
            em.persist(compositionIdentifier);
        }

        for (Composition.SectionComponent section : composition.getSection()) {
            CompositionSection sectionEntity = null;
            if (section.hasCode()) {
               log.trace("New Section Code = "+section.getCode().getCoding().get(0).getCode());
                for (CompositionSection sectionSearch :compositionEntity.getSections()) {
                    if (sectionSearch.getCode() != null) {
                        log.trace("Existing Section Code = "+sectionSearch.getCode().getCode());
                        if (section.getCode().getCoding().get(0).getCode().equals(sectionSearch.getCode().getCode())) {
                            sectionEntity = sectionSearch;
                            log.trace("Found match");
                            break;
                        }
                    }
                }
            }
            if (sectionEntity == null) {
                log.trace("Section Not Found");
                sectionEntity = new CompositionSection();
                sectionEntity.setComposition(compositionEntity);
            }
            if (section.hasCode()) {
                ConceptEntity code = conceptDao.findAddCode(section.getCode().getCoding().get(0));
                if (code != null) { sectionEntity.setCode(code); }
                else {
                    log.info("Class: Missing System/Code = "+ section.getCode().getCoding().get(0).getSystem()
                            +" code = "+section.getCode().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = "+ section.getCode().getCoding().get(0).getSystem()
                            +" code = "+section.getCode().getCoding().get(0).getCode());
                }
            }
            if (section.hasTitle()) {
                sectionEntity.setTitle(section.getTitle());
            }
            if (section.hasText()) {

                sectionEntity.setNarrative(section.getText().getDivAsString());
                sectionEntity.setNarrativeStatus(section.getText().getStatus());
            }
            if (section.hasOrderedBy()) {
                ConceptEntity code = conceptDao.findCode(section.getOrderedBy().getCoding().get(0));
                if (code != null) { sectionEntity.setOrderBy(code); }
                else {
                    log.info("Class: Missing System/Code = "+ section.getOrderedBy().getCoding().get(0).getSystem()
                            +" code = "+section.getOrderedBy().getCoding().get(0).getCode());

                    throw new IllegalArgumentException("Missing System/Code = "+ section.getOrderedBy().getCoding().get(0).getSystem()
                            +" code = "+section.getOrderedBy().getCoding().get(0).getCode());
                }
            }
            em.persist(sectionEntity);
        }

        return compositionEntityToFHIRCompositionTransformer.transform(compositionEntity);
    }

    @Override
    public List<Composition> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id, TokenParam type
            , DateRangeParam dateRange) {
        List<CompositionEntity> qryResults = searchEntity(ctx,patient, identifier, id, type, dateRange);
        List<Composition> results = new ArrayList<>();

        for (CompositionEntity compositionEntity : qryResults)
        {
            Composition composition = compositionEntityToFHIRCompositionTransformer.transform(compositionEntity);
            results.add(composition);
        }

        return results;
    }

    @Override
    public List<CompositionEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id, TokenParam type
            , DateRangeParam dateRange) {
        List<CompositionEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<CompositionEntity> criteria = builder.createQuery(CompositionEntity.class);
        Root<CompositionEntity> root = criteria.from(CompositionEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Condition> results = new ArrayList<Condition>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<CompositionEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"),patient.getIdPart());
                predList.add(p);
            } else {
                Join<CompositionEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"),-1);
                predList.add(p);
            }

        }
        if (id != null) {
            Predicate p = builder.equal(root.get("id"),id.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<CompositionEntity, CompositionIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

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

        TypedQuery<CompositionEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        qryResults = typedQuery.getResultList();
        return qryResults;
    }
}

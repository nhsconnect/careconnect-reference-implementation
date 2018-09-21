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
import uk.nhs.careconnect.ri.dao.transforms.DocumentReferenceEntityToFHIRDocumentReferenceTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceAttachment;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceAuthor;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceIdentifier;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;

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
public class DocumentReferenceDao implements DocumentReferenceRepository {

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
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    DocumentReferenceEntityToFHIRDocumentReferenceTransformer documentReferenceEntityToFHIRDocumentReferenceTransformer;


    private static final Logger log = LoggerFactory.getLogger(DocumentReferenceDao.class);
    @Override
    public void save(FhirContext ctx, DocumentReferenceEntity documentReference) {

    }

    @Override
    public DocumentReference read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            DocumentReferenceEntity documentReference = em.find(DocumentReferenceEntity.class, Long.parseLong(theId.getIdPart()));

            return documentReference != null ? documentReferenceEntityToFHIRDocumentReferenceTransformer.transform(documentReference) : null;
        } else {
            return null;
        }
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(DocumentReferenceEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public DocumentReferenceEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            DocumentReferenceEntity documentReference = em.find(DocumentReferenceEntity.class, Long.parseLong(theId.getIdPart()));

            return documentReference;
        } else {
            return null;
        }
    }

    @Override
    public DocumentReference create(FhirContext ctx, DocumentReference documentReference, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("DocumentReference.save");

        DocumentReferenceEntity documentReferenceEntity = null;

        if (documentReference.hasId()) documentReferenceEntity = readEntity(ctx, documentReference.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/documentReference")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<DocumentReferenceEntity> results = searchEntity(ctx, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/documentReference"),null, null, null,null);
                    for (DocumentReferenceEntity con : results) {
                        documentReferenceEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (documentReferenceEntity == null) {
            documentReferenceEntity = new DocumentReferenceEntity();
        }

        if (documentReference.hasStatus()) {
            documentReferenceEntity.setStatus(documentReference.getStatus());
        }

        if (documentReference.hasType()) {
            ConceptEntity code = conceptDao.findAddCode(documentReference.getType().getCoding().get(0));
            if (code != null) { documentReferenceEntity.setType(code); }
            else {
                String message = "Type: Missing System/Code = "+ documentReference.getType().getCoding().get(0).getSystem() +" code = "+documentReference.getType().getCoding().get(0).getCode();
                log.error(message);
                throw new OperationOutcomeException("DocumentReference",message, OperationOutcome.IssueType.CODEINVALID);


            }
        }

        PatientEntity patientEntity = null;
        if (documentReference.hasSubject()) {
            log.trace(documentReference.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(documentReference.getSubject().getReference()));
            documentReferenceEntity.setPatient(patientEntity);
        }

        if (documentReference.hasIndexed()) {
            documentReferenceEntity.setIndexed(documentReference.getIndexed());
        }
        if (documentReference.hasCreated()) {
            documentReferenceEntity.setCreated(documentReference.getCreated());
        }

        if (documentReference.hasCustodian()) {
            OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(documentReference.getCustodian().getReference()));
            if (organisationEntity != null) documentReferenceEntity.setCustodian(organisationEntity);
        }

        // KGM 10/4/2018 replace class with practice setting
        if (documentReference.hasContext() ) {
            if (documentReference.getContext().hasPracticeSetting()) {
                ConceptEntity code = conceptDao.findAddCode(documentReference.getContext().getPracticeSetting().getCoding().get(0));
                if (code != null) {
                    documentReferenceEntity.setContextPracticeSetting(code);
                } else {
                    String message ="PracticeSetting: Missing System/Code = " + documentReference.getContext().getPracticeSetting().getCoding().get(0).getSystem() + " code = " + documentReference.getContext().getPracticeSetting().getCoding().get(0).getCode();
                    log.error(message);
                    throw new OperationOutcomeException("DocumentReference",message, OperationOutcome.IssueType.CODEINVALID);
                }
            }
            if (documentReference.getContext().hasFacilityType()) {
                ConceptEntity code = conceptDao.findAddCode(documentReference.getContext().getFacilityType().getCoding().get(0));
                if (code != null) {
                    documentReferenceEntity.setContextFaciltityType(code);
                } else {
                    String message ="FacilityType: Missing System/Code = " + documentReference.getContext().getFacilityType().getCoding().get(0).getSystem() + " code = " + documentReference.getContext().getFacilityType().getCoding().get(0).getCode();
                    log.error(message);
                    throw new OperationOutcomeException("DocumentReference",message, OperationOutcome.IssueType.CODEINVALID);
                }
            }
            if (documentReference.getContext().hasEncounter()) {
                EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(documentReference.getContext().getEncounter().getReference()));
                if (encounterEntity!=null) documentReferenceEntity.setContextEncounter(encounterEntity);

            }
        }


        em.persist(documentReferenceEntity);

        // Remove previous links to documents.

        for (DocumentReferenceAttachment attachmentSearch : documentReferenceEntity.getAttachments() ) {
            em.remove(attachmentSearch);
        }
        for (DocumentReference.DocumentReferenceContentComponent content : documentReference.getContent()) {
            DocumentReferenceAttachment documentReferenceAttachment = null;
            /*
            for (DocumentReferenceAttachment attachmentSearch : documentReferenceEntity.getAttachments() ) {
                if (attachmentSearch.getUrl().equals(content.getAttachment().getUrl())) {
                    documentReferenceAttachment = attachmentSearch;
                    break;
                }
            }
            */
            if (documentReferenceAttachment == null ) {
                documentReferenceAttachment = new DocumentReferenceAttachment();
                documentReferenceAttachment.setDocumentReference(documentReferenceEntity);
                documentReferenceAttachment.setUrl(content.getAttachment().getUrl());
            }
            if (content.getAttachment().hasTitle()) documentReferenceAttachment.setTitle(content.getAttachment().getTitle());
            if (content.getAttachment().hasCreation()) documentReferenceAttachment.setCreation(content.getAttachment().getCreation());
            if (content.getAttachment().hasContentType()) documentReferenceAttachment.setContentType(content.getAttachment().getContentType());
            em.persist(documentReferenceAttachment);
        }

        for (Reference author : documentReference.getAuthor()) {
            DocumentReferenceAuthor documentReferenceAuthor = null;
            if (author.getReference().contains("Practitioner")) {
                PractitionerEntity practitioner = practitionerDao.readEntity(ctx, new IdType(author.getReference()));
                for (DocumentReferenceAuthor authSearch : documentReferenceEntity.getAuthors()) {
                    if (authSearch.getPractitioner() != null &&authSearch.getPractitioner().getId().equals(practitioner.getId())) {
                        documentReferenceAuthor = authSearch;
                        break;
                    }

                }
                if (documentReferenceAuthor == null) {
                    documentReferenceAuthor = new DocumentReferenceAuthor();
                    documentReferenceAuthor.setPractitioner(practitioner);
                    documentReferenceAuthor.setDocumentReference(documentReferenceEntity);
                    documentReferenceAuthor.setAuthorType(DocumentReferenceAuthor.author.Practitioner);
                    em.persist(documentReferenceAuthor);
                }
            }
            if (author.getReference().contains("Organization")) {
                OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(author.getReference()));
                for (DocumentReferenceAuthor authSearch : documentReferenceEntity.getAuthors()) {
                    if (authSearch.getOrganisation() != null && authSearch.getOrganisation().getId().equals(organisationEntity.getId())) {
                        documentReferenceAuthor = authSearch;
                        break;
                    }
                }
                if (documentReferenceAuthor == null) {
                    documentReferenceAuthor = new DocumentReferenceAuthor();
                    documentReferenceAuthor.setOrganisation(organisationEntity);
                    documentReferenceAuthor.setDocumentReference(documentReferenceEntity);
                    documentReferenceAuthor.setAuthorType(DocumentReferenceAuthor.author.Organisation);
                    em.persist(documentReferenceAuthor);
                }
            }
            if (author.getReference().contains("Patient")) {
                PatientEntity patientEntity1 = patientDao.readEntity(ctx, new IdType(author.getReference()));
                for (DocumentReferenceAuthor authSearch : documentReferenceEntity.getAuthors()) {
                    if (authSearch.getPatient() != null && authSearch.getPatient().getId().equals(patientEntity.getId())) {
                        documentReferenceAuthor = authSearch;
                        break;
                    }
                }
                if (documentReferenceAuthor == null) {
                    documentReferenceAuthor = new DocumentReferenceAuthor();
                    documentReferenceAuthor.setPatient(patientEntity);
                    documentReferenceAuthor.setDocumentReference(documentReferenceEntity);
                    documentReferenceAuthor.setAuthorType(DocumentReferenceAuthor.author.Patient);
                    em.persist(documentReferenceAuthor);
                }
            }

        }

        for (Identifier identifier : documentReference.getIdentifier()) {
            DocumentReferenceIdentifier documentReferenceIdentifier = null;

            for (DocumentReferenceIdentifier orgSearch : documentReferenceEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    documentReferenceIdentifier = orgSearch;
                    break;
                }
            }
            if (documentReferenceIdentifier == null)  documentReferenceIdentifier = new DocumentReferenceIdentifier();

            documentReferenceIdentifier.setValue(identifier.getValue());
            documentReferenceIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            documentReferenceIdentifier.setDocumentReference(documentReferenceEntity);
            em.persist(documentReferenceIdentifier);
        }



        return documentReferenceEntityToFHIRDocumentReferenceTransformer.transform(documentReferenceEntity);
    }

    @Override
    public List<DocumentReference> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id, TokenParam type
            , DateRangeParam dateRange, TokenParam setting) {
        List<DocumentReferenceEntity> qryResults = searchEntity(ctx,patient, identifier, id, type, dateRange, setting);
        List<DocumentReference> results = new ArrayList<>();

        for (DocumentReferenceEntity documentReferenceEntity : qryResults)
        {
            DocumentReference documentReference = documentReferenceEntityToFHIRDocumentReferenceTransformer.transform(documentReferenceEntity);
            results.add(documentReference);
        }

        return results;
    }

    @Override
    public List<DocumentReferenceEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id,TokenParam type
            , DateRangeParam dateRange
            , TokenParam setting) {
        List<DocumentReferenceEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<DocumentReferenceEntity> criteria = builder.createQuery(DocumentReferenceEntity.class);
        Root<DocumentReferenceEntity> root = criteria.from(DocumentReferenceEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Condition> results = new ArrayList<Condition>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<DocumentReferenceEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"),patient.getIdPart());
                predList.add(p);
            } else {
                Join<DocumentReferenceEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<DocumentReferenceEntity, DocumentReferenceIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (type!=null) {
            log.trace("Search on DocumentReference.type code = "+type.getValue());

            Join<DocumentReferenceEntity, ConceptEntity> joinConcept = root.join("type", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),type.getValue());
            predList.add(p);
        }
        if (setting!=null) {
            log.trace("Search on DocumentReference.practiceSetting code = "+setting.getValue());

            Join<DocumentReferenceEntity, ConceptEntity> joinConcept = root.join("contextPracticeSetting", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),setting.getValue());
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

        TypedQuery<DocumentReferenceEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        qryResults = typedQuery.getResultList();
        return qryResults;
    }
}

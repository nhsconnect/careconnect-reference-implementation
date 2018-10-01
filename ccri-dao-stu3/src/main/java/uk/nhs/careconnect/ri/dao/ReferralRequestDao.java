package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.dao.transforms.ReferralRequestEntityToFHIRReferralRequestTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.healthcareService.HealthcareServiceEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureIdentifier;
import uk.nhs.careconnect.ri.database.entity.referral.*;

import uk.nhs.careconnect.ri.database.entity.referral.*;

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
public class ReferralRequestDao implements ReferralRequestRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ReferralRequestEntityToFHIRReferralRequestTransformer referralRequestEntityToFHIRReferralRequestTransformer;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;


    @Autowired
    @Lazy
    EncounterRepository encounterDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    HealthcareServiceRepository serviceDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(ReferralRequestDao.class);

    @Override
    public List<ReferralRequestEntity> searchReferralRequestEntity(FhirContext ctx, TokenParam identifier, TokenOrListParam codes, StringParam id, ReferenceParam patient) {
        List<ReferralRequestEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ReferralRequestEntity> criteria = builder.createQuery(ReferralRequestEntity.class);
        Root<ReferralRequestEntity> root = criteria.from(ReferralRequestEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Procedure> results = new ArrayList<Procedure>();

      
        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<ReferralRequestEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<ReferralRequestEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (id != null) {
            Predicate p = builder.equal(root.get("id"),id.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<ReferralRequestEntity, ProcedureIdentifier> join = root.join("identifiers", JoinType.LEFT);

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

        criteria.orderBy(builder.desc(root.get("authoredOn")));

        TypedQuery<ReferralRequestEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

   
        qryResults = typedQuery.getResultList();

        return qryResults;
    }

    @Override
    public void save(FhirContext ctx, ReferralRequestEntity referralRequestEntity) {
        em.persist(referralRequestEntity);
    }

    @Override
    public ReferralRequest read(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            ReferralRequestEntity referralRequestEntity = (ReferralRequestEntity) em.find(ReferralRequestEntity.class, Long.parseLong(theId.getIdPart()));
            return referralRequestEntity == null
                    ? null
                    : referralRequestEntityToFHIRReferralRequestTransformer.transform(referralRequestEntity);

        } else {
            return null;
        }
    }

    @Override
    public ReferralRequestEntity readEntity(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            ReferralRequestEntity referralRequestEntity = (ReferralRequestEntity) em.find(ReferralRequestEntity.class, Long.parseLong(theId.getIdPart()));

            return referralRequestEntity;

        } else {
            return null;
        }
    }

    @Override
    public ReferralRequest create(FhirContext ctx, ReferralRequest referralRequest, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("ReferralRequest.save");

        ReferralRequestEntity referralRequestEntity = null;

        if (referralRequest.hasId()) referralRequestEntity = readEntity(ctx, referralRequest.getIdElement());

        if (theConditional != null) {
            try {
                if (theConditional.contains("https://tools.ietf.org/html/rfc4122")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    log.info("** Scheme = "+scheme);
                    String host = uri.getHost();
                    log.info("** Host = "+host);
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<ReferralRequestEntity> results = searchReferralRequestEntity(ctx,  new TokenParam().setValue(spiltStr[1]).setSystem("https://tools.ietf.org/html/rfc4122"),null, null, null);
                    for (ReferralRequestEntity con : results) {
                        referralRequestEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (referralRequestEntity == null) {
            referralRequestEntity = new ReferralRequestEntity();
        }

        if (referralRequest.hasStatus() ) {
            referralRequestEntity.setStatus(referralRequest.getStatus());
        }

        if (referralRequest.hasIntent() ) {
            referralRequestEntity.setIntent(referralRequest.getIntent());
        }

        if (referralRequest.hasPriority() ) {
            referralRequestEntity.setPriority(referralRequest.getPriority());
        }

        if (referralRequest.hasAuthoredOn()) {
            referralRequestEntity.setAuthoredOn(referralRequest.getAuthoredOn());
        }
        
        if (referralRequest.hasType()) {
            ConceptEntity code = conceptDao.findAddCode(referralRequest.getType().getCoding().get(0));
            if (code != null) { referralRequestEntity.setType(code); }
            else {
                log.info("Type: Missing System/Code = "+ referralRequest.getType().getCoding().get(0).getSystem() +" code = "+referralRequest.getType().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ referralRequest.getType().getCoding().get(0).getSystem()
                        +" code = "+referralRequest.getType().getCoding().get(0).getCode());
            }
        }

        if (referralRequest.hasRequester()) {
            if (referralRequest.getRequester().hasAgent()) {
                if (referralRequest.getRequester().getAgent().getReference().contains("Practitioner")) {
                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(referralRequest.getRequester().getAgent().getReference()));
                    if (practitionerEntity != null) {
                        referralRequestEntity.setRequesterPractitioner(practitionerEntity);
                    }
                } else if (referralRequest.getRequester().getAgent().getReference().contains("Organization")) {
                    OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(referralRequest.getRequester().getAgent().getReference()));
                    if (organisationEntity != null) {
                        referralRequestEntity.setRequesterOrganisation(organisationEntity);
                    }
                } else if (referralRequest.getRequester().getAgent().getReference().contains("Patient")) {
                    PatientEntity patientEntity= patientDao.readEntity(ctx, new IdType(referralRequest.getRequester().getAgent().getReference()));
                    if (patientEntity != null) {
                        referralRequestEntity.setRequesterPatient(patientEntity);
                    }
                }
            }
            if (referralRequest.getRequester().hasOnBehalfOf()) {
                if (referralRequest.getRequester().getOnBehalfOf().getReference().contains("Organization")) {
                    OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(referralRequest.getRequester().getOnBehalfOf().getReference()));
                    if (organisationEntity != null) {
                        referralRequestEntity.setOnBehalfOrganisation(organisationEntity);
                    }
                }
            }
        }

        PatientEntity patientEntity = null;
        if (referralRequest.hasSubject()) {
            log.trace(referralRequest.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(referralRequest.getSubject().getReference()));
            referralRequestEntity.setPatient(patientEntity);
        }

        EncounterEntity encounterEntity = null;
        if (referralRequest.hasContext()) {
            encounterEntity = encounterDao.readEntity(ctx, new IdType(referralRequest.getContext().getReference()));
            referralRequestEntity.setContextEncounter(encounterEntity);
        }
        
        em.persist(referralRequestEntity);

        for (Identifier identifier : referralRequest.getIdentifier()) {
            ReferralRequestIdentifier referralRequestIdentifier = null;

            for (ReferralRequestIdentifier orgSearch : referralRequestEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    referralRequestIdentifier = orgSearch;
                    break;
                }
            }
            if (referralRequestIdentifier == null)  referralRequestIdentifier = new ReferralRequestIdentifier();

            referralRequestIdentifier.setValue(identifier.getValue());
            referralRequestIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            referralRequestIdentifier.setReferralRequest(referralRequestEntity);
            em.persist(referralRequestIdentifier);
        }

        for (CodeableConcept concept :referralRequest.getReasonCode()) {
            // Category must have a code 15/Jan/2018 testing with Synthea examples
            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    ReferralRequestReason reason = null;
                    // Look for existing categories
                    for (ReferralRequestReason cat :referralRequestEntity.getReasons()) {
                        if (cat.getReason().getCode().equals(concept.getCodingFirstRep().getCode())) reason= cat;
                    }
                    if (reason == null) reason = new ReferralRequestReason();

                    reason.setReason(conceptEntity);
                    reason.setReferralRequest(referralRequestEntity);
                    em.persist(reason);
                    referralRequestEntity.getReasons().add(reason);
                }
                else {
                    log.info("Missing Reason. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }

        for (CodeableConcept concept :referralRequest.getServiceRequested()) {
            // Category must have a code 15/Jan/2018 testing with Synthea examples
            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    ReferralRequestServiceRequested service = null;
                    // Look for existing categories
                    for (ReferralRequestServiceRequested cat :referralRequestEntity.getServices()) {
                        if (cat.getService().getCode().equals(concept.getCodingFirstRep().getCode())) service= cat;
                    }
                    if (service == null) service = new ReferralRequestServiceRequested();

                    service.setService(conceptEntity);
                    service.setReferralRequest(referralRequestEntity);
                    em.persist(service);
                    referralRequestEntity.getServices().add(service);
                }
                else {
                    log.info("Missing ServiceRequested. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }
        for (ReferralRequestRecipient search : referralRequestEntity.getRecipients()) {
           // TODO clear any old entries
        }
        for (Reference reference : referralRequest.getRecipient()) {
            ReferralRequestRecipient requestRecipient = new ReferralRequestRecipient();
            if (reference.getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(reference.getReference()));
                if (practitionerEntity != null) {
                    requestRecipient.setPractitioner(practitionerEntity);
                }
            } else if (referralRequest.getRequester().getAgent().getReference().contains("Organization")) {
                OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(reference.getReference()));
                if (organisationEntity != null) {
                    requestRecipient.setOrganisation(organisationEntity);
                }
            } else if (referralRequest.getRequester().getAgent().getReference().contains("HealthcareService")) {
                HealthcareServiceEntity service = serviceDao.readEntity(ctx, new IdType(reference.getReference()));
                if (service != null) {
                    requestRecipient.setService(service);
                }
            }

            em.persist(requestRecipient);

        }
        
        return referralRequestEntityToFHIRReferralRequestTransformer.transform(referralRequestEntity);

    }

    @Override
    public List<ReferralRequest> searchReferralRequest(FhirContext ctx, TokenParam identifier, TokenOrListParam codes, StringParam id,ReferenceParam patient) {
        List<ReferralRequestEntity> qryResults = searchReferralRequestEntity(ctx,identifier,codes,id,patient);
        List<ReferralRequest> results = new ArrayList<>();

        for (ReferralRequestEntity referralRequestEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            ReferralRequest referralRequest = referralRequestEntityToFHIRReferralRequestTransformer.transform(referralRequestEntity);
            results.add(referralRequest);
        }

        return results;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ReferralRequestEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }
}

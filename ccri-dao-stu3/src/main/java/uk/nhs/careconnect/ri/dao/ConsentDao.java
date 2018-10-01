package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
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
import uk.nhs.careconnect.ri.dao.transforms.ConsentEntityToFHIRConsentTransformer;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.database.entity.consent.*;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaireResponse.QuestionnaireResponseEntity;
import uk.nhs.careconnect.ri.database.entity.consent.*;
import uk.nhs.careconnect.ri.database.entity.relatedPerson.RelatedPersonEntity;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class ConsentDao implements ConsentRepository {

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
    ConditionRepository conditionDao;

    @Autowired
    AllergyIntoleranceRepository allergyDao;

    @Autowired
    RiskAssessmentRepository riskDao;

    @Autowired
    ObservationRepository observationDao;

    @Autowired
    QuestionnaireResponseRepository formDao;

    @Autowired
    DocumentReferenceRepository documentDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    CareTeamRepository teamDao;

    @Autowired
    RelatedPersonRepository personDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;
    
    @Autowired
    ConsentEntityToFHIRConsentTransformer consentEntityToFHIRConsentTransformer;

    private static final Logger log = LoggerFactory.getLogger(ConsentDao.class);

    @Override
    public void save(FhirContext ctx, ConsentEntity
            consent) throws OperationOutcomeException {

    }

    @Override
    public Consent read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ConsentEntity consent = (ConsentEntity) em.find(ConsentEntity.class, Long.parseLong(theId.getIdPart()));
            return consentEntityToFHIRConsentTransformer.transform(consent);
        }
        return null;
    }

    @Override
    public Consent create(FhirContext ctx, Consent consent, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("Consent.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        ConsentEntity consentEntity = null;

        if (consent.hasId()) consentEntity = readEntity(ctx, consent.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/consent")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<ConsentEntity> results = searchEntity(ctx, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/consent"),null);
                    for (ConsentEntity con : results) {
                        consentEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (consentEntity == null) consentEntity = new ConsentEntity();

        if (consent.hasStatus()) {
            consentEntity.setStatus(consent.getStatus());
        }

        PatientEntity patientEntity = null;
        if (consent.hasPatient()) {
            log.trace(consent.getPatient().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(consent.getPatient().getReference()));
            consentEntity.setPatient(patientEntity);
        }


        if (consent.hasPeriod() ) {
            try {
                consentEntity.setPeriodEndDateTime(consent.getPeriod().getEnd());
            } catch (Exception ex) {

            }
            try {
                consentEntity.setPeriodStartDateTime(consent.getPeriod().getStart());
            } catch (Exception ex) {

            }
        }
        if (consent.hasDateTime()) {
            try {
                consentEntity.setDateTime(consent.getDateTime());
            } catch (Exception ex) {

            }
        }

        if (consent.hasSourceIdentifier()) {
            try {
                consentEntity.setSourceSystem(consent.getSourceIdentifier().getSystem());
                consentEntity.setSourceValue(consent.getSourceIdentifier().getValue());
            } catch (Exception ex) {

            }
        }
        if (consent.hasSourceReference()) {
            try {
                if (consent.getSourceReference().getReference().contains("QuestionnaireResponse")) {
                    QuestionnaireResponseEntity questionnaireResponseEntity = formDao.readEntity(ctx, new IdType(consent.getSourceReference().getReference()));
                    consentEntity.setForm(questionnaireResponseEntity);
                }
                if (consent.getSourceReference().getReference().contains("DocumentReference")) {
                    DocumentReferenceEntity documentReferenceEntity = documentDao.readEntity(ctx, new IdType(consent.getSourceReference().getReference()));
                    consentEntity.setDocument(documentReferenceEntity);
                }
                consentEntity.setSourceSystem(consent.getSourceIdentifier().getSystem());
                consentEntity.setSourceValue(consent.getSourceIdentifier().getValue());
            } catch (Exception ex) {

            }
        }

        if (consent.hasPolicyRule()) {
            consentEntity.setPolicyRule(consent.getPolicyRule());
        }

        em.persist(consentEntity);


        if (consent.hasIdentifier()) {
            Identifier identifier = consent.getIdentifier();

            ConsentIdentifier
                    consentIdentifier = null;

            for (ConsentIdentifier orgSearch : consentEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    consentIdentifier = orgSearch;
                    break;
                }
            }
            if (consentIdentifier == null)  consentIdentifier = new ConsentIdentifier();

            consentIdentifier.setValue(identifier.getValue());
            consentIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            consentIdentifier.setConsent(consentEntity);
            em.persist(consentIdentifier);

        }

        // Action

        for (ConsentAction consentAction : consentEntity.getActions()) {
            em.remove(consentAction);
        }
        for (CodeableConcept action : consent.getAction()) {
            ConsentAction consentAction = new ConsentAction();
            consentAction.setConsent(consentEntity);
            ConceptEntity concept = conceptDao.findAddCode(action.getCodingFirstRep());
            if (concept != null) consentAction.setActionCode(concept);

            em.persist(consentAction);
        }


        // Actor

        for (ConsentActor actor : consentEntity.getActors()) {
            em.remove(actor);
        }
        for (Consent.ConsentActorComponent actor : consent.getActor()) {
            ConsentActor consentActor = new ConsentActor();
            consentActor.setConsent(consentEntity);
            if (actor.hasRole()) {
                ConceptEntity concept = conceptDao.findAddCode(actor.getRole().getCodingFirstRep());
                if (concept != null) consentActor.setRoleCode(concept);
            }
            if (actor.hasReference()) {
                if (actor.getReference().getReference().contains("CareTeam")) {
                    CareTeamEntity careTeamEntity = teamDao.readEntity(ctx, new IdType(actor.getReference().getReference()));
                    consentActor.setReferenceCareTeam(careTeamEntity);
                }
                if (actor.getReference().getReference().contains("Organization")) {
                    OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(actor.getReference().getReference()));
                    consentActor.setReferenceOrganisation(organisationEntity);
                }
                if (actor.getReference().getReference().contains("Patient")) {
                    PatientEntity patientEntity1 = patientDao.readEntity(ctx, new IdType(actor.getReference().getReference()));
                    consentActor.setReferencePatient(patientEntity1);
                }
                if (actor.getReference().getReference().contains("Practitioner")) {
                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(actor.getReference().getReference()));
                    consentActor.setReferencePractitioner(practitionerEntity);
                }
                if (actor.getReference().getReference().contains("RelatedPerson")) {
                        RelatedPersonEntity relatedPersonEntity = personDao.readEntity(ctx, new IdType(actor.getReference().getReference()));
                        consentActor.setReferencePerson(relatedPersonEntity);
                }
            }

            em.persist(consentActor);
        }


        // Category

        for (ConsentCategory consentCategory : consentEntity.getCategories()) {
            em.remove(consentCategory);
        }
        for (CodeableConcept category : consent.getCategory()) {
            ConsentCategory consentCategory = new ConsentCategory();
            consentCategory.setConsent(consentEntity);
            ConceptEntity concept = conceptDao.findAddCode(category.getCodingFirstRep());
            if (concept != null) consentCategory.setCategory(concept);

            em.persist(consentCategory);
        }

        // Organisation
        for (ConsentOrganisation consentOrganisation : consentEntity.getOrganisations()) {
            em.remove(consentOrganisation);
        }
        for (Reference reference : consent.getOrganization()) {
            ConsentOrganisation consentOrganisation = new ConsentOrganisation();
            consentOrganisation.setConsent(consentEntity);

            OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(reference.getReference()));
            consentOrganisation.setOrganisation(organisationEntity);

            em.persist(consentOrganisation);
        }
        // Party
        for (ConsentParty party : consentEntity.getParties()) {
            em.remove(party);
        }
        for (Reference reference : consent.getConsentingParty()) {
            ConsentParty consentParty = new ConsentParty();
            consentParty.setConsent(consentEntity);
            if (reference.hasReference()) {
                if (reference.getReference().contains("Organization")) {
                        OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(reference.getReference()));
                        consentParty.setReferenceOrganisation(organisationEntity);
                }
                if (reference.getReference().contains("Patient")) {
                    PatientEntity patientEntity1 = patientDao.readEntity(ctx, new IdType(reference.getReference()));
                    consentParty.setReferencePatient(patientEntity1);
                }
                if (reference.getReference().contains("Practitioner")) {
                    PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(reference.getReference()));
                    consentParty.setReferencePractitioner(practitionerEntity);
                }
                if (reference.getReference().contains("RelatedPerson")) {
                        RelatedPersonEntity relatedPersonEntity = personDao.readEntity(ctx, new IdType(reference.getReference()));
                        consentParty.setReferencePerson(relatedPersonEntity);
                }
            }
            em.persist(consentParty);
        }
        // Policy

        for (ConsentPolicy policy : consentEntity.getPolicies()) {
            em.remove(policy);
        }
        for (Consent.ConsentPolicyComponent policy : consent.getPolicy()) {
            ConsentPolicy policyEntity = new ConsentPolicy();
            policyEntity.setConsent(consentEntity);
            if (policy.hasAuthority()) {
                policyEntity.setAuthority(policy.getAuthority());
            }
            if (policy.hasUri()) {
                policyEntity.setPolicyUri(policy.getUri());
            }
            em.persist(policyEntity);
        }

        // Purpose

        for (ConsentPurpose consentPurpose: consentEntity.getPurposes()) {
            em.remove(consentPurpose);
        }
        for (Coding coding : consent.getPurpose()) {
            ConsentPurpose consentPurpose = new ConsentPurpose();
            consentPurpose.setConsent(consentEntity);
            ConceptEntity concept = conceptDao.findAddCode(coding);
            if (concept != null) consentPurpose.setPurpose(concept);

            em.persist(consentPurpose);
        }

        return consentEntityToFHIRConsentTransformer.transform(consentEntity);
    }

    @Override
    public ConsentEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ConsentEntity consentIntolerance = (ConsentEntity) em.find(ConsentEntity.class, Long.parseLong(theId.getIdPart()));
            return consentIntolerance;
        }
        return null;
    }

    @Override
    public List<Consent> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id) {
        List<ConsentEntity> qryResults = searchEntity(ctx,patient, identifier,id);
        List<Consent> results = new ArrayList<>();

        for (ConsentEntity consentIntoleranceEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Consent consent = consentEntityToFHIRConsentTransformer.transform(consentIntoleranceEntity);
            results.add(consent);
        }

        return results;
    }

    @Override
    public List<ConsentEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam resid) {
        List<ConsentEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ConsentEntity> criteria = builder.createQuery(ConsentEntity.class);
        Root<ConsentEntity> root = criteria.from(ConsentEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Consent> results = new ArrayList<Consent>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<ConsentEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<ConsentEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<ConsentEntity, ConsentIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        
        ParameterExpression<Date> parameterLower = builder.parameter(Date.class);
        ParameterExpression<Date> parameterUpper = builder.parameter(Date.class);

        

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


        TypedQuery<ConsentEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);
        
        qryResults = typedQuery.getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ConsentEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }
}

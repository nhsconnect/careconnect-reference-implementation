package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.carePlan.CarePlanCondition;
import uk.nhs.careconnect.ri.database.entity.claim.*;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.questionnaire.QuestionnaireEntity;
import uk.nhs.careconnect.ri.stu3.dao.transforms.ClaimEntityToFHIRClaim;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class ClaimDao implements ClaimRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ClaimEntityToFHIRClaim claimEntityToFHIRClaim;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    ConditionRepository conditionDao;


    @Autowired
    private LibDao libDao;

    private static final Logger log = LoggerFactory.getLogger(ClaimDao.class);


    @Override
    public void save(FhirContext ctx, ClaimEntity claim) throws OperationOutcomeException {

    }

    @Override
    public Claim read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ClaimEntity claim = (ClaimEntity) em.find(ClaimEntity.class, Long.parseLong(theId.getIdPart()));
            return claimEntityToFHIRClaim.transform(claim, ctx);
        }
        return null;
    }

    @Override
    public ClaimEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ClaimEntity claim = (ClaimEntity) em.find(ClaimEntity.class, Long.parseLong(theId.getIdPart()));
            return claim;
        }
        return null;
    }

    @Override
    public ClaimEntity readEntity(FhirContext ctx, TokenParam identifier) {
        List<ClaimEntity> claimEntities = searchEntity(ctx,null,identifier,null, null, null);
        for (ClaimEntity claimEntity : claimEntities) {
            return claimEntity;
        }
        return null;
    }

    @Override
    public Claim create(FhirContext ctx, Claim claim, IdType theId, String theConditional) throws OperationOutcomeException {
        ClaimEntity claimEntity = null;

        if (claim.hasId()) claimEntity = readEntity(ctx, claim.getIdElement());

        if (claim.getIdentifier().size()== 0 && theId != null) {
            throw new PreconditionFailedException("Business rule violation. At least one identifier needs to be supplied when updating a resource");
        }

        List<ClaimEntity> entries = searchEntity(ctx
                , null
                , new TokenParam().setSystem(claim.getIdentifierFirstRep().getSystem()).setValue(claim.getIdentifierFirstRep().getValue())
                ,null
                ,null
                ,null
        );
        for (ClaimEntity msg : entries) {
            if (claim.getId() == null) {
                throw new ResourceVersionConflictException("Claim is already present on the system "+ msg.getId() + ". Update existing claim.");
            }

            if (!msg.getId().toString().equals(claim.getIdElement().getIdPart())) {
                throw new ResourceVersionConflictException("Claim is already present on the system with a different Id "+ msg.getId() + ". Update existing claim.");
            }
        }

        if (claimEntity == null) claimEntity = new ClaimEntity();


        PatientEntity patientEntity = null;
        if (claim.hasPatient()) {
            if (claim.getPatient().hasReference()) {
                log.trace(claim.getPatient().getReference());
                patientEntity = patientDao.readEntity(ctx, new IdType(claim.getPatient().getReference()));

            }
            if (claim.getPatient().hasIdentifier()) {
                // This copes with reference.identifier param (a short cut?)
                log.trace(claim.getPatient().getIdentifier().getSystem() + " " + claim.getPatient().getIdentifier().getValue());
                patientEntity = patientDao.readEntity(ctx, new TokenParam().setSystem(claim.getPatient().getIdentifier().getSystem()).setValue(claim.getPatient().getIdentifier().getValue()));
            }
            if (patientEntity != null ) {
                claimEntity.setPatient(patientEntity);
            } else {
                throw new ResourceNotFoundException("Patient reference was not found");
            }
        }

        if (claim.hasStatus()) {
            claimEntity.setStatus(claim.getStatus());
        }

        if (claim.hasBillablePeriod()) {
            if (claim.getBillablePeriod().hasStart()) {
                claimEntity.setPeriodStart(claim.getBillablePeriod().getStart());
            }
            if (claim.getBillablePeriod().hasEnd()) {
                claimEntity.setPeriodEnd(claim.getBillablePeriod().getEnd());
            }
        }

        if (claim.hasCreated()) {
            claimEntity.setCreated(claim.getCreated());
        }
        if (claim.hasExtension()) {
            log.info("has Extension");
            for (Extension ext : claim.getExtension()) {
                log.info(ext.getUrl());
                if (ext.getUrl().equals("https://fhir.gov.uk/Extension/claimEntererPatient")) {
                    log.info("processing EntererPatient Extension");

                    Reference ref = (Reference) ext.getValue();
                    patientEntity = null;
                    if (ref.hasReference()) {
                        log.trace(ref.getReference());
                        patientEntity = patientDao.readEntity(ctx, new IdType(ref.getReference()));

                    }
                    if (ref.hasIdentifier()) {
                        // This copes with reference.identifier param (a short cut?)
                        log.trace(ref.getIdentifier().getSystem() + " " + ref.getIdentifier().getValue());
                        patientEntity = patientDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                    }
                    if (patientEntity != null) {
                        claimEntity.setEntererPatient(patientEntity);
                    }
                }
            }
        }
        if (claim.hasEnterer() ) {

                Reference ref = claim.getEnterer();
                PractitionerEntity practitionerEntity = null;
                if (ref.hasReference()) {
                    log.trace(ref.getReference());
                    practitionerEntity = practitionerDao.readEntity(ctx, new IdType(ref.getReference()));

                }
                if (ref.hasIdentifier()) {
                    // This copes with reference.identifier param (a short cut?)
                    log.trace(ref.getIdentifier().getSystem() + " " + ref.getIdentifier().getValue());
                    practitionerEntity = practitionerDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
                }
                if (practitionerEntity != null) {
                    claimEntity.setEntererPractitioner(practitionerEntity);
                }

        }
        if (claim.hasOrganization()) {

            Reference ref = claim.getOrganization();
            OrganisationEntity organisationEntity = null;
            if (ref.hasReference()) {
                log.trace(ref.getReference());
                organisationEntity = organisationDao.readEntity(ctx, new IdType(ref.getReference()));

            }
            if (ref.hasIdentifier()) {
                // This copes with reference.identifier param (a short cut?)
                log.trace(ref.getIdentifier().getSystem() + " " + ref.getIdentifier().getValue());
                organisationEntity = organisationDao.readEntity(ctx, new TokenParam().setSystem(ref.getIdentifier().getSystem()).setValue(ref.getIdentifier().getValue()));
            }
            if (organisationEntity != null) {
                claimEntity.setProviderOrganisation(organisationEntity);
            }
        }


        String resource = ctx.newJsonParser().encodeResourceToString(claim);
        claimEntity.setResource(resource);

        if (claim.hasType()) {
            ClaimType claimType = claimEntity.getType();
            if (claimType == null) {
                claimType = new ClaimType();
                log.info("Claim Id = "+claimEntity.getId());
            }
            claimType.setConceptCode(null);
            if (claim.getType().hasCoding()) {
                ConceptEntity code = conceptDao.findCode(claim.getType().getCoding().get(0));
                if (code != null) {
                    claimType.setConceptCode(code);
                } else {

                    throw new PreconditionFailedException("Missing System/Code = " + claim.getType().getCoding().get(0).getSystem() + " code = " + claim.getType().getCoding().get(0).getCode());
                }
            }
            if (claim.getType().hasText()) {
                claimType.setConceptText(claim.getType().getText());
            }
           em.persist(claimType);
           claimEntity.setType(claimType);
        }

        if (claim.hasSubType()) {
            ClaimSubType
                    claimSubType = claimEntity.getSubType();
            if (claimSubType == null) {
                claimSubType = new ClaimSubType();
                log.info("Claim Id = "+claimEntity.getId());
            }
            claimSubType.setConceptCode(null);
            if (claim.getSubTypeFirstRep().hasCoding() && claim.getSubTypeFirstRep().getCodingFirstRep().hasSystem()) {
                ConceptEntity code = conceptDao.findCode(claim.getSubTypeFirstRep().getCoding().get(0));
                if (code != null) {
                    claimSubType.setConceptCode(code);
                } else {

                    throw new PreconditionFailedException("Missing System/Code = " + claim.getSubTypeFirstRep().getCoding().get(0).getSystem() + " code = " + claim.getSubTypeFirstRep().getCoding().get(0).getCode());
                }
            }
            if (claim.getType().hasText()) {
                claimSubType.setConceptText(claim.getSubTypeFirstRep().getText());
            }
            em.persist(claimSubType);
            claimEntity.setSubType(claimSubType);
        }

        if (claim.hasPriority()) {
            ClaimPriority
                    claimPriority = claimEntity.getPriority();
            if (claimPriority == null) {
                claimPriority= new ClaimPriority();
                log.info("Claim Id = "+claimEntity.getId());
            }
            claimPriority.setConceptCode(null);
            if (claim.getPriority().hasCoding() && claim.getPriority().getCodingFirstRep().hasSystem() ) {
                ConceptEntity code = conceptDao.findAddCode(claim.getPriority().getCoding().get(0));
                if (code != null) {
                    claimPriority.setConceptCode(code);
                } else {

                    throw new PreconditionFailedException("Missing System/Code = " + claim.getPriority().getCoding().get(0).getSystem() + " code = " + claim.getPriority().getCoding().get(0).getCode());
                }
            }
            if (claim.getPriority().hasText()) {
                claimPriority.setConceptText(claim.getPriority().getText());
            }
            em.persist(claimPriority);
            claimEntity.setPriority(claimPriority);
        }
        em.persist(claimEntity);

        for (Identifier identifier : claim.getIdentifier()) {
            ClaimIdentifier claimIdentifier = null;

            for (ClaimIdentifier orgSearch : claimEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    claimIdentifier = orgSearch;
                    break;
                }
            }
            if (claimIdentifier == null)  claimIdentifier = new ClaimIdentifier();

            claimIdentifier= (ClaimIdentifier) libDao.setIdentifier(identifier, claimIdentifier );
            claimIdentifier.setClaim(claimEntity);
            em.persist(claimIdentifier);
        }

        for (ClaimDiagnosis claimDiagnosis : claimEntity.getDiagnoses()) {
            em.remove(claimDiagnosis);
        }

        for (Claim.DiagnosisComponent diagnosis : claim.getDiagnosis()) {
            ClaimDiagnosis claimDiagnosis = new ClaimDiagnosis();
            claimDiagnosis.setClaim(claimEntity);

            if (diagnosis.hasDiagnosisReference()) {
                if (diagnosis.getDiagnosisReference().hasReference()) {
                    ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new IdType(diagnosis.getDiagnosisReference().getReference()));
                    if (conditionEntity == null) throw new ResourceNotFoundException("Condition not found");
                    claimDiagnosis.setCondition(conditionEntity);
                }
                if (diagnosis.getDiagnosisReference().hasIdentifier()) {
                    ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new TokenParam()
                            .setSystem(diagnosis.getDiagnosisReference().getIdentifier().getSystem())
                            .setValue(diagnosis.getDiagnosisReference().getIdentifier().getValue())
                    );
                    if (conditionEntity == null) throw new ResourceNotFoundException("Condition not found");
                    claimDiagnosis.setCondition(conditionEntity);
                }
            }

            em.persist(claimDiagnosis);
        }
        log.debug("Claim.saveCategory");

        for(ClaimRelated claimRelated : claimEntity.getRelatedClaims()) {
            em.remove(claimRelated);
        }
        for (Claim.RelatedClaimComponent component : claim.getRelated()) {
            ClaimRelated claimRelated = new ClaimRelated();
            claimRelated.setClaim(claimEntity);
            ClaimEntity relatedClaim = null;
            if (component.hasClaim() ) {
                if (component.getClaim().hasReference()) {
                    relatedClaim = readEntity(ctx, new IdType(component.getClaim().getReference()));
                }
                if (component.getClaim().hasIdentifier()) {
                    // This copes with reference.identifier param (a short cut?)
                    log.trace(component.getClaim().getIdentifier().getSystem() + " " + component.getClaim().getIdentifier().getValue());
                    relatedClaim = readEntity(ctx, new TokenParam().setSystem(component.getClaim().getIdentifier().getSystem()).setValue(component.getClaim().getIdentifier().getValue()));
                }
                if (relatedClaim != null) {
                    claimRelated.setRelatedClaim(relatedClaim);
                } else {
                    throw new ResourceNotFoundException("Focus reference was not found");
                }
            }
            if (component.hasRelationship()) {
                if (component.getRelationship().hasCoding() && component.getRelationship().getCodingFirstRep().hasSystem()) {
                    ConceptEntity code = conceptDao.findAddCode(component.getRelationship().getCoding().get(0));
                    if (code != null) {
                        claimRelated.setConceptCode(code);
                    } else {

                        throw new PreconditionFailedException("Missing System/Code = " + claim.getPriority().getCoding().get(0).getSystem() + " code = " + claim.getPriority().getCoding().get(0).getCode());
                    }
                }
                if (component.getRelationship().hasText()) {
                    claimRelated.setConceptText(component.getRelationship().getText());
                }
            }

            em.persist(claimRelated);
        }

        claim.setId(claimEntity.getId().toString());

        return claim;
    }

    @Override
    public List<Resource> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id
            , @OptionalParam(name = Claim.SP_USE) TokenParam use
            , @OptionalParam(name = "status") TokenParam status) {

        List<ClaimEntity> qryResults =  searchEntity(ctx,patient, identifier,id, use, status);
        List<Resource> results = new ArrayList<>();

        for (ClaimEntity claimIntoleranceEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Claim claim = claimEntityToFHIRClaim.transform(claimIntoleranceEntity, ctx);
            results.add(claim);
        }

        return results;
    }

    @Override
    public List<ClaimEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam resid
            , @OptionalParam(name = Claim.SP_USE) TokenParam use
            , @OptionalParam(name = "status") TokenParam status
    ) {
        List<ClaimEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ClaimEntity> criteria = builder.createQuery(ClaimEntity.class);
        Root<ClaimEntity> root = criteria.from(ClaimEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Claim> results = new ArrayList<Claim>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<ClaimEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<ClaimEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<ClaimEntity, ClaimIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        if (status != null) {
            Integer taskstatus = null;

            switch (status.getValue().toLowerCase()) {
                case "active":
                    taskstatus = 0;
                    break;
                case "cancelled":
                    taskstatus = 1;
                    break;
                case "draft":
                    taskstatus = 2;
                    break;
                case "entered-in-error":
                    taskstatus = 3;
                    break;

                default:
                    taskstatus=-1;
            }


            Predicate p = builder.equal(root.get("status"), taskstatus);
            predList.add(p);

        }
        if (use != null) {
            Integer taskstatus = null;

            switch (use.getValue().toLowerCase()) {
                case "complete":
                    taskstatus = 0;
                    break;
                case "proposed":
                    taskstatus = 1;
                    break;
                case "exploratory":
                    taskstatus = 2;
                    break;
                case "other":
                    taskstatus = 3;
                    break;

                default:
                    taskstatus=-1;
            }


            Predicate p = builder.equal(root.get("use"), taskstatus);
            predList.add(p);

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
        criteria.orderBy(builder.desc(root.get("created")));

        TypedQuery<ClaimEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        qryResults = typedQuery.getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(ClaimEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }
}

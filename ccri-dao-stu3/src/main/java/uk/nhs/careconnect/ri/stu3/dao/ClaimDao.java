package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.ClaimRepository;
import uk.nhs.careconnect.ri.database.daointerface.ConceptRepository;
import uk.nhs.careconnect.ri.database.daointerface.PatientRepository;
import uk.nhs.careconnect.ri.database.daointerface.PractitionerRepository;
import uk.nhs.careconnect.ri.database.entity.claim.ClaimEntity;
import uk.nhs.careconnect.ri.database.entity.claim.ClaimIdentifier;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;
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
    PractitionerRepository practitionerDao;


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
    public Claim create(FhirContext ctx, Claim claim, IdType theId, String theConditional) throws OperationOutcomeException {
        ClaimEntity claimEntity = null;

        if (claim.hasId()) claimEntity = readEntity(ctx, claim.getIdElement());

        

        if (claimEntity == null) claimEntity = new ClaimEntity();


        PatientEntity patientEntity = null;
        if (claim.hasPatient()) {
            if (claim.getPatient().hasReference()) {
                log.trace(claim.getPatient().getReference());
                patientEntity = patientDao.readEntity(ctx, new IdType(claim.getPatient().getReference()));
                claimEntity.setPatient(patientEntity);
            }
            if (claim.getPatient().hasIdentifier()) {
                // TODO KGM
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

        String resource = ctx.newJsonParser().encodeResourceToString(claim);
        claimEntity.setResource(resource);

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
        log.debug("Claim.saveCategory");

        claim.setId(claimEntity.getId().toString());

        return claim;
    }

    @Override
    public List<Resource> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam id) {

        List<ClaimEntity> qryResults =  searchEntity(ctx,patient, identifier,id);
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
    public List<ClaimEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, StringParam resid) {
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

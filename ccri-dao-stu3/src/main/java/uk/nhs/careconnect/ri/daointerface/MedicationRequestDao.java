package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.nhs.careconnect.ri.daointerface.transforms.MedicationRequestEntityToFHIRMedicationRequestTransformer;

import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.immunisation.ImmunisationEntity;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestEntity;
import uk.nhs.careconnect.ri.entity.medication.MedicationRequestIdentifier;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class MedicationRequestDao implements MedicationRequestRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private MedicationRequestEntityToFHIRMedicationRequestTransformer
            medicationRequestEntityToFHIRMedicationRequestTransformer;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private ConceptRepository codeSvc;


    private static final Logger log = LoggerFactory.getLogger(MedicationRequestDao.class);

    @Override
    public void save(FhirContext ctx, MedicationRequestEntity prescription) {

    }

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(MedicationRequestEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public MedicationRequestEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationRequestEntity medicationRequestEntity = (MedicationRequestEntity) em.find(MedicationRequestEntity.class, Long.parseLong(theId.getIdPart()));

            return medicationRequestEntity ;
        } else {
            return null;
        }
    }


    @Override
    public MedicationRequest read(FhirContext ctx,IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            MedicationRequestEntity medicationRequestEntity = (MedicationRequestEntity) em.find(MedicationRequestEntity.class, Long.parseLong(theId.getIdPart()));

            return medicationRequestEntity == null
                    ? null
                    : medicationRequestEntityToFHIRMedicationRequestTransformer.transform(medicationRequestEntity);
        } else {
            return null;
        }
    }

    @Override
    public MedicationRequest create(FhirContext ctx,MedicationRequest prescription, IdType theId, String theConditional) {
        return null;
    }

    @Override
    public List<MedicationRequest> search(FhirContext ctx, ReferenceParam patient, TokenParam code, DateRangeParam dateWritten, TokenParam status, TokenParam identifier) {
        List<MedicationRequestEntity> qryResults = searchEntity(ctx, patient, code, dateWritten, status, identifier);
        List<MedicationRequest> results = new ArrayList<>();

        for (MedicationRequestEntity medicationRequestEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            MedicationRequest medicationRequest =  medicationRequestEntityToFHIRMedicationRequestTransformer.transform(medicationRequestEntity);
            results.add(medicationRequest);
        }
        return results;
    }

    @Override
    public List<MedicationRequestEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam code, DateRangeParam dateWritten, TokenParam status, TokenParam identifier) {
        List<MedicationRequestEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<MedicationRequestEntity> criteria = builder.createQuery(MedicationRequestEntity.class);
        Root<MedicationRequestEntity> root = criteria.from(MedicationRequestEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<MedicationRequest> results = new ArrayList<MedicationRequest>();

        if (patient != null) {
            Join<MedicationRequestEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

            Predicate p = builder.equal(join.get("id"),patient.getIdPart());
            predList.add(p);
        }

        if (identifier !=null)
        {
            Join<MedicationRequestEntity, MedicationRequestIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (code!=null) {
            log.trace("Search on MedicationRequest.medicationCode code = "+code.getValue());
            Join<MedicationRequestEntity, ConceptEntity> joinConcept = root.join("medicationCode", JoinType.LEFT);
            Predicate p = builder.equal(joinConcept.get("code"),code.getValue());
            predList.add(p);
        }

        if (status != null) {
            Integer presstatus = null;
            switch (status.getValue().toLowerCase()) {
                case "active":
                    presstatus = 0;
                    break;
                case "on-hold":
                    presstatus = 1;
                    break;
                case "cancelled":
                    presstatus = 2;
                    break;
                case "completed":
                    presstatus = 3;
                    break;
                case "entered-in-error":
                    presstatus = 4;
                    break;
                case "stopped":
                    presstatus = 5;
                    break;
                case "draft":
                    presstatus = 6;
                    break;
                case "unknown":
                    presstatus = 7;
                    break;

            }

            Predicate p = builder.equal(root.get("status"), presstatus);
            predList.add(p);

        }


        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);

        if (dateWritten !=null)
        {


            if (dateWritten.getLowerBound() != null) {

                DateParam dateParam = dateWritten.getLowerBound();


                switch (dateParam.getPrefix()) {
                    case GREATERTHAN: {
                        Predicate p = builder.greaterThan(root.<Date>get("dateWritten"), parameterLower);
                        predList.add(p);

                        break;
                    }
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("dateWritten"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("dateWritten"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("dateWritten"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("dateWritten"), parameterLower);
                        predList.add(p);
                        break;

                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + dateWritten.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }
            if (dateWritten.getUpperBound() != null) {

                DateParam dateParam = dateWritten.getUpperBound();

                log.debug("Upper Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case APPROXIMATE:
                    case EQUAL: {
                        Predicate pupper = builder.lessThan(root.<Date>get("dateWritten"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("dateWritten"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("dateWritten"), parameterUpper);
                        predList.add(p);

                        break;
                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + dateWritten.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }

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

        TypedQuery<MedicationRequestEntity> typedQuery = em.createQuery(criteria);

        if (dateWritten != null) {
            if (dateWritten.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, dateWritten.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            if (dateWritten.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, dateWritten.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
        }
        qryResults = typedQuery.getResultList();
        return qryResults;
    }


}

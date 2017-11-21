package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.ImmunisationEntityToFHIRImmunizationTransformer;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

import uk.nhs.careconnect.ri.entity.immunisation.ImmunisationEntity;
import uk.nhs.careconnect.ri.entity.immunisation.ImmunisationIdentifier;
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
public class ImmunizationDao implements ImmunizationRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    EncounterRepository encounterDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;


    private static final Logger log = LoggerFactory.getLogger(ImmunizationDao.class);

    @Autowired
    ImmunisationEntityToFHIRImmunizationTransformer immunisationEntityToFHIRImmunizationTransformer;



    @Override
    public ImmunisationEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            ImmunisationEntity immunisation = (ImmunisationEntity) em.find(ImmunisationEntity.class, Long.parseLong(theId.getIdPart()));
            return immunisation;
        }
        return null;

    }


    @Override
    public void save(FhirContext ctx, ImmunisationEntity immunisation) {

    }

    @Override
    public Immunization read(FhirContext ctx,IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            ImmunisationEntity immunisation = (ImmunisationEntity) em.find(ImmunisationEntity.class, Long.parseLong(theId.getIdPart()));
            return immunisation == null
                    ? null
                    : immunisationEntityToFHIRImmunizationTransformer.transform(immunisation);
        } else {
            return null;
        }

    }

    @Override
    public Immunization create(FhirContext ctx,Immunization immunisation, IdType theId, String theImmunizational) {
        return null;
    }

    @Override
    public List<Immunization> search(FhirContext ctx,ReferenceParam patient, DateRangeParam date, TokenParam status, TokenParam identifier) {
        List<ImmunisationEntity> qryResults = searchEntity(ctx, patient, date, status,identifier);
        List<Immunization> results = new ArrayList<>();

        for (ImmunisationEntity immunisationEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Immunization immunization = immunisationEntityToFHIRImmunizationTransformer.transform(immunisationEntity);
            results.add(immunization);
        }

        return results;
    }

    @Override
    public List<ImmunisationEntity> searchEntity(FhirContext ctx,ReferenceParam patient, DateRangeParam date, TokenParam status, TokenParam identifier) {
        List<ImmunisationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ImmunisationEntity> criteria = builder.createQuery(ImmunisationEntity.class);
        Root<ImmunisationEntity> root = criteria.from(ImmunisationEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Immunization> results = new ArrayList<Immunization>();

        if (patient != null) {
            Join<ImmunisationEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

            Predicate p = builder.equal(join.get("id"),patient.getIdPart());
            predList.add(p);
        }

        if (identifier !=null)
        {
            Join<ImmunisationEntity, ImmunisationIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (status != null) {
            Integer immstatus = null;
            switch (status.getValue().toLowerCase()) {
                case "completed":
                    immstatus = 0;
                    break;
                case "entered-in-error":
                    immstatus = 1;
                    break;
                    
            }

            Predicate p = builder.equal(root.get("status"), immstatus);
            predList.add(p);

        }
        
        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);

        if (date !=null)
        {


            if (date.getLowerBound() != null) {

                DateParam dateParam = date.getLowerBound();


                switch (dateParam.getPrefix()) {
                    case GREATERTHAN: {
                        Predicate p = builder.greaterThan(root.<Date>get("administrationDateTime"), parameterLower);
                        predList.add(p);

                        break;
                    }
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("administrationDateTime"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("administrationDateTime"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("administrationDateTime"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("administrationDateTime"), parameterLower);
                        predList.add(p);
                        break;

                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + date.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }
            if (date.getUpperBound() != null) {

                DateParam dateParam = date.getUpperBound();

                log.debug("Upper Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case APPROXIMATE:
                    case EQUAL: {
                        Predicate pupper = builder.lessThan(root.<Date>get("administrationDateTime"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("administrationDateTime"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("administrationDateTime"), parameterUpper);
                        predList.add(p);

                        break;
                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + date.getValuesAsQueryTokens().get(0).getPrefix());
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

        TypedQuery<ImmunisationEntity> typedQuery = em.createQuery(criteria);

        if (date != null) {
            if (date.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, date.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            if (date.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, date.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
        }
        qryResults = typedQuery.getResultList();
        return qryResults;
    }
}



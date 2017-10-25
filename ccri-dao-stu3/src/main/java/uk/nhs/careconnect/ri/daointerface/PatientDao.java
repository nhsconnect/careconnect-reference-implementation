package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.PatientEntityToFHIRPatientTransformer;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;
import uk.nhs.careconnect.ri.entity.patient.*;

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
public class PatientDao implements PatientRepository {


    @PersistenceContext
    EntityManager em;

    @Autowired
    private PatientEntityToFHIRPatientTransformer patientEntityToFHIRPatientTransformer;

    private static final Logger log = LoggerFactory.getLogger(PatientDao.class);


    @Transactional
    @Override
    public void save(PatientEntity patient)
    {
        em.persist(patient);
    }

    public Patient read(IdType theId) {

        log.info("Looking for patient = "+theId.getIdPart());
        PatientEntity patientEntity = (PatientEntity) em.find(PatientEntity.class,Long.parseLong(theId.getIdPart()));

        return patientEntity == null
                ? null
                : patientEntityToFHIRPatientTransformer.transform(patientEntity);

    }

    public PatientEntity readEntity(IdType theId) {

        return  (PatientEntity) em.find(PatientEntity.class,Long.parseLong(theId.getIdPart()));

    }
    public List<Patient> searchPatient (
            @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
            @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
            @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
            @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
            @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
            @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
            @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name= Patient.SP_NAME) StringParam name,
            @OptionalParam(name= Patient.SP_PHONE) StringParam phone
    )
    {


        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<PatientEntity> criteria = builder.createQuery(PatientEntity.class);
        Root<PatientEntity> root = criteria.from(PatientEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Patient> results = new ArrayList<Patient>();

        if (identifier !=null)
        {
            Join<PatientEntity, PatientIdentifier> join = root.join("identifiers", JoinType.LEFT);
            Join<PatientIdentifier,SystemEntity> joinSystem = join.join("systemEntity",JoinType.LEFT);

            Predicate pvalue = builder.like(
                    builder.upper(join.get("value")),
                    builder.upper(builder.literal(identifier.getValue()))
            );
            if (identifier.getSystem() != null) {
                Predicate psystem = builder.like(
                        builder.upper(joinSystem.get("codeSystemUri")),
                        builder.upper(builder.literal(identifier.getSystem()))
                );
                Predicate p = builder.and(pvalue, psystem);
                predList.add(p);
            } else {
                predList.add(pvalue);
            }


        }
        if ((familyName != null) || (givenName != null) || (name != null)) {

            Join<PatientEntity,PatientName> namejoin = root.join("names",JoinType.LEFT);
            if (familyName != null) {
                Predicate p =
                        builder.like(
                                builder.upper(namejoin.get("familyName").as(String.class)),
                                builder.upper(builder.literal(familyName.getValue()+"%"))
                        );
                predList.add(p);
            }
            if (givenName != null) {
                Predicate p =
                        builder.like(
                                builder.upper(namejoin.get("givenName").as(String.class)),
                                builder.upper(builder.literal(givenName.getValue()+"%"))
                        );
                predList.add(p);
            }

            if (name != null) {
                Predicate pgiven = builder.like(
                        builder.upper(namejoin.get("givenName").as(String.class)),
                        builder.upper(builder.literal(name.getValue()+"%"))
                );
                Predicate pfamily = builder.like(
                        builder.upper(namejoin.get("familyName").as(String.class)),
                        builder.upper(builder.literal(name.getValue()+"%"))
                );
                Predicate p = builder.or(pfamily, pgiven);
                predList.add(p);
            }
        }
        ParameterExpression<java.util.Date> parameterLower = null;
        ParameterExpression<java.util.Date> parameterUpper = null;
        Boolean paramLowerPresent = false;
        Boolean paramUpperPresent = false;
        Date dobFrom = null;
        Date dobTo = null;

        if (birthDate !=null)
        {

            dobFrom = birthDate.getLowerBoundAsInstant();
            dobTo = birthDate.getUpperBoundAsInstant();
            if (dobFrom != null) log.trace("dob lower"+dobFrom.toString());
            if (dobTo!= null) log.trace("dob upper"+dobTo.toString());

            parameterLower = builder.parameter(java.util.Date.class);
            parameterUpper = builder.parameter(java.util.Date.class);

            for (DateParam dateParam: birthDate.getValuesAsQueryTokens()) {

                log.trace("Date Param - "+dateParam.getValue()+" Prefix - "+dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case GREATERTHAN: {
                        Predicate p = builder.greaterThan(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(p);
                        paramLowerPresent = true;
                        break;
                    }
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(p);
                        paramLowerPresent = true;
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {
                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(plow);
                        Predicate pupper = builder.lessThanOrEqualTo(root.<Date>get("dateOfBirth"), parameterUpper);
                        predList.add(pupper);
                        paramLowerPresent = true;
                        paramUpperPresent = true;
                        break;
                    }

                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(p);
                        paramLowerPresent = true;
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(p);
                        paramLowerPresent = true;
                        break;

                    }
                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("dateOfBirth"), parameterUpper);
                        predList.add(p);
                        paramUpperPresent = true;
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("dateOfBirth"), parameterUpper);
                        predList.add(p);
                        paramUpperPresent = true;
                        break;
                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + birthDate.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }
        }

        if (gender != null)
        {
            // KGM 2017-10-11 Fix for #3 https://github.com/nhsconnect/careconnect-reference-implementation/issues/3
            Predicate p = builder.equal(
                    builder.upper(root.get("gender"))
                    ,builder.upper(builder.literal(gender.getValue()))
            );
            predList.add(p);
        }

        if (email != null || phone != null)
        {
            Join<PatientEntity, PatientTelecom> joinTel = root.join("telecoms", JoinType.LEFT);

            if (email!=null) {
                Predicate psystem = builder.equal(joinTel.get("system"),2);

                Predicate pvalue =
                        builder.like(
                                builder.upper(joinTel.get("value").as(String.class)),
                                builder.upper(builder.literal(email.getValue()+"%"))
                        );

                Predicate p = builder.and(pvalue,psystem);
                predList.add(p);
            }
            if (phone!=null) {
                Predicate pvalue = builder.equal(joinTel.get("value"),phone.getValue());
                Predicate psystem = builder.equal(joinTel.get("system"),0);
                Predicate p = builder.and(pvalue,psystem);
                predList.add(p);
            }
        }
        if (addressPostcode != null )
        {
            Join<PatientEntity, PatientAddress> joinAdr = root.join("addresses", JoinType.LEFT);
            Join<PatientAddress, AddressEntity> joinAdrTable = joinAdr.join("address", JoinType.LEFT);
            if (addressPostcode!=null) {
                Predicate p = builder.like(
                        builder.upper(joinAdrTable.get("postcode").as(String.class)),
                        builder.upper(builder.literal(addressPostcode.getValue()+"%"))
                );
                predList.add(p);
            }
        }

        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0) {
            criteria.select(root).where(predArray);
        }
        else {
            criteria.select(root);
        }
        List<PatientEntity> qryResults = null;
        TypedQuery<PatientEntity> typedQuery = em.createQuery(criteria);

        if (paramLowerPresent && dobFrom!=null) typedQuery.setParameter(parameterLower,dobFrom, TemporalType.DATE);
        if (paramUpperPresent && dobTo!=null) typedQuery.setParameter(parameterUpper,dobTo, TemporalType.DATE);

        qryResults = typedQuery.getResultList();

        log.info("Found Patients = "+qryResults.size());
        for (PatientEntity patientEntity : qryResults)
        {
            Patient patient = patientEntityToFHIRPatientTransformer.transform(patientEntity);
            results.add(patient);
        }

        return results;
    }


}

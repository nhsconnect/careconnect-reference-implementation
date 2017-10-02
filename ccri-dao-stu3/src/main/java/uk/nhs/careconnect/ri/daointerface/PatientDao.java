package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.Transforms.PatientEntityToFHIRPatientTransformer;
import uk.nhs.careconnect.ri.entity.AddressEntity;
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

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if ((familyName != null) || (givenName != null) || (name != null)) {

            Join<PatientEntity,PatientName> namejoin = root.join("names",JoinType.LEFT);
            if (familyName != null) {
                Predicate p =
                        builder.like(
                                builder.upper(namejoin.get("familyName").as(String.class)),
                                builder.upper(builder.literal("%"+familyName.getValue()+"%"))
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
        ParameterExpression<java.util.Date> parameterTo = null;
        ParameterExpression<java.util.Date> parameterFrom = null;
        Date dobFrom = null;
        Date dobTo = null;

        if (birthDate !=null)
        {

            dobFrom = birthDate.getLowerBoundAsInstant();
            dobTo = birthDate.getUpperBoundAsInstant();


            parameterFrom = builder.parameter(java.util.Date.class);

            switch (birthDate.getValuesAsQueryTokens().get(0).getPrefix())
            {
                case GREATERTHAN :
                {
                    Predicate p = builder.greaterThan(root.<Date>get("dateOfBirth"),parameterFrom);
                    predList.add(p);
                    break;
                }
                case GREATERTHAN_OR_EQUALS :
                {
                    Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("dateOfBirth"),parameterFrom);
                    predList.add(p);
                    break;
                }
                case APPROXIMATE :
                case EQUAL :
                {
                    Predicate p = builder.equal(root.<Date>get("dateOfBirth"),parameterFrom);
                    predList.add(p);
                    break;
                }

                case NOT_EQUAL :
                {
                    Predicate p = builder.notEqual(root.<Date>get("dateOfBirth"),parameterFrom);
                    predList.add(p);
                    break;
                }
                case STARTS_AFTER :
                {
                    Predicate p = builder.greaterThan(root.<Date>get("dateOfBirth"),parameterFrom);
                    predList.add(p);
                    break;

                }
                case LESSTHAN_OR_EQUALS :
                {
                    Predicate p = builder.lessThanOrEqualTo(root.<Date>get("dateOfBirth"),parameterFrom);
                    predList.add(p);
                    break;
                }
                case ENDS_BEFORE :
                case LESSTHAN :
                {
                    Predicate p = builder.lessThan(root.<Date>get("dateOfBirth"),parameterFrom);
                    predList.add(p);
                    break;
                }
                default:
                    log.info("DEFAULT DATE(0) Prefix = "+birthDate.getValuesAsQueryTokens().get(0).getPrefix());
            }
            /*
            if (birthDate.getValuesAsQueryTokens().size()>1)
            {
                log.info("MORE THAN ONE DATE");
                parameterTo = builder.parameter(java.util.Date.class);
                switch (birthDate.getValuesAsQueryTokens().get(1).getPrefix())
                {
                    case GREATERTHAN :
                    {
                        Predicate p = builder.greaterThan(root.<Date>get("dateOfBirth"),parameterTo);
                        predList.add(p);
                        break;
                    }
                    case GREATERTHAN_OR_EQUALS :
                    {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("dateOfBirth"),parameterTo);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE :
                    case EQUAL :
                    {
                        Predicate p = builder.equal(root.<Date>get("dateOfBirth"),parameterTo);
                        predList.add(p);
                        break;
                    }

                    case NOT_EQUAL :
                    {
                        Predicate p = builder.notEqual(root.<Date>get("dateOfBirth"),parameterTo);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER :
                    {
                        Predicate p = builder.greaterThan(root.<Date>get("dateOfBirth"),parameterTo);
                        predList.add(p);
                        break;

                    }
                    case LESSTHAN_OR_EQUALS :
                    {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("dateOfBirth"),parameterTo);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE :
                    case LESSTHAN :
                    {
                        Predicate p = builder.lessThan(root.<Date>get("dateOfBirth"),parameterTo);
                        predList.add(p);
                        break;
                    }
                    default:
                        log.info("DEFAULT DATE(1) Prefix = "+birthDate.getValuesAsQueryTokens().get(1).getPrefix());
                }
            }
*/


        }

        if (gender != null)
        {
            Predicate p = builder.equal(root.get("gender"),gender.getValue());
            predList.add(p);
        }

        if (email != null || phone != null)
        {
            Join<PatientEntity, PatientTelecom> joinTel = root.join("telecoms", JoinType.LEFT);

            if (email!=null) {
                Predicate pvalue = builder.equal(joinTel.get("value"),email.getValue());
                Predicate psystem = builder.equal(joinTel.get("system"),2);
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
                        builder.upper(builder.literal("%"+addressPostcode.getValue()+"%"))
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

        if (dobFrom!=null) typedQuery.setParameter(parameterFrom,dobFrom, TemporalType.DATE);
       // if (dobTo!=null) typedQuery.setParameter(parameterTo,dobTo, TemporalType.DATE);

        qryResults = typedQuery.getResultList();


        for (PatientEntity patientEntity : qryResults)
        {
            Patient patient = patientEntityToFHIRPatientTransformer.transform(patientEntity);
            results.add(patient);
        }

        return results;
    }


}

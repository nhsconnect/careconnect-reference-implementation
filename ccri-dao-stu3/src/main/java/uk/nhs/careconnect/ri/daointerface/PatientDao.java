package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.PatientEntityToFHIRPatientTransformer;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.Terminology.SystemEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.*;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;
import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

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
    ConceptRepository conceptDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    private PatientEntityToFHIRPatientTransformer patientEntityToFHIRPatientTransformer;

    private static final Logger log = LoggerFactory.getLogger(PatientDao.class);



    @Transactional
    @Override
    public void save(FhirContext ctx, PatientEntity patient)
    {


        em.persist(patient);
    }

    public Patient read(FhirContext ctx, IdType theId) {

        log.info("Looking for patient = "+theId.getIdPart());
        if (daoutils.isNumeric(theId.getIdPart())) {
            PatientEntity patientEntity = (PatientEntity) em.find(PatientEntity.class, Long.parseLong(theId.getIdPart()));

            Patient patient = null;
            if (patientEntity != null) {
                patient = patientEntityToFHIRPatientTransformer.transform(patientEntity);
                patientEntity.setResource(ctx.newJsonParser().encodeResourceToString(patient));
                em.persist(patientEntity);
            }
            return patient;
        } else {
            return null;
        }
    }

    public PatientEntity readEntity(FhirContext ctx,IdType theId) {

        return  (PatientEntity) em.find(PatientEntity.class,Long.parseLong(theId.getIdPart()));

    }

    @Override
    public Patient update(FhirContext ctx, Patient patient, IdType theId, String theConditional) {

        PatientEntity patientEntity = null;

        if (theId != null) {
            log.info("theId.getIdPart()="+theId.getIdPart());
            patientEntity = (PatientEntity) em.find(PatientEntity.class, Long.parseLong(theId.getIdPart()));
        }
        if (patientEntity == null)
            return null;

        if (patient.hasExtension())
        {
            for (Extension extension : patient.getExtension()) {
                switch (extension.getUrl()) {
                    case CareConnectExtension.UrlEthnicCategory :
                        CodeableConcept ethnic = (CodeableConcept) extension.getValue();
                        ConceptEntity code = conceptDao.findCode(ethnic.getCoding().get(0).getSystem(),ethnic.getCoding().get(0).getCode());
                        if (code != null) { patientEntity.setEthnicCode(code); }
                        else {
                            log.error("Ethnic: Missing System/Code = "+ ethnic.getCoding().get(0).getSystem() +" code = "+ethnic.getCoding().get(0).getCode());
                            throw new IllegalArgumentException("Missing System/Code = "+ ethnic.getCoding().get(0).getSystem() +" code = "+ethnic.getCoding().get(0).getCode());
                        }
                        break;
                }

            }
        }
        if (patient.hasActive()) {
            patientEntity.setActive(patient.getActive());
        } else {
            patientEntity.setActive(null);
        }

        if (patient.hasGender()) {
            switch (patient.getGender()) {
                case MALE:
                    patientEntity.setGender("MALE");
                    break;
                case FEMALE:
                    patientEntity.setGender("FEMALE");
                    break;
                case UNKNOWN:
                    patientEntity.setGender("UNKNOWN");
                    break;
                case OTHER:
                    patientEntity.setGender("OTHER");
                    break;
                case NULL:
                    patientEntity.setGender(null);
                    break;
            }
        } else {
            patientEntity.setGender(null);
        }

        if (patient.hasBirthDate()) {
            patientEntity.setDateOfBirth(patient.getBirthDate());
        } else {
            patientEntity.setDateOfBirth(null);
        }

        if (patient.hasMaritalStatus()) {
            CodeableConcept martial = patient.getMaritalStatus();
            ConceptEntity code = conceptDao.findCode(martial.getCoding().get(0).getSystem(),martial.getCoding().get(0).getCode());
            if (code != null) { patientEntity.setMaritalCode(code); }
            else {
                log.error("Marital: Missing System/Code = "+ martial.getCoding().get(0).getSystem() +" code = "+martial.getCoding().get(0).getCode());
                throw new IllegalArgumentException("Missing System/Code = "+ martial.getCoding().get(0).getSystem() +" code = "+martial.getCoding().get(0).getCode());
            }
        }
        for (Identifier identifer : patient.getIdentifier()) {
            if (identifer.getSystem().equals(CareConnectSystem.NHSNumber) && identifer.getExtension().size()>0) {
                CodeableConcept nhsVerification = (CodeableConcept) identifer.getExtension().get(0).getValue();
                ConceptEntity code = conceptDao.findCode(nhsVerification.getCoding().get(0).getSystem(),nhsVerification.getCoding().get(0).getCode());
                if (code != null) { patientEntity.setNHSVerificationCode(code); }
                else {
                    log.error("NHS Verfication: Missing System/Code = "+ nhsVerification.getCoding().get(0).getSystem() +" code = "+nhsVerification.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ nhsVerification.getCoding().get(0).getSystem() +" code = "+nhsVerification.getCoding().get(0).getCode());
                }
            }
        }
        if (patient.hasManagingOrganization()) {
            OrganisationEntity organisationEntity = organisationDao.readEntity(new IdType(patient.getManagingOrganization().getReference()));
            if (organisationEntity != null) {
                patientEntity.setPractice(organisationEntity);
            }
        }
        if (patient.hasGeneralPractitioner()) {
            PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(patient.getGeneralPractitioner().get(0).getReference()));
            if (practitionerEntity != null) {
                patientEntity.setGp(practitionerEntity);
            }
        }
        em.persist(patientEntity);



        return patient;
    }

    public List<Patient> searchPatient (FhirContext ctx,
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



        ParameterExpression<java.util.Date> parameterLower = builder.parameter(java.util.Date.class);
        ParameterExpression<java.util.Date> parameterUpper = builder.parameter(java.util.Date.class);
        if (birthDate !=null)
        {

            if (birthDate.getLowerBoundAsInstant() != null) log.info("getLowerBoundAsInstant()="+birthDate.getLowerBoundAsInstant().toString());
            if (birthDate.getUpperBoundAsInstant() != null) log.info("getUpperBoundAsInstant()="+birthDate.getUpperBoundAsInstant().toString());


            if (birthDate.getLowerBound() != null) {

                DateParam dateParam = birthDate.getLowerBound();
                log.info("Lower Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case GREATERTHAN: {
                        Predicate p = builder.greaterThan(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(p);

                        break;
                    }
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("dateOfBirth"), parameterLower);
                        predList.add(p);
                        break;

                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + birthDate.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }
            if (birthDate.getUpperBound() != null) {

                DateParam dateParam = birthDate.getUpperBound();

                log.info("Upper Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case APPROXIMATE:
                    case EQUAL: {
                        Predicate pupper = builder.lessThan(root.<Date>get("dateOfBirth"), parameterUpper);
                        predList.add(pupper);
                        // For Date searh need to correct upper bound
                        birthDate.setUpperBound(new DateParam().setValue(DateHelper.getTomorrowDate(birthDate.getUpperBoundAsInstant())));
                        log.info("New upperBound = "+birthDate.getUpperBoundAsInstant().toString());
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("dateOfBirth"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("dateOfBirth"), parameterUpper);
                        predList.add(p);

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

        if (birthDate != null) {
            if (birthDate.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, birthDate.getLowerBoundAsInstant(), TemporalType.DATE);
            if (birthDate.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, birthDate.getUpperBoundAsInstant(), TemporalType.DATE);
        }

        qryResults = typedQuery.getResultList();

        log.info("Found Patients = "+qryResults.size());
        for (PatientEntity patientEntity : qryResults)
        {
            Patient patient = null;

            if (patientEntity.getResource() != null) {
                patient = (Patient) ctx.newJsonParser().parseResource(patientEntity.getResource());
            } else {
                patient = patientEntityToFHIRPatientTransformer.transform(patientEntity);
                String resourceStr = ctx.newJsonParser().encodeResourceToString(patient);
                log.trace("Length = "+resourceStr.length() +" Data = " +resourceStr);
                patientEntity.setResource(resourceStr);
                em.persist(patientEntity);
            }

            results.add(patient);
        }

        return results;
    }


}

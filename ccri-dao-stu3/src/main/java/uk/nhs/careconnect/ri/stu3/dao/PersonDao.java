package uk.nhs.careconnect.ri.stu3.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.daointerface.PersonRepository;
import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.Person.*;
import uk.nhs.careconnect.ri.database.entity.namingSystem.NamingSystemUniqueId;
import uk.nhs.careconnect.ri.stu3.dao.transforms.PersonEntityToFHIRPersonTransformer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class PersonDao implements PersonRepository {


    @PersistenceContext
    EntityManager em;

    @Autowired
    private LibDao libDao;

    @Autowired
    private PersonEntityToFHIRPersonTransformer personEntityToFHIRPersonTransformer;

    private static final Logger log = LoggerFactory.getLogger(PersonDao.class);

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(PersonEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Transactional
    @Override
    public void save(FhirContext ctx, PersonEntity
            person) {

        em.persist(person);
    }

    @Override
    public Person read(FhirContext ctx, IdType theId) {

        log.info("Looking for person = " + theId.getIdPart());
        if (daoutils.isNumeric(theId.getIdPart())) {
            PersonEntity personEntity = (PersonEntity) em.find(PersonEntity.class, Long.parseLong(theId.getIdPart()));

            Person person = null;

            if (personEntity != null) {
                person = personEntityToFHIRPersonTransformer.transform(personEntity);
                personEntity.setResource(ctx.newJsonParser().encodeResourceToString(person));
                em.persist(personEntity);
            }
            return person;
        } else {
            return null;
        }
    }

    @Override
    public PersonEntity readEntity(FhirContext ctx, IdType theId) {

        return em.find(PersonEntity.class, Long.parseLong(theId.getIdPart()));

    }

    @Override
    public Person update(FhirContext ctx, Person person, IdType theId) throws OperationOutcomeException {

        PersonEntity personEntity = null;
        log.info("Started person updated");
        if (theId != null) {
            log.trace("theId.getIdPart()=" + theId.getIdPart());
            personEntity = (PersonEntity) em.find(PersonEntity.class, Long.parseLong(theId.getIdPart()));
        }

        for (Identifier component: person.getIdentifier()) {
            List<PersonEntity> entries = searchEntity(ctx, null, new TokenParam().setValue(component.getValue()), null, null);
            for (PersonEntity personSearch : entries) {
                if (person.getId() == null) {
                    throw new ResourceVersionConflictException("NamingSystem Unique identifier "+component.getValue()+ " is already present on the system "+ personSearch.getId());
                }

                if (!personSearch.getId().equals(personEntity.getId())) {
                    throw new ResourceVersionConflictException("NamingSystem Unique identifier "+component.getValue()+ " is already present on the system "+ personSearch.getId());
                }
            }
        }



        if (personEntity == null) {
            log.trace("Adding new Person");
            personEntity = new PersonEntity();
        }
        personEntity.setResource(null);

        if (person.hasActive()) {
            personEntity.setActive(person.getActive());
        } else {
            personEntity.setActive(null);
        }

        if (person.hasGender()) {
            switch (person.getGender()) {
                case MALE:
                    personEntity.setGender("MALE");
                    break;
                case FEMALE:
                    personEntity.setGender("FEMALE");
                    break;
                case UNKNOWN:
                    personEntity.setGender("UNKNOWN");
                    break;
                case OTHER:
                    personEntity.setGender("OTHER");
                    break;
                case NULL:
                    personEntity.setGender(null);
                    break;
            }
        } else {
            personEntity.setGender(null);
        }

        if (person.hasBirthDate()) {
            personEntity.setDateOfBirth(person.getBirthDate());
        } else {
            personEntity.setDateOfBirth(null);
        }


        em.persist(personEntity);
        log.info("Person Stored" + personEntity.getId());


        // Remove all identifiers with systems that match the updated Person, leave unmatched identifiers alone.
        for (PersonIdentifier orgSearch : personEntity.getIdentifiers()) {
            Boolean found = false;
            for (Identifier identifier : person.getIdentifier()) {
                if (identifier.getSystem().equals(orgSearch.getSystemValue())) {
                    found = true;
                }
            }
            if (found) {
                em.remove(orgSearch);
            }
        }

        for (Identifier identifier : person.getIdentifier()) {

            PersonIdentifier personIdentifier = null;

            if (personIdentifier == null) {
                personIdentifier = new PersonIdentifier();
                personEntity.getIdentifiers().add(personIdentifier);
            }

            personIdentifier = (PersonIdentifier) libDao.setIdentifier2(identifier, personIdentifier);
            if (personIdentifier != null) {
                personIdentifier.setPerson(personEntity);

                em.persist(personIdentifier);
            }
        }

        log.info("Person Identifier");


        for (PersonName nameSearch : personEntity.getNames()) {
            em.remove(nameSearch);
        }
        personEntity.setNames(new ArrayList<>());

        for (HumanName name : person.getName()) {
            PersonName personName = null;

            if (personName == null) {
                personName = new PersonName();
                personName.setPersonEntity(personEntity);
            }

            personName.setFamilyName(name.getFamily());
            if (name.getGiven().size() > 0)
                personName.setGivenName(name.getGiven().get(0).getValue());
            if (name.getPrefix().size() > 0)
                personName.setPrefix(name.getPrefix().get(0).getValue());
            if (name.getUse() != null) {
                personName.setNameUse(name.getUse());
            }
            em.persist(personName);
        }

        log.info("Person Name Stored");


        // Temp fix to remove old addresses 16/8/2018 KGM
        for (PersonAddress adrSearch : personEntity.getAddresses()) {
            em.remove(adrSearch);
        }
        for (Address address : person.getAddress()) {
            PersonAddress personAdr = null;

            if (personAdr == null) {
                personAdr = new PersonAddress();
                personAdr.setPersonEntity(personEntity);
                personEntity.getAddresses().add(personAdr);
            }

            AddressEntity addr = personAdr.getAddress();
            if (addr == null) {
                addr = personAdr.setAddress(new AddressEntity());
            }

            if (address.getLine().size() > 0) addr.setAddress1(address.getLine().get(0).getValue().trim());
            if (address.getLine().size() > 1) addr.setAddress2(address.getLine().get(1).getValue().trim());
            if (address.getLine().size() > 2) addr.setAddress3(address.getLine().get(2).getValue().trim());
            if (address.getCity() != null) addr.setCity(address.getCity());
            if (address.getDistrict() != null) addr.setCounty(address.getDistrict());
            if (address.getPostalCode() != null) addr.setPostcode(address.getPostalCode());
            if (address.getCountry() != null) addr.setCountry(address.getCountry());

            if (address.getUse() != null) personAdr.setAddressUse(address.getUse());
            if (address.getType() != null) personAdr.setAddressType(address.getType());


            em.persist(addr);
            em.persist(personAdr);
        }

        log.info("Person Address Stored");

        for (PersonTelecom telSearch : personEntity.getTelecoms()) {
            em.remove(telSearch);
        }
        for (ContactPoint contact : person.getTelecom()) {
            PersonTelecom personTel = new PersonTelecom();

            personTel.setPersonEntity(personEntity);
            personTel.setValue(contact.getValue());

            if (contact.hasSystem())
                personTel.setSystem(contact.getSystem());
            if (contact.hasUse())
                personTel.setTelecomUse(contact.getUse());
            em.persist(personTel);
            personEntity.getTelecoms().add(personTel);
        }

        log.info("Person Telecoms Stored");

        Person newPerson = null;

        newPerson = personEntityToFHIRPersonTransformer.transform(personEntity);

        return newPerson;

    }

    @Override
    public List<Resource> search(FhirContext ctx,
                                 @OptionalParam(name = Person.SP_NAME) StringParam name,
                                 @OptionalParam(name = Person.SP_IDENTIFIER) TokenParam identifier,
                                 @OptionalParam(name = Person.SP_EMAIL) TokenParam email,
                                 @OptionalParam(name = Person.SP_PHONE) TokenParam phone

    ) {
        List<PersonEntity> qryResults = searchEntity(ctx, name, identifier,email, phone);
        List<Resource> results = new ArrayList<>();

        for (PersonEntity personEntity : qryResults) {
            log.info("personEntity = " + personEntity.getId().toString());
            Person person = personEntityToFHIRPersonTransformer.transform(personEntity);
            String resourceStr = ctx.newJsonParser().encodeResourceToString(person);
            personEntity.setResource(resourceStr);

            results.add(person);

            // If reverse include selected

        }


        return results;
    }

    @Override
    public List<PersonEntity> searchEntity(FhirContext ctx,
                                           @OptionalParam(name = Person.SP_NAME) StringParam name,
                                           @OptionalParam(name = Person.SP_IDENTIFIER) TokenParam identifier,
                                           @OptionalParam(name = Person.SP_EMAIL) TokenParam email,
                                           @OptionalParam(name = Person.SP_PHONE) TokenParam phone
    ) {


        CriteriaBuilder builder = em.getCriteriaBuilder();

        // KGM 18/12/2017 Added distinct
        CriteriaQuery<PersonEntity> criteria = builder.createQuery(PersonEntity.class).distinct(true);
        Root<PersonEntity> root = criteria.from(PersonEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<PersonEntity> results = new ArrayList<PersonEntity>();

        if (identifier !=null)
        {
            Join<PersonEntity, PersonIdentifier> join = root.join("identifiers", JoinType.LEFT);
            Join<PersonIdentifier, NamingSystemUniqueId> joinSystem = join.join("system",JoinType.LEFT);

            Predicate pvalue = builder.like(
                    builder.upper(join.get("value")),
                    builder.upper(builder.literal(daoutils.removeSpace(identifier.getValue())))
            );
            if (identifier.getSystem() != null) {
                Predicate psystem = builder.like(
                        builder.upper(joinSystem.get("value")),
                        builder.upper(builder.literal(identifier.getSystem()))
                );
                Predicate p = builder.and(pvalue, psystem);
                predList.add(p);
            } else {
                predList.add(pvalue);
            }


        }

        if ((name != null)) {

            Join<PersonEntity, PersonName> namejoin = root.join("names", JoinType.LEFT);


            if (name != null) {
                Predicate pgiven = builder.like(
                        builder.upper(namejoin.get("givenName").as(String.class)),
                        builder.upper(builder.literal(name.getValue() + "%"))
                );
                Predicate pfamily = builder.like(
                        builder.upper(namejoin.get("familyName").as(String.class)),
                        builder.upper(builder.literal(name.getValue() + "%"))
                );
                Predicate p = builder.or(pfamily, pgiven);
                predList.add(p);
            }
        }

        if (email != null || phone != null)
        {
            Join<PersonEntity, PersonTelecom> joinTel = root.join("telecoms", JoinType.LEFT);

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


        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size() > 0) {
            criteria.select(root).where(predArray);
        } else {
            criteria.select(root);
        }
        List<PersonEntity> qryResults = null;
        TypedQuery<PersonEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);


        qryResults = typedQuery.getResultList();

        log.debug("Found Persons = " + qryResults.size());

        return qryResults;
    }


}

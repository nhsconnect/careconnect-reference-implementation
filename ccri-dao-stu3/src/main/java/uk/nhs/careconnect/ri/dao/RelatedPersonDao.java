package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.dao.transforms.*;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.AddressEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.SystemEntity;
import uk.nhs.careconnect.ri.database.entity.patient.PatientEntity;

import uk.nhs.careconnect.ri.database.entity.relatedPerson.*;

import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;

@Repository
@Transactional
public class RelatedPersonDao implements RelatedPersonRepository {


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
    OrganisationRepository organisationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private RelatedPersonEntityToFHIRRelatedPersonTransformer personEntityToFHIRRelatedPersonTransformer;

    @Autowired
    ProcedureEntityToFHIRProcedureTransformer procedureEntityToFHIRProcedureTransformer;

    @Autowired
    private ObservationEntityToFHIRObservationTransformer observationEntityToFHIRObservationTransformer;

    @Autowired
    ConditionEntityToFHIRConditionTransformer conditionEntityToFHIRConditionTransformer;

    @Autowired
    private MedicationRequestEntityToFHIRMedicationRequestTransformer
            medicationRequestEntityToFHIRMedicationRequestTransformer;

    @Autowired
    private EncounterEntityToFHIREncounterTransformer encounterEntityToFHIREncounterTransformer;

    @Autowired
    private AllergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer;

    @Autowired
    private ImmunisationEntityToFHIRImmunizationTransformer immunisationEntityToFHIRImmunizationTransformer;

    @Autowired
    private OrganisationEntityToFHIROrganizationTransformer organisationEntityToFHIROrganizationTransformer;

    @Autowired
    private PractitionerEntityToFHIRPractitionerTransformer practitionerEntityToFHIRPractitionerTransformer;

    private static final Logger log = LoggerFactory.getLogger(RelatedPersonDao.class);


    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(RelatedPersonEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Transactional
    @Override
    public void save(FhirContext ctx, RelatedPersonEntity person)
    {


        em.persist(person);
    }

    @Override
    public RelatedPerson read(FhirContext ctx, IdType theId) {

        log.info("Looking for person = "+theId.getIdPart());
        if (daoutils.isNumeric(theId.getIdPart())) {
            RelatedPersonEntity personEntity = (RelatedPersonEntity) em.find(RelatedPersonEntity.class, Long.parseLong(theId.getIdPart()));

            RelatedPerson person = null;
            if (personEntity != null) {
                person = personEntityToFHIRRelatedPersonTransformer.transform(personEntity);
                personEntity.setResource(ctx.newJsonParser().encodeResourceToString(person));
                em.persist(personEntity);
            }
            return person;
        } else {
            return null;
        }
    }

    @Override
    public RelatedPersonEntity readEntity(FhirContext ctx,IdType theId) {

        return  (RelatedPersonEntity) em.find(RelatedPersonEntity.class,Long.parseLong(theId.getIdPart()));

    }

    @Override
    public RelatedPerson update(FhirContext ctx, RelatedPerson person, IdType theId, String theConditional) throws OperationOutcomeException {

        RelatedPersonEntity personEntity = null;
        log.debug("RelatedPerson.save");
        if (theId != null) {
            log.trace("theId.getIdPart()="+theId.getIdPart());
            personEntity = (RelatedPersonEntity) em.find(RelatedPersonEntity.class, Long.parseLong(theId.getIdPart()));
        }

        if (theConditional != null) {
            try {
                log.trace("Conditional Url = "+theConditional);

                //CareConnectSystem.ODSOrganisationCode
                if (theConditional.contains("PPMIdentifier")) {
                    URI uri = new URI(theConditional);

                    //String scheme = uri.getScheme();
                    //String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.trace(query);
                    String[] spiltStr = query.split("%7C");
                    log.trace(spiltStr[1]);

                    List<RelatedPersonEntity> results = searchEntity(ctx, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/PPMIdentifier"), null,null);
                    log.trace("Loop over results");
                    for (RelatedPersonEntity pat : results) {
                        personEntity = pat;
                        break;
                    }
                    // This copes with the new identifier being added.
                    if (personEntity == null && daoutils.isNumeric(spiltStr[1])) {
                        log.trace("Looking for person with id of "+spiltStr[1]);
                        personEntity = (RelatedPersonEntity) em.find(RelatedPersonEntity.class, Long.parseLong(spiltStr[1]));
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }

        log.debug("RelatedPerson.saveMain");

        if (personEntity == null) {
            log.trace("Adding new RelatedPerson");
            personEntity = new RelatedPersonEntity();
        }

        PatientEntity patientEntity = null;
        if (person.hasPatient()) {
            log.trace(person.getPatient().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(person.getPatient().getReference()));
            personEntity.setPatient(patientEntity);
        }

        if (person.hasRelationship()) {
            ConceptEntity code = conceptDao.findAddCode(person.getRelationship().getCoding().get(0));
            if (code != null) { personEntity.setRelationship(code); }
            else {
                log.info("Code: Missing System/Code = "+ person.getRelationship().getCoding().get(0).getSystem() +" code = "+person.getRelationship().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ person.getRelationship().getCoding().get(0).getSystem() +" code = "+person.getRelationship().getCoding().get(0).getCode());
            }

        }

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

        log.debug("RelatedPerson.saveEntity");

        em.persist(personEntity);

        for (Identifier identifier : person.getIdentifier()) {
            if (identifier.getSystem().equals(CareConnectSystem.NHSNumber) && identifier.getExtension().size()>0) {
                CodeableConcept nhsVerification = (CodeableConcept) identifier.getExtension().get(0).getValue();
                ConceptEntity code = conceptDao.findCode(nhsVerification.getCoding().get(0));

            }
            RelatedPersonIdentifier personIdentifier = null;
            for (RelatedPersonIdentifier orgSearch : personEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    personIdentifier = orgSearch;
                    break;
                }
            }
            if (personIdentifier == null)  personIdentifier = new RelatedPersonIdentifier();

            personIdentifier.setValue(daoutils.removeSpace(identifier.getValue()));
            personIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            personIdentifier.setRelatedPerson(personEntity);
            em.persist(personIdentifier);
        }
        em.persist(personEntity);

        log.debug("RelatedPerson.saveIdentifier");

        for (HumanName name : person.getName()) {
            RelatedPersonName personName = null;
            for (RelatedPersonName nameSearch : personEntity.getNames()) {
                // look for matching surname and also if the have matching given name
                if (nameSearch.getFamilyName().equals(name.getFamily())) {
                    if (name.getGiven().size()> 0) {
                        if (nameSearch.getGivenName().equals(name.getGiven().get(0).getValue())) {
                            personName = nameSearch;
                            break;
                        }
                    }
                    else {
                        personName = nameSearch;
                        break;
                    }
                }
            }
            if (personName == null)  {
                personName = personEntity.addName();
                personName.setRelatedPersonEntity(personEntity);
            }

            personName.setFamilyName(name.getFamily());
            if (name.getGiven().size()>0)
                personName.setGivenName(name.getGiven().get(0).getValue());
            if (name.getPrefix().size()>0)
                personName.setPrefix(name.getPrefix().get(0).getValue());
            if (name.getUse() != null) {
                personName.setNameUse(name.getUse());
            }
            em.persist(personName);
        }

        log.debug("RelatedPerson.saveName");

        for (Address address : person.getAddress()) {
            RelatedPersonAddress personAdr = null;
            for (RelatedPersonAddress adrSearch : personEntity.getAddresses()) {
                // look for matching postcode and first line of address
                if (adrSearch.getAddress().getPostcode().equals(address.getPostalCode())) {
                    if (address.hasLine() && address.getLine().get(0)!=null && adrSearch.getAddress().getAddress1().equals(address.getLine().get(0))) {
                        personAdr = adrSearch;
                        break;
                    }
                }
            }
            if (personAdr == null) {
                personAdr = new RelatedPersonAddress();
                personAdr.setRelatedPersonEntity(personEntity);
                personEntity.addAddress(personAdr);
            }

            AddressEntity addr = personAdr.getAddress();
            if (addr == null) {
                addr = personAdr.setAddress(new AddressEntity());
            }

            if (address.getLine().size()>0) addr.setAddress1(address.getLine().get(0).getValue().trim());
            if (address.getLine().size()>1) addr.setAddress2(address.getLine().get(1).getValue().trim());
            if (address.getLine().size()>2) addr.setAddress3(address.getLine().get(2).getValue().trim());
            if (address.getCity() != null) addr.setCity(address.getCity());
            if (address.getDistrict() != null) addr.setCounty(address.getDistrict());
            if (address.getPostalCode() != null) addr.setPostcode(address.getPostalCode());
            if (address.getCountry() != null) addr.setCountry(address.getCountry());

            if (address.getUse() != null) personAdr.setAddressUse(address.getUse());
            if (address.getType() != null) personAdr.setAddressType(address.getType());


            em.persist(addr);
            em.persist(personAdr);
        }
        log.debug("RelatedPerson.saveAddress");

        for (ContactPoint contact : person.getTelecom()) {
            RelatedPersonTelecom personTel = null;
            log.info("RelatedPerson.searchTelecom");
            for (RelatedPersonTelecom telSearch : personEntity.getTelecoms()) {

                if (telSearch.getValue().equals(contact.getValue())) {
                        personTel = telSearch;
                        break;
                }
            }

            if (personTel == null) {
                log.info("RelatedPerson.isNullTelecom");
                personTel = new RelatedPersonTelecom();
                personTel.setRelatedPersonEntity(personEntity);
                personEntity.addTelecom(personTel);
                personTel.setValue(contact.getValue());
            }
            log.info("RelatedPerson.hasSystemTelecom");
            if (contact.hasSystem())
                personTel.setSystem(contact.getSystem());
            log.info("RelatedPerson.hasUseTelecom");
            if (contact.hasUse())
                personTel.setTelecomUse(contact.getUse());

            em.persist(personTel);
        }
        log.debug("RelatedPerson.saveContactPoint");

        RelatedPerson newRelatedPerson = null;


        newRelatedPerson = personEntityToFHIRRelatedPersonTransformer.transform(personEntity);
      //  personEntity.setResource(ctx.newJsonParser().encodeResourceToString(newRelatedPerson));
        em.persist(personEntity);


        return newRelatedPerson;
    }

    @Override
    public List<Resource> search (FhirContext ctx,
                                  @OptionalParam(name = RelatedPerson.SP_IDENTIFIER) TokenParam identifier,
                                  @OptionalParam(name = RelatedPerson.SP_PATIENT) ReferenceParam patient,
                                  @OptionalParam(name = RelatedPerson.SP_RES_ID) StringParam resid
    ) {
        List<RelatedPersonEntity> qryResults = searchEntity(ctx,  identifier, patient,resid);
        List<Resource> results = new ArrayList<>();

        for (RelatedPersonEntity personEntity : qryResults)
        {
            RelatedPerson person;

            person = personEntityToFHIRRelatedPersonTransformer.transform(personEntity);
            String resourceStr = ctx.newJsonParser().encodeResourceToString(person);
       //     personEntity.setResource(resourceStr);
       //     em.persist(personEntity);

            results.add(person);

            // If reverse include selected

        }


        return results;
    }

    @Override
    public List<RelatedPersonEntity> searchEntity (FhirContext ctx,
                                                   @OptionalParam(name = RelatedPerson.SP_IDENTIFIER) TokenParam identifier,
                                                   @OptionalParam(name = RelatedPerson.SP_PATIENT) ReferenceParam patient,
                                                   @OptionalParam(name = RelatedPerson.SP_RES_ID) StringParam resid
    )
    {


        CriteriaBuilder builder = em.getCriteriaBuilder();

        // KGM 18/12/2017 Added distinct
        CriteriaQuery<RelatedPersonEntity> criteria = builder.createQuery(RelatedPersonEntity.class).distinct(true);
        Root<RelatedPersonEntity> root = criteria.from(RelatedPersonEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<RelatedPersonEntity> results = new ArrayList<RelatedPersonEntity>();

        if (identifier !=null)
        {
            Join<RelatedPersonEntity, RelatedPersonIdentifier> join = root.join("identifiers", JoinType.LEFT);
            Join<RelatedPersonIdentifier, SystemEntity> joinSystem = join.join("systemEntity",JoinType.LEFT);

            Predicate pvalue = builder.like(
                    builder.upper(join.get("value")),
                    builder.upper(builder.literal(daoutils.removeSpace(identifier.getValue())))
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
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }


        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<RelatedPersonEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<RelatedPersonEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
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
        List<RelatedPersonEntity> qryResults = null;
        TypedQuery<RelatedPersonEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);


        qryResults = typedQuery.getResultList();

        log.debug("Found RelatedPersons = "+qryResults.size());

        return qryResults;
    }


}

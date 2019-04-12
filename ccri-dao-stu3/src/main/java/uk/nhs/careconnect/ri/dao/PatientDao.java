package uk.nhs.careconnect.ri.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
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
import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.SystemEntity;
import uk.nhs.careconnect.ri.database.entity.carePlan.CarePlanEntity;
import uk.nhs.careconnect.ri.database.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.database.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.database.entity.encounter.EncounterExtension;
import uk.nhs.careconnect.ri.database.entity.flag.FlagEntity;
import uk.nhs.careconnect.ri.database.entity.immunisation.ImmunisationEntity;
import uk.nhs.careconnect.ri.database.entity.medicationStatement.MedicationStatementEntity;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.database.entity.patient.*;
import uk.nhs.careconnect.ri.database.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.database.entity.procedure.ProcedureEntity;
import uk.nhs.careconnect.ri.database.entity.allergy.AllergyIntoleranceEntity;

import uk.org.hl7.fhir.core.Stu3.CareConnectExtension;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;

@Repository
@Transactional
public class PatientDao implements PatientRepository {


    @PersistenceContext
    EntityManager em;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private PatientEntityToFHIRPatientTransformer patientEntityToFHIRPatientTransformer;

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
    private MedicationStatementEntityToFHIRMedicationStatementTransformer medicationStatementEntityToFHIRMedicationStatementTransformer;

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

    @Autowired
    private FlagEntityToFHIRFlagTransformer flagEntityToFHIRFlagTransformer;

    @Autowired
    private CarePlanEntityToFHIRCarePlanTransformer carePlanEntityToFHIRCarePlanTransformer;

    @Autowired
    private LibDao libDao;

    @Autowired
    private DocumentReferenceEntityToFHIRDocumentReferenceTransformer documentReferenceEntityToFHIRDocumentReferenceTransformer;
    private static final Logger log = LoggerFactory.getLogger(PatientDao.class);


    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(PatientEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Transactional
    @Override
    public void save(FhirContext ctx, PatientEntity patient)
    {


        em.persist(patient);
    }

    @Override
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

    @Override
    public PatientEntity readEntity(FhirContext ctx,IdType theId) {

        return  (PatientEntity) em.find(PatientEntity.class,Long.parseLong(theId.getIdPart()));

    }

    @Override
    public Patient update(FhirContext ctx, Patient patient, IdType theId, String theConditional) throws OperationOutcomeException {

        PatientEntity patientEntity = null;
        log.info("Started patient updated");
        if (theId != null) {
            log.trace("theId.getIdPart()="+theId.getIdPart());
            patientEntity = (PatientEntity) em.find(PatientEntity.class, Long.parseLong(theId.getIdPart()));
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

                    List<PatientEntity> results = searchEntity(ctx, null, null,null, null, null,null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/PPMIdentifier"), null,null,null, null,null);
                    log.trace("Loop over results");
                    for (PatientEntity pat : results) {
                        patientEntity = pat;
                        break;
                    }
                    // This copes with the new identifier being added.
                    if (patientEntity == null && daoutils.isNumeric(spiltStr[1])) {
                        log.trace("Looking for patient with id of "+spiltStr[1]);
                        patientEntity = (PatientEntity) em.find(PatientEntity.class, Long.parseLong(spiltStr[1]));
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }


        if (patientEntity == null) {
            log.trace("Adding new Patient");
            patientEntity = new PatientEntity();
        }
        patientEntity.setResource(null);
        if (patient.hasExtension())
        {
            for (Extension extension : patient.getExtension()) {
                switch (extension.getUrl()) {
                    case CareConnectExtension.UrlEthnicCategory :
                        CodeableConcept ethnic = (CodeableConcept) extension.getValue();
                        ConceptEntity code = conceptDao.findCode(ethnic.getCoding().get(0));
                        if (code != null) { patientEntity.setEthnicCode(code); }
                        else {
                            String message = "Ethnic: Missing System/Code = "+ ethnic.getCoding().get(0).getSystem() +" code = "+ethnic.getCoding().get(0).getCode();
                            log.error(message);
                            throw new OperationOutcomeException("Patient",message, OperationOutcome.IssueType.CODEINVALID);
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
            ConceptEntity code = conceptDao.findAddCode(martial.getCoding().get(0));
            if (code != null) { patientEntity.setMaritalCode(code); }
            else {
                String message = "Marital: Missing System/Code = "+ martial.getCoding().get(0).getSystem() +" code = "+martial.getCoding().get(0).getCode();
                log.error(message);
                throw new OperationOutcomeException("Patient",message, OperationOutcome.IssueType.CODEINVALID);
            }
        }

        if (patient.hasManagingOrganization()) {
            OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(patient.getManagingOrganization().getReference()));
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

        // Remove all identifiers with systems that match the updated Patient, leave unmatched identifiers alone.
        for (PatientIdentifier orgSearch : patientEntity.getIdentifiers()) {
            Boolean found = false;
            for (Identifier identifier : patient.getIdentifier()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri())) {
                    found = true;
                }
            }
            if (found) {
                em.remove(orgSearch);
            }
        }

        for (Identifier identifier : patient.getIdentifier()) {
            if (identifier.getSystem().equals(CareConnectSystem.NHSNumber) && identifier.getExtension().size()>0) {
                CodeableConcept nhsVerification = (CodeableConcept) identifier.getExtension().get(0).getValue();
                ConceptEntity code = conceptDao.findCode(nhsVerification.getCoding().get(0));
                if (code != null) { patientEntity.setNHSVerificationCode(code); }
                else {

                    String message = "NHS Verification: Missing System/Code = "+ nhsVerification.getCoding().get(0).getSystem() +" code = "+nhsVerification.getCoding().get(0).getCode();
                    log.error(message);
                    throw new OperationOutcomeException("Patient",message, OperationOutcome.IssueType.CODEINVALID);
                }
            }
            PatientIdentifier patientIdentifier = null;
            /* No longer necessary
            for (PatientIdentifier orgSearch : patientEntity.getIdentifiers()) {
                // overwrites existing
                if (identifier.getSystem().equals(orgSearch.getSystemUri())) {

                }
                    if ( identifier.getValue().equals(orgSearch.getValue())) {
                    patientIdentifier = orgSearch;
                    break;
                }
            }
            */
            if (patientIdentifier == null) {
                patientIdentifier = new PatientIdentifier();
                patientEntity.addIdentifier(patientIdentifier);
            }

            patientIdentifier = (PatientIdentifier) libDao.setIdentifier(identifier,  patientIdentifier);
            patientIdentifier.setPatient(patientEntity);

            em.persist(patientIdentifier);
        }
        em.persist(patientEntity);

        for (PatientName nameSearch : patientEntity.getNames()) {
           em.remove(nameSearch);
        }
        patientEntity.setNames(new ArrayList<>());

        for (HumanName name : patient.getName()) {
            PatientName patientName = null;
            /*
            for (PatientName nameSearch : patientEntity.getNames()) {
                // look for matching surname and also if the have matching given name
                if (nameSearch.getFamilyName().equals(name.getFamily())) {
                    if (name.getGiven().size()> 0) {
                        if (nameSearch.getGivenName().equals(name.getGiven().get(0).getValue())) {
                            patientName = nameSearch;
                            break;
                        }
                    }
                    else {
                        patientName = nameSearch;
                        break;
                    }
                }
            }
            */
            if (patientName == null)  {
                patientName = patientEntity.addName();
                patientName.setPatientEntity(patientEntity);
            }

            patientName.setFamilyName(name.getFamily());
            if (name.getGiven().size()>0)
                patientName.setGivenName(name.getGiven().get(0).getValue());
            if (name.getPrefix().size()>0)
                patientName.setPrefix(name.getPrefix().get(0).getValue());
            if (name.getUse() != null) {
                patientName.setNameUse(name.getUse());
            }
            em.persist(patientName);
        }

        // Temp fix to remove old addresses 16/8/2018 KGM
        for (PatientAddress adrSearch : patientEntity.getAddresses()) {
            em.remove(adrSearch);
        }
        for (Address address : patient.getAddress()) {
            PatientAddress patientAdr = null;
            /*
            for (PatientAddress adrSearch : patientEntity.getAddresses()) {
                // look for matching postcode and first line of address
                if (adrSearch.getAddress().getPostcode().equals(address.getPostalCode())) {
                    if (address.hasLine() && address.getLine().get(0)!=null && adrSearch.getAddress().getAddress1().equals(address.getLine().get(0))) {
                        patientAdr = adrSearch;
                        break;
                    }
                }
            }*/
            if (patientAdr == null) {
                patientAdr = new PatientAddress();
                patientAdr.setPatientEntity(patientEntity);
                patientEntity.addAddress(patientAdr);
            }

            AddressEntity addr = patientAdr.getAddress();
            if (addr == null) {
                addr = patientAdr.setAddress(new AddressEntity());
            }

            if (address.getLine().size()>0) addr.setAddress1(address.getLine().get(0).getValue().trim());
            if (address.getLine().size()>1) addr.setAddress2(address.getLine().get(1).getValue().trim());
            if (address.getLine().size()>2) addr.setAddress3(address.getLine().get(2).getValue().trim());
            if (address.getCity() != null) addr.setCity(address.getCity());
            if (address.getDistrict() != null) addr.setCounty(address.getDistrict());
            if (address.getPostalCode() != null) addr.setPostcode(address.getPostalCode());
            if (address.getCountry() != null) addr.setCountry(address.getCountry());

            if (address.getUse() != null) patientAdr.setAddressUse(address.getUse());
            if (address.getType() != null) patientAdr.setAddressType(address.getType());


            em.persist(addr);
            em.persist(patientAdr);
        }
        for (PatientTelecom telSearch : patientEntity.getTelecoms()) {
            em.remove(telSearch);
        }
        for (ContactPoint contact : patient.getTelecom()) {
            PatientTelecom patientTel  = new PatientTelecom();

            patientTel.setPatientEntity(patientEntity);
            patientTel.setValue(contact.getValue());

            if (contact.hasSystem())
                patientTel.setSystem(contact.getSystem());
            if (contact.hasUse())
                patientTel.setTelecomUse(contact.getUse());
            em.persist(patientTel);
            patientEntity.addTelecom(patientTel);
        }

        for (PatientExtension extension : patientEntity.getExtensions()) {
            em.remove(extension);
        }
        if (patient.hasExtension())
        {
            for (Extension extension : patient.getExtension()) {
                switch (extension.getUrl()) {
                    case CareConnectExtension.UrlEthnicCategory :
                        // already processed
                        break;
                    default:
                        PatientExtension patientExtension = new PatientExtension();
                        patientExtension.setPatientEntity(patientEntity);
                        if (extension.hasUrl()) {
                            patientExtension.setUrl(extension.getUrl());
                        }
                        // TODO
                        if (extension.hasValue()) {
                            patientExtension.setValue(extension.getValue());
                        }

                        em.persist(patientExtension);
                }

            }
        }
        Patient newPatient = null;


        newPatient = patientEntityToFHIRPatientTransformer.transform(patientEntity);

        //em.persist(patientEntity);


        return newPatient;
    }

    @Override
    public List<Resource> search (FhirContext ctx,
                                             @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
                                             @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
                                             @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
                                             @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
                                             @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
                                             @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
                                             @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
                                             @OptionalParam(name= Patient.SP_NAME) StringParam name,
                                             @OptionalParam(name= Patient.SP_PHONE) StringParam phone
            , @OptionalParam(name= Patient.SP_RES_ID) StringParam resid
            ,@IncludeParam(reverse=true, allow = {"Observation:patient","Procedure:patient","Encounter:patient","DocumentReference:patient","*"}) Set<Include> reverseIncludes
            ,@IncludeParam(allow= {
                "Patient:general-practitioner"
                ,"Patient:organization"
                , "*"}) Set<Include> includes
    ) {
        List<PatientEntity> qryResults = searchEntity(ctx, addressPostcode, birthDate, email, familyName, gender, givenName, identifier, name, phone,resid, reverseIncludes,includes);
        List<Resource> results = new ArrayList<>();

        for (PatientEntity patientEntity : qryResults)
        {
            Patient patient;
            if (patientEntity.getResource() != null) {
                patient = (Patient) ctx.newJsonParser().parseResource(patientEntity.getResource());
            } else {
                    patient = patientEntityToFHIRPatientTransformer.transform(patientEntity);
                    String resourceStr = ctx.newJsonParser().encodeResourceToString(patient);
                    patientEntity.setResource(resourceStr);
                    em.persist(patientEntity);
            }
            results.add(patient);

            // If reverse include selected

        }

        if (reverseIncludes!= null || includes!=null) {
            //log.info("Reverse includes");
            for (PatientEntity patientEntity : qryResults) {
                if (includes.size() > 0) {
                    for (Include include : includes) {
                        log.info("_Include  = "+include.getValue());
                        switch (include.getValue()) {
                            case "Patient:general-practitioner":
                                PractitionerEntity practitioner = patientEntity.getGP();
                                if (practitioner != null)
                                    results.add(practitionerEntityToFHIRPractitionerTransformer.transform(practitioner));
                                break;
                            case "Patient:organization":
                                OrganisationEntity organisation = patientEntity.getPractice();
                                if (organisation != null)
                                    results.add(organisationEntityToFHIROrganizationTransformer.transform(organisation));
                                break;
                        }
                    }
                }
                if (reverseIncludes.size() > 0) {

                    for (Include revinclude : reverseIncludes) {

                        log.debug("_revInclude  = "+revinclude.getValue());
                        switch (revinclude.getValue()) {

                            case "*":
                                for (ProcedureEntity procedureEntity : patientEntity.getPatientProcedures()) {
                                    results.add(procedureEntityToFHIRProcedureTransformer.transform(procedureEntity));
                                }

                                for (ConditionEntity conditionEntity : patientEntity.getPatientConditions()) {
                                    results.add(conditionEntityToFHIRConditionTransformer.transform(conditionEntity));
                                }
                                //allegry
                                for (AllergyIntoleranceEntity allergy : patientEntity.getPatientAlelrgies()) {
                                    results.add(allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer.transform(allergy));
                                }
                                //immunisation
                                for (ImmunisationEntity immunisation : patientEntity.getPatientImmunisations()) {
                                    results.add(immunisationEntityToFHIRImmunizationTransformer.transform(immunisation));
                                }

                                for (MedicationStatementEntity medicationStatementEntity : patientEntity.getMedicationStatements()) {
                                    if (medicationStatementEntity.getStatus() != null && medicationStatementEntity.getStatus().equals(MedicationStatement.MedicationStatementStatus.ACTIVE)) {
                                        results.add(medicationStatementEntityToFHIRMedicationStatementTransformer.transform(medicationStatementEntity));
                                    }
                                }


                                for (EncounterEntity encounterEntity : patientEntity.getPatientEncounters()) {
                                    results.add(encounterEntityToFHIREncounterTransformer.transform(encounterEntity));
                                }

                                for (DocumentReferenceEntity documentReferenceEntity : patientEntity.getPatientDocuments()) {
                                    results.add(documentReferenceEntityToFHIRDocumentReferenceTransformer.transform(documentReferenceEntity));
                                }

                                for (FlagEntity flagEntity : patientEntity.getPatientFlags()) {
                                    results.add(flagEntityToFHIRFlagTransformer.transform(flagEntity));
                                }
                                break;
                            case "AllergyIntolerance:patient":
                                for (AllergyIntoleranceEntity allergy : patientEntity.getPatientAlelrgies()) {
                                    results.add(allergyIntoleranceEntityToFHIRAllergyIntoleranceTransformer.transform(allergy));
                                }
                                break;
                            case "CarePlan:patient":
                                for (CarePlanEntity carePlanEntity : patientEntity.getPatientCarePlans()) {
                                    results.add(carePlanEntityToFHIRCarePlanTransformer.transform(carePlanEntity));
                                }
                                break;
                            case "Condition:patient":
                                for (ConditionEntity conditionEntity : patientEntity.getPatientConditions()) {
                                    results.add(conditionEntityToFHIRConditionTransformer.transform(conditionEntity));
                                }
                                break;
                            case "MedicationStatement:patient":
                                for (MedicationStatementEntity medicationStatementEntity : patientEntity.getMedicationStatements()) {
                                    if (medicationStatementEntity.getStatus() != null && medicationStatementEntity.getStatus().equals(MedicationStatement.MedicationStatementStatus.ACTIVE)) {
                                        results.add(medicationStatementEntityToFHIRMedicationStatementTransformer.transform(medicationStatementEntity));
                                    }
                                }
                                break;
                            case "Immunization:patient":
                                for (ImmunisationEntity immunisation : patientEntity.getPatientImmunisations()) {
                                    results.add(immunisationEntityToFHIRImmunizationTransformer.transform(immunisation));
                                }
                                break;

                            case "Observation:patient":
                                for (ObservationEntity observationEntity : patientEntity.getPatientObservations()) {
                                    if (observationEntity.getParentObservation() == null) {
                                        results.add(observationEntityToFHIRObservationTransformer.transform(observationEntity));
                                    }
                                }
                                break;

                            case "Procedure:patient":
                                for (ProcedureEntity procedureEntity : patientEntity.getPatientProcedures()) {
                                    results.add(procedureEntityToFHIRProcedureTransformer.transform(procedureEntity));
                                }
                                break;
                            case "Encounter:patient":
                                for (EncounterEntity encounterEntity : patientEntity.getPatientEncounters()) {
                                    results.add(encounterEntityToFHIREncounterTransformer.transform(encounterEntity));
                                }
                                break;
                            case "DocumentReference:patient":
                                for (DocumentReferenceEntity documentReferenceEntity : patientEntity.getPatientDocuments()) {
                                    results.add(documentReferenceEntityToFHIRDocumentReferenceTransformer.transform(documentReferenceEntity));
                                }
                                break;
                            case "Flag:patient":
                                for (FlagEntity flagEntity : patientEntity.getPatientFlags()) {
                                    results.add(flagEntityToFHIRFlagTransformer.transform(flagEntity));
                                }
                                break;
                        }
                    }
                }
            }
        }
        return results;
    }

    @Override
    public List<PatientEntity> searchEntity (FhirContext ctx,
            @OptionalParam(name= Patient.SP_ADDRESS_POSTALCODE) StringParam addressPostcode,
            @OptionalParam(name= Patient.SP_BIRTHDATE) DateRangeParam birthDate,
            @OptionalParam(name= Patient.SP_EMAIL) StringParam email,
            @OptionalParam(name = Patient.SP_FAMILY) StringParam familyName,
            @OptionalParam(name= Patient.SP_GENDER) StringParam gender ,
            @OptionalParam(name= Patient.SP_GIVEN) StringParam givenName ,
            @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name= Patient.SP_NAME) StringParam name,
            @OptionalParam(name= Patient.SP_PHONE) StringParam phone
            , StringParam resid
            ,@IncludeParam(reverse=true, allow = {"Observation:encounter","Procedure:patient","Encounter:patient","DocumentReference:patient","*"}) Set<Include> reverseIncludes
            ,@IncludeParam(allow= {
                "Patient:general-practitioner"
                ,"Patient:organization"
                , "*"}) Set<Include> includes
    )
    {


        CriteriaBuilder builder = em.getCriteriaBuilder();

        // KGM 18/12/2017 Added distinct
        CriteriaQuery<PatientEntity> criteria = builder.createQuery(PatientEntity.class).distinct(true);
        Root<PatientEntity> root = criteria.from(PatientEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<PatientEntity> results = new ArrayList<PatientEntity>();

        if (identifier !=null)
        {
            Join<PatientEntity, PatientIdentifier> join = root.join("identifiers", JoinType.LEFT);
            Join<PatientIdentifier, SystemEntity> joinSystem = join.join("systemEntity",JoinType.LEFT);

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
            // KGM 4/1/2018 to support null search
            Predicate p = null;
            if (!gender.getValueNotNull().isEmpty()) {
                p = builder.equal(
                        builder.upper(root.get("gender"))
                        , builder.upper(builder.literal(gender.getValue()))
                );
            } else {
                p = builder.isNull(root.get("gender"));
            }
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
        TypedQuery<PatientEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);

        if (birthDate != null) {
            if (birthDate.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, birthDate.getLowerBoundAsInstant(), TemporalType.DATE);
            if (birthDate.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, birthDate.getUpperBoundAsInstant(), TemporalType.DATE);
        }

        qryResults = typedQuery.getResultList();

        log.debug("Found Patients = "+qryResults.size());

        return qryResults;
    }


}

package uk.nhs.careconnect.ri.database.jpatest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.*;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hl7.fhir.dstu3.model.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import uk.nhs.careconnect.ri.database.daointerface.*;
import uk.nhs.careconnect.ri.database.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.Terminology.ConceptParentChildLink;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;


public class JPAStepsDef {

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    ValueSetRepository valueSetRepository;

    @Autowired
    CodeSystemRepository codeSystemDao;

    @Autowired
    @Lazy
    ConceptRepository conceptDao;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    PractitionerRepository practitionerRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    ObservationRepository observationRepository;

    @Autowired
    EncounterRepository encounterRepository;

    @Autowired
    ConditionRepository conditionRepository;

    @Autowired
    PractitionerRoleRepository practitionerRoleRepository;

    @Autowired
    AllergyIntoleranceRepository allergyIntoleranceRepository;

    @Autowired
    ImmunizationRepository immunizationRepository;

    @Autowired
    ProcedureRepository procedureRepository;

    @Autowired
    MedicationRequestRepository prescribingRepository;

    @Autowired
    MedicationStatementRepository statementRepository;

    @Autowired
    DocumentReferenceRepository documentReferenceRepository;

    @Autowired
    ReferralRequestRepository referralRequestRepository;

    @Autowired
    HealthcareServiceRepository serviceRepository;

    @Autowired
    ListRepository listRepository;

    @Autowired
    QuestionnaireResponseRepository questionnaireResponseRepository;

    @Autowired
    RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    CarePlanRepository carePlanRepository;

    @Autowired
    ClinicalImpressionRepository impressionRepository;

    @Autowired
    ConsentRepository consentRepository;

    @Autowired
    TerminologyLoader myTermSvc;

    Patient patient;

    Location location;
    Resource resource;

    Observation observation;
    PractitionerRole practitionerRole;

    CodeSystemEntity cs;

    List<Resource> patientList = null;

    List<Organization> organizationList = null;

    List<Resource> encounterList = null;

    List<Condition> conditionList = null;

    List<Practitioner> practitionerList = null;
    List<Location> locationList = null;

    List<Resource> observationList = null;

    List<AllergyIntolerance> allergyList = null;

    List<Immunization> immunisationList = null;

    List<Procedure> procedureList = null;

    List<MedicationRequest> prescribingList;

    List<ListResource> listList;

    List<Resource> carePlanList;

    List<RiskAssessment> riskAssessmentList;

    List<QuestionnaireResponse> questionnaireResponseList;

    Transaction tx;

    static Boolean initialized = false;

    public  static FhirContext ctx = FhirContext.forDstu3();
    private static final FhirContext ourCtx = FhirContext.forDstu3();

    private static final String CS_URL = "http://example.com/my_code_system";
    



    private static FhirContext ctxFHIR = FhirContext.forDstu3();
    private static FhirValidator validator = ctxFHIR.newValidator();


    @Given("^I add a Patient with an Id of (\\d+)$")
    public void i_add_a_Patient_with_an_Id_of(Integer id) throws Throwable {
        patient = patientRepository.read(ctx,new IdType().setValue(id.toString()));
    }

    @Given("^I search for a Patient with a family name of (\\w+)$")
    public void i_search_for_a_Patient_with_a_family_name_of(String name) throws Throwable {
        patientList = patientRepository.search(ctx,null,null,null, new StringParam(name), null, null,null, null,null,null, null,null);
    }

    @Given("^I search for a Patient with a given name of (\\w+)$")
    public void i_search_for_a_Patient_with_a_given_name_of(String name) throws Throwable {
        patientList = patientRepository.search(ctx,null,null,null, null, null, new StringParam(name),null, null,null,null,null,null);
    }



    @Given("^I search for a Patient with a gender of (\\w+)$")
    public void i_search_for_a_Patient_with_a_gender_of(String gender) throws Throwable {
        patientList = patientRepository.search(ctx,null,null,null,null,new StringParam(gender), null,null,null, null,null,null,null);
    }

    @Given("^I search for a Patient with a NHSNumber of (\\d+)$")
    public void i_search_for_a_Patient_with_a_NHSNumber_of(String NHSNumber) throws Throwable {
        patientList = patientRepository.search(ctx,null,null,null,null,null,null,new TokenParam().setSystem(CareConnectSystem.NHSNumber).setValue(NHSNumber),null,null,null,null,null);
    }

    @Given("^I search for a Patient with a name of \"([^\"]*)\"$")
    public void i_search_for_a_Patient_with_a_name_of(String name) throws Throwable {
        patientList = patientRepository.search(ctx,null,null,null,null,null,null, null,new StringParam(name),null,null,null,null);
    }

    @Given("^I search for a Patient with a birthdate of '(\\w+)-(\\w+)-(\\w+)'$")
    public void i_search_for_a_Patient_with_a_birthdate_of(String year, String month, String day) throws Throwable {
        patientList = patientRepository.search(ctx,null, new DateRangeParam(new DateParam(ParamPrefixEnum.EQUAL,year+"-"+month+"-"+day)),null,null,null,null, null,null,null,null,null,null);

    }


    @Given("^I search for a Patient with a email of \"([^\"]*)\"$")
    public void i_search_for_a_Patient_with_a_email_of(String email) throws Throwable {
        patientList = patientRepository.search(ctx,null,null,new StringParam(email),null,null,null, null,null,null,null,null,null);

    }

    @Given("^I search for a Patient with a address-postcode of \"([^\"]*)\"$")
    public void i_search_for_a_Patient_with_a_address_postcode_of(String postcode) throws Throwable {
        patientList = patientRepository.search(ctx,new StringParam(postcode),null,null,null,null,null, null,null,null,null,null,null);
    }
    @Given("^I search for a Patient with a phone of \"([^\"]*)\"$")
    public void i_search_for_a_Patient_with_a_phone_of(String phone) throws Throwable {
        patientList = patientRepository.search(ctx,null,null,null,null,null,null, null,null,new StringParam(phone),null,null,null);
    }


    @Given("^I add a ValueSet with an Id of ([^\"]*)$")
    public void i_add_a_ValueSet_with_an_Id_of(String valueSetId) throws Throwable {
        resource = (Resource) valueSetRepository.read(new IdType().setValue("ValueSet/"+valueSetId));
    }

    @Then("^the result should be a FHIR ValueSet$")
    public void the_result_should_be_a_FHIR_ValueSet() throws Throwable {
        Assert.assertThat(resource,instanceOf(ValueSet.class));
    }


    // ORGANISATION


    @Given("^I search for Organisations by name (\\w+)$")
    public void i_search_for_Organisations_by_name(String name) throws Throwable {
        organizationList = organisationRepository.searchOrganization(ctx,null,new StringParam(name),null,null);
    }

    @Then("^the results should be a list of CareConnect Organisations$")
    public void the_results_should_be_a_list_of_CareConnect_Organisations() throws Throwable {
        for (Organization organization : organizationList) {
            validateResource(organization);
        }
    }

    @Given("^I search for Organisations by SDSCode (\\w+)$")
    public void i_have_search_for_these_Organisations_by_SDSCode(String SDSCode) throws Throwable {
        organizationList = organisationRepository.searchOrganization(ctx,new TokenParam().setSystem(CareConnectSystem.ODSOrganisationCode).setValue(SDSCode),null,null,null);
    }

    @Then("^the result should be a organisation list with (\\d+) entry$")
    public void the_result_should_be_a_organisation_list_with_entry(int count) throws Throwable {
        Assert.assertNotNull(organizationList);
        Assert.assertEquals(count,organizationList.size());
    }
    @Then("^the result should be a list with (\\d+) entry$")
    public void the_result_should_be_a_valid_FHIR_Bundle_with_entry(int count) throws Throwable {
        Assert.assertNotNull(patientList);
        Assert.assertEquals(count,patientList.size());

    }

    @Then("^the result should be a list with several entries$")
    public void the_result_should_be_a_list_with_several_entries() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        Assert.assertTrue(patientList.size() > 0);
    }

    @Then("^they shall all be FHIR Patient resources$")
    public void they_shall_all_be_FHIR_Patient_resources() throws Throwable {

        for (Resource resource : patientList) {
            Assert.assertThat(resource,instanceOf(Patient.class));
        }
    }

    @Then("^the result should be a FHIR Patient$")
    public void the_result_should_be_a_valid_FHIR_Patient() throws Throwable {
        System.out.println("Patient found = "+patient.getId()+ " "+patient.getName().get(0).getFamily());
        Assert.assertNotNull(patient);
        Assert.assertThat(patient,instanceOf(Patient.class));
    }

    @Then("^they shall all be FHIR Organization resources$")
    public void they_shall_all_be_FHIR_Organization_resources() throws Throwable {
        for (Organization organization : organizationList) {
            Assert.assertThat(organization,instanceOf(Organization.class));
        }
    }

    @Then("^the results should be a CareConnect Patient$")
    public void the_results_should_be_valid_CareConnect_Patients() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        validateResource(patient);
    }

    @Then("^the results should be a list of CareConnect Patients$")
    public void the_results_should_be_valid_CareConnect_Patient() throws Throwable {
        for (Resource resource : patientList) {
            validateResource(resource);
        }
    }

    @Given("^I add a dummy codesystem$")
    public void i_add_a_dummy_codesystem() throws Throwable {
        // Write code here that turns the phrase above into concrete actions

        Session session =  conceptDao.getSession();
         tx = conceptDao.getTransaction(session);
      //  conceptRepository.beginTransaction(tx);
        cs = codeSystemDao.findBySystem(CS_URL);


        ConceptEntity parent = new ConceptEntity();
        parent.setCodeSystem(cs);
        parent.setCode("parent");
        cs.getConcepts().add(parent);

        ConceptEntity child = new ConceptEntity();
        child.setCodeSystem(cs);
        child.setCode("child");
        parent.addChild(child, ConceptParentChildLink.RelationshipTypeEnum.ISA);
    // use something like this for circular references
     //   child.addChild(parent, ConceptParentChildLink.RelationshipTypeEnum.ISA);
    }

    @Then("^the CodeSystem should save$")
    public void the_CodeSystem_should_save() throws Throwable {
        try {
            conceptDao.storeNewCodeSystemVersion( cs,null);
           // fail();
        } catch (InvalidRequestException e) {
            assertEquals("CodeSystem contains circular reference around code parent", e.getMessage());
        }
    }


    //PRACTITIONER



    @Then("^the result should be a practitioner list with (\\d+) entry$")
    public void the_result_should_be_a_practitioner_list_with_entry(int count) throws Throwable {
        Assert.assertNotNull(practitionerList);
        Assert.assertEquals(count,practitionerList.size());
    }

    @Then("^they shall all be FHIR Practitioner resources$")
    public void they_shall_all_be_FHIR_Practitioner_resources() throws Throwable {
        for (Practitioner practitioner : practitionerList) {
            Assert.assertThat(practitioner,instanceOf(Practitioner.class));
        }
    }

    @Then("^the results should be a list of CareConnect Practitioners$")
    public void the_results_should_be_a_list_of_CareConnect_Practitioners() throws Throwable {
        for (Practitioner practitioner : practitionerList) {
            validateResource(practitioner);
        }
    }

    @Given("^I search for Practitioners by SDSId (\\w+)$")
    public void i_search_for_Practitioners_by_SDSId_S(String Id) throws Throwable {
        practitionerList = practitionerRepository.searchPractitioner(ctx, new TokenParam().setSystem(CareConnectSystem.SDSUserId).setValue(Id),null,null,null);
    }

    @Given("^I search for Practitioners by name (\\w+)$")
    public void i_search_for_Practitioners_by_name_Bhatia(String name) throws Throwable {
        practitionerList = practitionerRepository.searchPractitioner(ctx,null,new StringParam(name),null,null);
    }


    // Location

    @Given("^I search for Locations by SDSCode (\\w+)$")
    public void i_search_for_Locations_by_SDSCode(String code) throws Throwable {
        locationList = locationRepository.searchLocation(ctx,new TokenParam().setSystem(CareConnectSystem.ODSSiteCode).setValue(code),null,null,null);
    }

    @Then("^the result should be a Location list with (\\d+) entry$")
    public void the_result_should_be_a_Location_list_with_entry(int count) throws Throwable {
        Assert.assertNotNull(locationList);
        Assert.assertEquals(count,locationList.size());
    }

    @Then("^they shall all be FHIR Location resources$")
    public void they_shall_all_be_FHIR_Location_resources() throws Throwable {
        for (Location location : locationList) {
            Assert.assertThat(location,instanceOf(Location.class));
        }
    }

    @Then("^the results should be a list of CareConnect Locations$")
    public void the_results_should_be_a_list_of_CareConnect_Locations() throws Throwable {
        for (Location location : locationList) {
            validateResource(location);
        }
    }


    @Given("^I search for Locations by name (\\w+)$")
    public void i_search_for_Locations_by_name(String name) throws Throwable {
        locationList = locationRepository.searchLocation(ctx,null,new StringParam(name),null,null);
    }

    @Given("^Location resource file$")
    public void location_resource_file() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/Location.xml");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        location = ctx.newXmlParser().parseResource(Location.class,reader);
    }

    @Given("^Observation resource file$")
    public void observation_resource_file() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/Observation.xml");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        observation = ctx.newXmlParser().parseResource(Observation.class,reader);
    }

    @Given("^PractitionerRole resource file$")
    public void practitionerrole_resource_file() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/PractitionerRole.xml");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        practitionerRole = ctx.newXmlParser().parseResource(PractitionerRole.class,reader);
    }

    @Then("^save the PractitionerRole$")
    public void save_the_PractitionerRole() throws Throwable {
       practitionerRoleRepository.create(ctx, practitionerRole,null,null);
    }

    @Then("^save the Observation$")
    public void save_the_Observation() throws Throwable {
        observationRepository.save(ctx,observation,null,null);
    }

    @Then("^save the location$")
    public void save_the_location() throws Throwable {
        locationRepository.create(ctx,location,null,"Location?identifier="+location.getIdentifier().get(0).getSystem()+"%7C"+location.getIdentifier().get(0).getValue());
    }


    @Given("^I have two sample resources loaded$")
    public void i_have_two_sample_resources_loaded() throws Throwable {

    }

    @When("^I search Observations on SNOMED category (\\d+)$")
    public void i_search_on_SNOMED_category(String category) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        observationList = observationRepository.search(ctx,new TokenParam().setValue(category).setSystem(CareConnectSystem.SNOMEDCT),null,null,null,null,null, null,null);
    }

    @Then("^I should get a Bundle of Observations with (\\d+) resource$")
    public void i_should_get_a_Bundle_of_Observations_with_resource(int count) throws Throwable {

        assertEquals(count,observationList.size());
    }

    @When("^I search Observations on SNOMED code (\\d+)$")
    public void i_search_on_SNOMED_code(String code) throws Throwable {
        observationList = observationRepository.search(ctx,null, new TokenOrListParam().add(new TokenParam().setValue(code).setSystem(CareConnectSystem.SNOMEDCT)),null,null, null,null, null,null);
    }

    @When("^I search on Patient ID = (\\d+)$")
    public void i_search_on_Patient_ID(String patientId) throws Throwable {
        observationList = observationRepository.search(ctx,null, null, null, new ReferenceParam("Patient/"+patientId),null,null, null,null);
    }

    @Then("^I should get a Bundle of Observations with more then (\\d+) resources$")
    public void i_should_get_a_Bundle_of_Observations_with_more_than_resource(int count) throws Throwable {
        assertTrue("Expected "+count+" and actual "+ observationList.size(),count<observationList.size());
    }

    @When("^I search on dates less than (\\d+)-(\\d+)-(\\d+)$")
    public void dates_less_than_yyyymmdd(String year, String month, String day) throws Throwable {
        observationList = observationRepository.search(ctx,null, null, new DateRangeParam(new DateParam(ParamPrefixEnum.LESSTHAN,year+"-"+month+"-"+day)), null,null,null, null, null);
    }

    @When("^I search on dates equal to (\\d+)-(\\d+)-(\\d+)$")
    public void dates_equal_yyyymmdd(String year, String month, String day) throws Throwable {
        observationList = observationRepository.search(ctx,null, null, new DateRangeParam(new DateParam(ParamPrefixEnum.EQUAL,year+"-"+month+"-"+day)), null,null,null, null, null);
    }

    @When("^I search on dates equal to (\\d+)-(\\d+)$")
    public void dates_equal_yyyymm(String year, String month) throws Throwable {
        observationList = observationRepository.search(ctx,null, null, new DateRangeParam(new DateParam(ParamPrefixEnum.EQUAL,year+"-"+month)), null, null,null, null,null);
    }

    @When("^I search on dates equal to (\\d+)$")
    public void dates_equal_yyyy(String year) throws Throwable {
        observationList = observationRepository.search(ctx,null, null, new DateRangeParam(new DateParam(ParamPrefixEnum.EQUAL,year)), null, null,null, null, null);
    }

    /*

    ENCOUNTER

     */

    @Given("^I have one Encounter resource loaded$")
    public void i_have_one_Encounter_resource_loaded() throws Throwable {
        assertNotNull(encounterRepository.read(ctx,new IdType(1)));
    }

    @When("^I update this Encounter$")
    public void i_update_this_Encounter() throws Throwable {


        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("json/EncounterExampleTwo.json");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        Encounter encounter = ctx.newJsonParser().parseResource(Encounter.class, reader);
        try {
            encounter = encounterRepository.create(ctx,encounter,null,"Encounter?identifier=" + encounter.getIdentifier().get(0).getSystem() + "%7C" +encounter.getIdentifier().get(0).getValue());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @When("^I search Encounter on Patient ID = (\\d+)$")
    public void i_search_Encounter_on_Patient_ID(int patient) throws Throwable {
        encounterList = encounterRepository.search(ctx, new ReferenceParam("Patient/"+patient),null,null, null,null,null, null);
    }

    @Then("^I should get a Bundle of Encounter (\\d+) resource$")
    public void i_should_get_a_Bundle_of_Encounter_resource(int count) throws Throwable {
        assertEquals(count,encounterList.size());
    }


    /*

    CONDITION

     */
    @Given("^I have one Condition resource loaded$")
    public void i_have_one_Condition_resource_loaded() throws Throwable {
        assertNotNull(conditionRepository.read(ctx,new IdType(1)));
    }

    @When("^I search Condition on Patient ID = (\\d+)$")
    public void i_search_Condition_on_Patient_ID(int patient) throws Throwable {
        conditionList = conditionRepository.search(ctx, new ReferenceParam("Patient/"+patient),null,null, null, null,null);
    }

    @Then("^I should get a Bundle of Condition (\\d+) resource$")
    public void i_should_get_a_Bundle_of_Condition_resource(int count) throws Throwable {
        assertEquals(count,conditionList.size());
    }

    @When("^I update this Condition$")
    public void i_update_this_Condition() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("json/ConditionExampleTwo.json");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        Condition condition = ctx.newJsonParser().parseResource(Condition.class, reader);
        try {
            condition = conditionRepository.create(ctx,condition,null,"Condition?identifier=" + condition.getIdentifier().get(0).getSystem() + "%7C" +condition.getIdentifier().get(0).getValue());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Then("^the results should be a list of CareConnect Conditions$")
    public void the_results_should_be_a_list_of_CareConnect_Conditions() throws Throwable {
        for (Condition condition: conditionList) {
            validateResource(condition);
        }
    }

    /*

    ALLERGY INTOLERANCE

     */

    @Given("^I have one AllergyIntolerance resource loaded$")
    public void i_have_one_AllergyIntolerance_resource_loaded() throws Throwable {
        assertNotNull(allergyIntoleranceRepository.read(ctx,new IdType(1)));
    }

    @When("^I search AllergyIntolerance on Patient ID = (\\d+)$")
    public void i_search_AllergyIntolerance_on_Patient_ID(int patient) throws Throwable {
        allergyList = allergyIntoleranceRepository.search(ctx, new ReferenceParam("Patient/"+patient),null,null, null,null);
    }
    @Then("^I should get a Bundle of AllergyIntolerance (\\d+) resource$")
    public void i_should_get_a_Bundle_of_AllergyIntolerance_resource(int count) throws Throwable {
        assertEquals(count,allergyList.size());
    }

    @Then("^the results should be a list of CareConnect AllergyIntolerance resources$")
    public void the_results_should_be_a_list_of_CareConnect_AllergyIntolerance_resources() throws Throwable {
        for (AllergyIntolerance allergy: allergyList) {
            validateResource(allergy);
        }
    }

    @When("^I update this AllergyIntolerance$")
    public void i_update_this_AllergyIntolerance() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("json/AllergyIntoleranceExampleTwo.json");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        AllergyIntolerance allergy = ctx.newJsonParser().parseResource(AllergyIntolerance.class, reader);
        try {
            allergy = allergyIntoleranceRepository.create(ctx,allergy,null,"AllergyIntolerance?identifier=" + allergy.getIdentifier().get(0).getSystem() + "%7C" +allergy.getIdentifier().get(0).getValue());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    /*

    IMMUNISATION

    */


    @Given("^I have one Immunisation resource loaded$")
    public void i_have_one_Immunisation_resource_loaded() throws Throwable {
        assertNotNull(immunizationRepository.read(ctx,new IdType(1)));
    }

    @When("^I search Immunisation on Patient ID = (\\d+)$")
    public void i_search_Immunisation_on_Patient_ID(int patient) throws Throwable {
        immunisationList = immunizationRepository.search(ctx, new ReferenceParam("Patient/"+patient),null,null, null,null);
    }

    @Then("^I should get a Bundle of Immunisation (\\d+) resource$")
    public void i_should_get_a_Bundle_of_Immunisation_resource(int count) throws Throwable {
        assertEquals(count,immunisationList.size());
    }

    @When("^I update this Immunisation$")
    public void i_update_this_Immunisation() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("json/ImmunisationExampleTwo.json");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        Immunization immunization = ctx.newJsonParser().parseResource(Immunization.class, reader);
        try {
            immunization = immunizationRepository.create(ctx,immunization,null,"Immunization?identifier=" + immunization.getIdentifier().get(0).getSystem() + "%7C" +immunization.getIdentifier().get(0).getValue());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Then("^the results should be a list of CareConnect Immunizations$")
    public void the_results_should_be_a_list_of_CareConnect_Immunizations() throws Throwable {
        for (Immunization immunization : immunisationList) {
            validateResource(immunization);
        }
    }

      /*

    MEDICATION REQUEST

    */


    @Given("^I have one MedicationRequest resource loaded$")
    public void i_have_one_MedicationRequest_resource_loaded() throws Throwable {
        assertNotNull(prescribingRepository.read(ctx,new IdType(1)));
    }

    @Given("^I have one MedicationStatement resource loaded$")
    public void i_have_one_MedicationStatement_resource_loaded() throws Throwable {
        assertNotNull(statementRepository.read(ctx,new IdType(1)));
    }

    @When("^I search MedicationRequest on Patient ID = (\\d+)$")
    public void i_search_MedicationRequest_on_Patient_ID(int patient) throws Throwable {
        prescribingList = prescribingRepository.search(ctx, new ReferenceParam("Patient/"+patient),null,null, null, null,null, null);
    }

    @Then("^I should get a Bundle of MedicationRequest (\\d+) resource$")
    public void i_should_get_a_Bundle_of_MedicationRequest_resource(int count) throws Throwable {
        assertEquals(count,prescribingList.size());
    }

    @When("^I update this MedicationRequest$")
    public void i_update_this_MedicationRequest() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("json/MedicationRequestExampleTwo.json");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        MedicationRequest prescribing = ctx.newJsonParser().parseResource(MedicationRequest.class, reader);
        try {
            prescribing = prescribingRepository.create(ctx,prescribing,null,"MedicationRequest?identifier=" + prescribing.getIdentifier().get(0).getSystem()
                    + "%7C" +prescribing.getIdentifier().get(0).getValue());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Then("^the results should be a list of CareConnect MedicationRequests$")
    public void the_results_should_be_a_list_of_CareConnect_MedicationRequests() throws Throwable {
        for (MedicationRequest prescribing : prescribingList) {
            validateResource(prescribing);
        }
    }



    /*

PROCEDURE

 */
    @Given("^I have one Procedure resource loaded$")
    public void i_have_one_Procedure_resource_loaded() throws Throwable {
        assertNotNull(procedureRepository.read(ctx,new IdType(1)));
    }

    @Given("^I have one List resource loaded$")
    public void i_have_one_List_resource_loaded() throws Throwable {
        assertNotNull(listRepository.read(ctx,new IdType(1)));
    }

    @Given("^I have one QuestionnaireResponse resource loaded$")
    public void i_have_one_QuestionnaireResponse_resource_loaded() throws Throwable {
        assertNotNull(questionnaireResponseRepository.read(ctx,new IdType(1)));
    }

    @Given("^I have one RiskAssessment resource loaded$")
    public void i_have_one_RiskAssessment_resource_loaded() throws Throwable {
        assertNotNull(riskAssessmentRepository.read(ctx,new IdType(1)));
    }

    @Given("^I have one CarePlan resource loaded$")
    public void i_have_one_CarePlan_resource_loaded() throws Throwable {
        assertNotNull(carePlanRepository.read(ctx,new IdType(1)));
    }



    @When("^I search Procedure on Patient ID = (\\d+)$")
    public void i_search_Procedure_on_Patient_ID(int patient) throws Throwable {
        procedureList = procedureRepository.search(ctx, new ReferenceParam("Patient/"+patient),null,null, null,null);
    }

    @When("^I search List on Patient ID = (\\d+)$")
    public void i_search_List_on_Patient_ID(int patient) throws Throwable {
        listList = listRepository.searchListResource(ctx, null,null, new ReferenceParam("Patient/"+patient));
    }

    @When("^I search CarePlan on Patient ID = (\\d+)$")
    public void i_search_CarePlan_on_Patient_ID(int patient) throws Throwable {
        carePlanList = carePlanRepository.search(ctx, new ReferenceParam("Patient/"+patient), null,null,null, null,null);
    }

    @When("^I search RiskAssessment on Patient ID = (\\d+)$")
    public void i_search_RiskAssessment_on_Patient_ID(int patient) throws Throwable {
        riskAssessmentList = riskAssessmentRepository.search(ctx,  new ReferenceParam("Patient/"+patient),null,null);
    }

    @When("^I search QuestionnaireResponse on Patient ID = (\\d+)$")
    public void i_search_QuestionnaireResponse_on_Patient_ID(int patient) throws Throwable {
        questionnaireResponseList = questionnaireResponseRepository.searchQuestionnaireResponse(ctx, null,null,null, new ReferenceParam("Patient/"+patient));
    }

    @Then("^I should get a Bundle of QuestionnaireResponse (\\d+) resource$")
    public void i_should_get_a_Bundle_of_QuestionnaireResponse_resource(int count) throws Throwable {
        assertEquals(count,questionnaireResponseList.size());
    }

    @Then("^I should get a Bundle of CarePlan (\\d+) resource$")
    public void i_should_get_a_Bundle_of_CarePlan_resource(int count) throws Throwable {
        assertEquals(count,carePlanList.size());
    }

    @Then("^I should get a Bundle of List (\\d+) resource$")
    public void i_should_get_a_Bundle_of_List_resource(int count) throws Throwable {
        assertEquals(count,listList.size());
    }

    @Then("^I should get a Bundle of RiskAssessment (\\d+) resource$")
    public void i_should_get_a_Bundle_of_RiskAssessment_resource(int count) throws Throwable {
        assertEquals(count,riskAssessmentList.size());
    }

    @Then("^I should get a Bundle of Procedure (\\d+) resource$")
    public void i_should_get_a_Bundle_of_Procedure_resource(int count) throws Throwable {
        assertEquals(count,procedureList.size());
    }

    @When("^I update this Procedure$")
    public void i_update_this_Procedure() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("json/ProcedureExampleTwo.json");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        Procedure procedure = ctx.newJsonParser().parseResource(Procedure.class, reader);
        try {
            procedure = procedureRepository.create(ctx,procedure,null,"Procedure?identifier=" + procedure.getIdentifier().get(0).getSystem() + "%7C" +procedure.getIdentifier().get(0).getValue());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    @When("^I get MedicationStatement ID = (\\d+)$")
    public void i_get_MedicationStatement_ID(String id) throws Throwable {
        resource = statementRepository.read(ctx,new IdType().setValue(id));
    }

    @Then("^I should get a MedicationRequest resource$")
    public void i_should_get_a_MedicationRequest_resource() throws Throwable {
        Assert.assertThat(resource,instanceOf(MedicationRequest.class));
    }

    @Then("^I should get a MedicationStatement resource$")
    public void i_should_get_a_MedicationStatement_resource() throws Throwable {
        Assert.assertThat(resource,instanceOf(MedicationStatement.class));
    }

    @When("^I Conditional add a Patient$")
    public void i_Conditional_add_a_Patient() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("json/Patient.json");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        patient = ctx.newJsonParser().parseResource(Patient.class, reader);
        try {
            patient = patientRepository.update(ctx,patient,null,"Patient?identifier=https://fhir.leedsth.nhs.uk/Id/PPMIdentifier%7C1101");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    @Then("^I search Patient on Patient PPMID = (\\d+)$")
    public void i_search_Patient_on_Patient_PPMID(String ident) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        patientList = patientRepository.search(ctx,null,null,null,null,null,null,new TokenParam().setValue(ident),null,null,null,null,null);
    }

    @Then("^I should get a Bundle of Patient (\\d+) resource$")
    public void i_should_get_a_Bundle_of_Patient_resource(int count) throws Throwable {
        assertEquals(count,patientList.size());
    }

    @Given("^I have one DocumentReference resource loaded$")
    public void i_have_one_DocumentReference_resource_loaded() throws Throwable {
        resource = documentReferenceRepository.read(ctx,new IdType().setValue("1"));
    }

    @When("^I get DocumentReference ID = (\\d+)$")
    public void i_get_DocumentReference_ID(String id) throws Throwable {
        resource = documentReferenceRepository.read(ctx,new IdType().setValue(id));
    }

    @Then("^I should get a DocumentReference resource$")
    public void i_should_get_a_DocumentReference_resource() throws Throwable {
        Assert.assertThat(resource,instanceOf(DocumentReference.class));
    }


    private void validateResource(Resource resource) {
        ValidationResult result = validator.validateWithResult(resource);

        // Show the issues
        // Colour values https://github.com/yonchu/shell-color-pallet/blob/master/color16
        for (SingleValidationMessage next : result.getMessages()) {
            switch (next.getSeverity())
            {
                case ERROR:
                    fail("FHIR Validation ERROR - "+ next.getMessage());
                    break;
                case WARNING:
                    //fail("FHIR Validation WARNING - "+ next.getMessage());
                    System.out.println(  (char)27 + "[34mWARNING" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                case INFORMATION:
                    System.out.println( (char)27 + "[34mINFORMATION" + (char)27 + "[0m" + " - " +  next.getLocationString() + " - " + next.getMessage());
                    break;
                default:
                    System.out.println(" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
            }
        }
    }
    @BeforeClass
    public static void beforeClass(){
        initialized = false;
    }

    @Before
    public void before() throws Exception {
		/*
		 * This runs under maven, and I'm not sure how else to figure out the target directory from code..
		 */

		if (!initialized) {

            cs = codeSystemDao.findBySystem(CareConnectSystem.SNOMEDCT);

            ConceptEntity concept = null;

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("162864005");
            conceptDao.save(concept);

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("228272008");
            conceptDao.save(concept);

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("229819007");
            conceptDao.save(concept);

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("284350006");
            conceptDao.save(concept);


            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("301331008");
            conceptDao.save(concept);

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("320141001");
            conceptDao.save(concept);

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("365605003");
            conceptDao.save(concept);

/*
            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("385669000");
            conceptDao.save(concept);
           */
              concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("394899003");
            conceptDao.save(concept);

/*
            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("409073007");
            conceptDao.save(concept);
*/
            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("722071008");
            conceptDao.save(concept);

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("73430006");
            conceptDao.save(concept);

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("86290005");
            conceptDao.save(concept);

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("1871000175100");
            conceptDao.save(concept);


            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("308081000000105");
            conceptDao.save(concept);

            concept = new ConceptEntity();
            concept.setCodeSystem(cs);
            concept.setCode("718347000");
            conceptDao.save(concept);


            InputStream inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/Vital-Body-Mass-Example.json");
            assertNotNull(inputStream);
            Reader reader = new InputStreamReader(inputStream);

            Observation observation = ctx.newJsonParser().parseResource(Observation.class, reader);
            try {
                observation = observationRepository.save(ctx,observation, null, null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }


            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/Social-History-SmokingStatus.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            observation = ctx.newJsonParser().parseResource(Observation.class, reader);
            try {
                observation = observationRepository.save(ctx,observation,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/observationExample.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            observation = ctx.newJsonParser().parseResource(Observation.class, reader);
            try {
                observation = observationRepository.save(ctx,observation,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/EncounterExample.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            Encounter encounter = ctx.newJsonParser().parseResource(Encounter.class, reader);
            try {
                encounter = encounterRepository.create(ctx,encounter,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/ConditionExample.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            Condition condition = ctx.newJsonParser().parseResource(Condition.class, reader);
            try {
                condition = conditionRepository.create(ctx,condition,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/AllergyIntoleranceExample.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            AllergyIntolerance allergy = ctx.newJsonParser().parseResource(AllergyIntolerance.class, reader);
            try {
                allergy = allergyIntoleranceRepository.create(ctx,allergy,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/ImmunisationExample.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            Immunization immunization = ctx.newJsonParser().parseResource(Immunization.class, reader);
            try {
                immunization = immunizationRepository.create(ctx,immunization,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/ProcedureExample.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            Procedure procedure = ctx.newJsonParser().parseResource(Procedure.class, reader);
            try {
                procedure = procedureRepository.create(ctx,procedure,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/MedicationRequestExample.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            MedicationRequest prescription = ctx.newJsonParser().parseResource(MedicationRequest.class, reader);
            try {
                prescription = prescribingRepository.create(ctx,prescription,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/DocumentReference.xml");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            DocumentReference documentReference = ctx.newXmlParser().parseResource(DocumentReference.class, reader);
            try {
                documentReference = documentReferenceRepository.create(ctx,documentReference,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/HealthcareService.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            HealthcareService healthcareService = ctx.newJsonParser().parseResource(HealthcareService.class, reader);
            try {
                healthcareService = serviceRepository.create(ctx,healthcareService,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("json/ReferralRequest.json");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            ReferralRequest referralRequest = ctx.newJsonParser().parseResource(ReferralRequest.class, reader);
            try {
                referralRequest = referralRequestRepository.create(ctx,referralRequest,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/MedicationStatement.xml");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            MedicationStatement statement = ctx.newXmlParser().parseResource(MedicationStatement.class, reader);
            try {
                statement = statementRepository.create(ctx,statement,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/List.xml");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            ListResource list = ctx.newXmlParser().parseResource(ListResource.class, reader);
            try {
                list = listRepository.create(ctx,list,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/RiskAssessment.xml");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            RiskAssessment risk = ctx.newXmlParser().parseResource(RiskAssessment.class, reader);
            try {
                risk = riskAssessmentRepository.create(ctx,risk,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/ClinicalImpression.xml");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            ClinicalImpression clinicalImpression = ctx.newXmlParser().parseResource(ClinicalImpression.class, reader);
            try {
                clinicalImpression = impressionRepository.create(ctx,clinicalImpression,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/Consent.xml");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            Consent consent = ctx.newXmlParser().parseResource(Consent.class, reader);
            try {
                consent = consentRepository.create(ctx,consent,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/EOLC-QuestionnaireResponse.xml");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            QuestionnaireResponse questionnaireResponse = ctx.newXmlParser().parseResource(QuestionnaireResponse.class, reader);
            try {
                questionnaireResponse = questionnaireResponseRepository.create(ctx,questionnaireResponse,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            initialized = true;

            inputStream =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/CarePlan.xml");
            assertNotNull(inputStream);
            reader = new InputStreamReader(inputStream);

            CarePlan carePlan = ctx.newXmlParser().parseResource(CarePlan.class, reader);
            try {
                carePlan = carePlanRepository.create(ctx,carePlan,null,null);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            initialized = true;
        }


        if (validator == null) {
            /* TODO STU3
            FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
            validator.registerValidatorModule(instanceValidator);

            IValidationSupport valSupport = new CareConnectValidation();
            ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), valSupport);
            instanceValidator.setValidationSupport(support);
            */
        }
        else {
           // ourLog.info("START - CALLED NOT Creating Server");
        }
    }

}

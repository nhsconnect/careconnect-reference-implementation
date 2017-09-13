package uk.nhs.careconnect.ri.jpatest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.hl7.fhir.instance.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.instance.model.*;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.nhs.careconnect.ri.dao.Patient.PatientRepository;
import uk.nhs.careconnect.ri.dao.ValueSet.ValueSetRepository;
import uk.org.hl7.fhir.core.dstu2.CareConnectSystem;
import uk.org.hl7.fhir.validation.dstu2.CareConnectValidation;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.fail;


public class JPAStepsDef {

    @Autowired
    PatientRepository patientDAO;

    @Autowired
    ValueSetRepository valueSetRepository;

    Patient patient;
    Organization organization;
    ValueSet valueSet;
    Resource resource;

    List<Patient> patientList = null;

    List<Organization> organizationList = null;
    

    private static final FhirContext ourCtx = FhirContext.forDstu2Hl7Org();

    private static FhirContext ctxFHIR = FhirContext.forDstu2Hl7Org();
    private static FhirValidator validator = ctxFHIR.newValidator();


    @Given("^I add a Patient with an Id of (\\d+)$")
    public void i_add_a_Patient_with_an_Id_of(Integer id) throws Throwable {
        patient = patientDAO.read(new IdType().setValue(id.toString()));
    }

    @Given("^I search for a Patient with a family name of (\\w+)$")
    public void i_search_for_a_Patient_with_a_family_name_of(String name) throws Throwable {
        patientList = patientDAO.searchPatient(null,new StringParam(name),null,null,null,null);
    }

    @Given("^I search for a Patient with a given name of (\\w+)$")
    public void i_search_for_a_Patient_with_a_given_name_of(String name) throws Throwable {
        patientList = patientDAO.searchPatient(null,null,null,new StringParam(name),null,null);
    }

    @Given("^I search for a Patient with a birthdate of '(\\d+)-(\\d+)-(\\d+)'$")
    public void i_search_for_a_Patient_with_a_birthdate_of(int arg1, int arg2, int arg3) throws Throwable {
       //  patientList = patientDAO.searchPatient(new DateRangeParam().setLowerBound(new DateParam());,null,null,null,null,null);
        throw new PendingException();
    }

    @Given("^I search for a Patient with a gender of (\\w+)$")
    public void i_search_for_a_Patient_with_a_gender_of(String gender) throws Throwable {
        patientList = patientDAO.searchPatient(null,null,new StringParam(gender),null,null,null);
    }

    @Given("^I search for a Patient with a NHSNumber of (\\d+)$")
    public void i_search_for_a_Patient_with_a_NHSNumber_of(String NHSNumber) throws Throwable {
        patientList = patientDAO.searchPatient(null,null,null,null,new TokenParam().setSystem(CareConnectSystem.NHSNumber).setValue(NHSNumber),null);
    }

    @Given("^I search for a Patient with a name of \"([^\"]*)\"$")
    public void i_search_for_a_Patient_with_a_name_of(String name) throws Throwable {
        patientList = patientDAO.searchPatient(null,null,null,null,null,new StringParam(name));
    }

    @Given("^I add a ValueSet with an Id of ([^\"]*)$")
    public void i_add_a_ValueSet_with_an_Id_of(String valueSetId) throws Throwable {
        resource = (Resource) valueSetRepository.read(new IdType().setValue("ValueSet/"+valueSetId));
    }

    @Then("^the result should be a FHIR ValueSet$")
    public void the_result_should_be_a_FHIR_ValueSet() throws Throwable {
        Assert.assertThat(resource,instanceOf(ValueSet.class));
    }



    @Then("^the results should be a list of CareConnect Organisations$")
    public void the_results_should_be_a_list_of_CareConnect_Organisations() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("^I search for Organisations by SDSCode (\\w+)$")
    public void i_have_search_for_these_Organisations_by_SDSCode(String arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }


    @Then("^the result should be a list with (\\d+) entry$")
    public void the_result_should_be_a_valid_FHIR_Bundle_with_entry(int count) throws Throwable {
        Assert.assertNotNull(patientList);
        Assert.assertEquals(count,patientList.size());

    }

    @Then("^they shall all be FHIR Patient resources$")
    public void they_shall_all_be_FHIR_Patient_resources() throws Throwable {

        for (Patient patient : patientList) {
            Assert.assertThat(patient,instanceOf(Patient.class));
        }
    }

    @Then("^the result should be a FHIR Patient$")
    public void the_result_should_be_a_valid_FHIR_Patient() throws Throwable {
        System.out.println("Patient found = "+patient.getId()+ " "+patient.getName().get(0).getFamily().get(0));
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
        for (Patient patient : patientList) {
            validateResource(patient);
        }
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


    @Before
    public static void beforeClass() throws Exception {
		/*
		 * This runs under maven, and I'm not sure how else to figure out the target directory from code..
		 */
        if (validator == null) {
            FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
            validator.registerValidatorModule(instanceValidator);

            IValidationSupport valSupport = new CareConnectValidation();
            ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), valSupport);
            instanceValidator.setValidationSupport(support);
        }
        else {
           // ourLog.info("START - CALLED NOT Creating Server");
        }
    }

}

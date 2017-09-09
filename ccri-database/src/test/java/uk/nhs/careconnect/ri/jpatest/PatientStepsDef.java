package uk.nhs.careconnect.ri.jpatest;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import uk.nhs.careconnect.ri.dao.Patient.PatientRepository;


public class PatientStepsDef {

    @Autowired
    PatientRepository patientDAO;

    Patient patient;

    @Given("^I add a Patient with NHS Number (\\d+)$")
    public void i_add_a_Patient_with_NHS_Number(int arg1) throws Throwable {
       patient = patientDAO.read(new IdType().setValue("1"));

        // Write code here that turns the phrase above into concrete actions
        //throw new PendingException();
    }

    @Then("^the result should be a valid FHIR Bundle$")
    public void the_result_should_be_a_valid_FHIR_Bundle() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
      //  patientDAO.read(new IdType().setValue("1"));
        //throw new PendingException();
    }

    @Then("^the results should be valid CareConnect Profiles$")
    public void the_results_should_be_valid_CareConnect_Profiles() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}

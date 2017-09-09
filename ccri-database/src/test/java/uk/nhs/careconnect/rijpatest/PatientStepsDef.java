package uk.nhs.careconnect.rijpatest;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class PatientStepsDef {
    @Given("^I add a Patient with NHS Number (\\d+)$")
    public void i_add_a_Patient_with_NHS_Number(int arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the result should be a valid FHIR Bundle$")
    public void the_result_should_be_a_valid_FHIR_Bundle() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the results should be valid CareConnect Profiles$")
    public void the_results_should_be_valid_CareConnect_Profiles() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}

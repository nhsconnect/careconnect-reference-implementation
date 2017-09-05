package uk.nhs.careconnect.rhubarb;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class practitionerSupport {

    @Given("^I have these Practitioners on the RI:$")
    public void i_have_these_Practitioners_on_the_RI(DataTable practitionerRow) throws Throwable {

        // Check database to ensure practitioner is present, if not add to the database

        throw new PendingException();
    }

    @When("^I search for <SDSCode>$")
    public void i_search_for_SDSCode() throws Throwable {
        // Access FHIR HAPI Server to retrieve search on SDS Code
        throw new PendingException();
    }

    @Then("^the results should be valid CareConnect Practitioner Profiles$")
    public void the_results_should_be_valid_CareConnect_Practitioner_Profiles() throws Throwable {

        // Validate the response using HAPI FHIR Validation extensions

        throw new PendingException();
    }

    @Then("^the search shall be logged in the Audit Trail$")
    public void the_search_shall_be_logged_in_the_Audit_Trail() throws Throwable {
        // Inspect the Elastic Db Audit Trail for entry
        throw new PendingException();
    }
}

package uk.nhs.careconnect.ri.jpatest;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import uk.nhs.careconnect.ri.dao.Practitioner.PractitionerRepository;


public class PractitionerStepsDef {

    @Autowired
    PractitionerRepository practitionerDao;

    Practitioner practitioner;
    @Given("^I have these Practitioners on the RI:$")
    public void i_have_these_Practitioners_on_the_RI(DataTable arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        // For automatic transformation, change DataTable to one of
        // List<YourType>, List<List<E>>, List<Map<K,V>> or Map<K,V>.
        // E,K,V must be a scalar (String, Integer, Date, enum etc)
        throw new PendingException();
    }

    @When("^I search for <SDSCode>$")
    public void i_search_for_SDSCode() throws Throwable {
        practitioner = practitionerDao.read(new IdType().setValue("1"));
    }

    @Then("^the search shall be logged in the Audit Trail$")
    public void the_search_shall_be_logged_in_the_Audit_Trail() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}

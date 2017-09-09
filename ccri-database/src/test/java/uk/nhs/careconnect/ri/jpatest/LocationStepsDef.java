package uk.nhs.careconnect.ri.jpatest;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import uk.nhs.careconnect.ri.dao.Location.LocationRepository;


public class LocationStepsDef {

    @Autowired
    LocationRepository locationDao;

    Location location;
    @Given("^I have these Locations on the RI:$")
    public void i_have_these_Locations_on_the_RI(DataTable arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        // For automatic transformation, change DataTable to one of
        // List<YourType>, List<List<E>>, List<Map<K,V>> or Map<K,V>.
        // E,K,V must be a scalar (String, Integer, Date, enum etc)
        throw new PendingException();
    }

    @When("^I search for Location <SDSCode>$")
    public void i_search_for_SDSCode() throws Throwable {
        location = locationDao.read(new IdType().setValue("1"));
    }

}

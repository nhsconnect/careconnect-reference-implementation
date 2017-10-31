package uk.nhs.careconnect.ri.integrationTest;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

public class conformanceFeatureSteps {

    private HttpTestClient client =new HttpTestClient();

    @Given("^FHIR STU(\\d+) Server$")
    public void fhir_STU_Server(int arg1) throws Throwable {

    }

    @When("^I retrieve the ConformanceStatement format=(\\w+)$")
    public void i_retrieve_the_ConformanceStatement(String format) throws Throwable {
        client.doGet("metadata?_format="+format);
    }

    @Then("^the response code should be (\\d+)$")
    public void the_response_code_should_be(int responseCode) throws Throwable {
        Assert.assertEquals(responseCode,client.getResponseCode());
    }

    @Then("^the Header:([A-z-]+)=([A-z//+]+)$")
    public void the_Header_ContentType_application_json_fhir(String header, String response) throws Throwable {
      //  System.out.println("Header = "+header+" Response Value = "+response);
        Assert.assertThat(client.getHeader(header), CoreMatchers.containsString(response));

    }

}

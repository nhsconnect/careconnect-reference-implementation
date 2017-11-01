package uk.nhs.careconnect.ri.integrationTest;

import ca.uhn.fhir.context.FhirContext;
import cucumber.api.DataTable;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

import java.util.List;

public class methodFeatureSteps {

    private static HttpTestClient client=null;

    @Before
    public static void beforeClass() throws Exception {

        if (client == null) {
            client = new HttpTestClient(FhirContext.forDstu3());
        }
    }



    @When("^I Delete ([A-z0-9//]+)$")
    public void Delete_Url(String url) throws Throwable {
      //  System.out.println("Url = "+url);
        client.doDelete(url);
    }

    @When("^I Patch ([A-z0-9//]+)$")
    public void Patch_Url(String url, DataTable xmlString) throws Throwable {
        List<List<String>> data = xmlString.raw();
     //   System.out.println("Url = "+data.get(0).get(0));
        client.doPatch(url,data.get(0).get(0));
    }



    @When("^I Post (\\w+)/(\\w+)$")
    public void Post_Url(String url1, String url2, DataTable xmlString) throws Throwable {
        List<List<String>> data = xmlString.raw();
     //   System.out.println("Url = "+data.get(0).get(0));
        client.doPost(url1+"/"+url2,data.get(0).get(0));
    }

    @When("^I Post ([A-z0-9/_=]+)\\?([A-z0-9/\\-_=]+)$")
    public void PostSearch_Url(String url1, String url2) throws Throwable {


        client.doPost(url1+"?"+url2, null);
    }

    @When("^I Put ([A-z0-9/]+)$")
    public void Put_Url(String url, DataTable xmlString) throws Throwable {
        List<List<String>> data = xmlString.raw();
     //   System.out.println("Url = "+data.get(0).get(0));
        client.doPatch(url,data.get(0).get(0));
    }

    @When("^I Get ([A-z0-9/?_\\-=]+)$")
    public void Get_Url(String url) throws Throwable {
     //   System.out.println("Url = "+url);
        client.doGet(url);
    }

    @When("^I Head ([A-z0-9/?\\-_=]+)$")
    public void Head_Url(String url) throws Throwable {
      //  System.out.println("Url = "+url);
        client.doHead(url);
    }

    @Then("^have (\\d+) (\\w+)'s returned$")
    public void have_Patient_s_returned(int count,String resource) throws Throwable {
        client.convertReplytoBundle();
        Assert.assertEquals(count,client.countResources());

        Assert.assertTrue(client.checkResourceType(resource));
    }



    @Then("^the method response code should be (\\d+)$")
    public void the_method_response_code_should_be(int responseCode) throws Throwable {
        Assert.assertEquals(responseCode,client.getResponseCode());
    }


}

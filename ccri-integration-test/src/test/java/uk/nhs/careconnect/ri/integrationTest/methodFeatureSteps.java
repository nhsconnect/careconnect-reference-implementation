package uk.nhs.careconnect.ri.integrationTest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import cucumber.api.DataTable;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.text.WordUtils;
import org.fusesource.jansi.Ansi;
import org.hamcrest.CoreMatchers;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Assert;
import uk.org.hl7.fhir.validation.stu3.CareConnectProfileValidationSupport;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.fusesource.jansi.Ansi.ansi;

public class methodFeatureSteps {


    public static final String LINESEP = System.getProperty("line.separator");

    private static HttpTestClient client=null;

    private static FhirContext ctx = null;

    private static FhirValidator val = null;

    private static FhirInstanceValidator instanceValidator = null;

    @Before
    public static void beforeClass() throws Exception {

        if (client == null) {

            ctx = FhirContext.forDstu3();
            client = new HttpTestClient(ctx);
            val = ctx.newValidator();
            instanceValidator = new FhirInstanceValidator();
            val.registerValidatorModule(instanceValidator);
            ValidationSupportChain validationSupport = new ValidationSupportChain(
                    new DefaultProfileValidationSupport()
                    , new CareConnectProfileValidationSupport()
            );

            instanceValidator.setValidationSupport(validationSupport);
        }
    }

    @When("^I Delete ([A-z0-9//]+)$")
    public void Delete_Url(String url) throws Throwable {

        client.doDelete(url);
    }

    @When("^I Patch ([A-z0-9//]+)$")
    public void Patch_Url(String url, DataTable xmlString) throws Throwable {
        List<List<String>> data = xmlString.raw();

        client.doPatch(url,data.get(0).get(0));
    }

    @When("^I Post (\\w+)/(\\w+)$")
    public void Post_Url(String url1, String url2, DataTable xmlString) throws Throwable {
        List<List<String>> data = xmlString.raw();

        client.doPost(url1+"/"+url2,data.get(0).get(0));
    }

    @When("^I Post ([A-z0-9/_=]+)\\?([A-z0-9/\\-_=]+)$")
    public void PostSearch_Url(String url1, String url2) throws Throwable {


        client.doPost(url1+"?"+url2, null);
    }

    @When("^I Put ([A-z0-9/]+)$")
    public void Put_Url(String url, DataTable xmlString) throws Throwable {
        List<List<String>> data = xmlString.raw();

        client.doPatch(url,data.get(0).get(0));
    }

    @When("^I Get ([A-zÀ-ÿ0-9ć/?_\\-=\\s@.:()|&]+)$")
    public void Get_Url(String url) throws Throwable {

        client.doGet(url);
    }

    @When("^I Head ([A-z0-9/?\\-_=]+)$")
    public void Head_Url(String url) throws Throwable {

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

    @Then("^Patient Id = (\\d+)$")
    public void patient_Id(String patientId) throws Throwable {
        Assert.assertEquals(patientId, client.getFirstPatientId());
    }


    @Then("^contains Ids$")
    public void contain_Ids(DataTable ids) throws Throwable {
       List<String> idArray= client.getPatientIds(); List<Map<String, String>> data = ids.asMaps(String.class, String.class);

        for (Map map : data) {
            Assert.assertThat(idArray.toString(), CoreMatchers.containsString(map.get("PatientId").toString()));
        }
    }

    @Then("^resource is valid$")
    public void resource_is_valid() throws Throwable {
       Assert.assertTrue(validateResource(client.bundle));
    }

    public boolean validateResource(Bundle resource) {
        Boolean passed = true;

        ValidationResult results = val.validateWithResult(resource);

        StringBuilder b = new StringBuilder("Validation results:" + ansi().boldOff());
        int count = 0;
        for (SingleValidationMessage next : results.getMessages()) {
            count++;
            b.append(LINESEP);
            String leftString = "Issue " + count + ": ";
            int leftWidth = leftString.length();
            b.append(ansi().fg(Ansi.Color.GREEN)).append(leftString);
            if (next.getSeverity() != null) {
                b.append(next.getSeverity()).append(ansi().fg(Ansi.Color.WHITE)).append(" - ");
            }
            if (isNotBlank(next.getLocationString())) {
                b.append(ansi().fg(Ansi.Color.WHITE)).append(next.getLocationString());
            }
            String[] message = WordUtils.wrap(next.getMessage(), 80 - leftWidth, "\n", true).split("\\n");
            for (String line : message) {
                b.append(LINESEP);
                b.append(ansi().fg(Ansi.Color.WHITE));
                b.append(leftPad("", leftWidth)).append(line);
            }
            System.out.println(message.toString());

            if (next.getSeverity().equals("ERROR")) passed = false;
        }
        return passed;
    }

}

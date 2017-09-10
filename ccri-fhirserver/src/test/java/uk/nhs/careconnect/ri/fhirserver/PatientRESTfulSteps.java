package uk.nhs.careconnect.ri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hl7.fhir.instance.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.hapi.validation.IValidationSupport;
import org.hl7.fhir.instance.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.Patient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.slf4j.Logger;
import uk.org.hl7.fhir.validation.dstu2.CareConnectValidation;

import java.io.File;

import static org.junit.Assert.fail;


public class PatientRESTfulSteps {

    private static IGenericClient client;
    private static final FhirContext ourCtx = FhirContext.forDstu2Hl7Org();
    private static final Logger ourLog = org.slf4j.LoggerFactory.getLogger(FHIRServerTest.class);

    private static int ourPort;

    private static Server ourServer;
    private static String ourServerBase;

    private static FhirContext ctxFHIR = FhirContext.forDstu2Hl7Org();
    private static FhirValidator validator = ctxFHIR.newValidator();

    Bundle bundle;


    @Given("^Patient Search by familyName kanfeld$")
    public void patient_Search_by_familyName_kanfeld() throws Throwable {
        ourLog.info("GIVEN - Patient Search by familyName");

        bundle = client.search().forResource(Patient.class)
                .where(new StringClientParam("family").matches().value("kanfeld"))
                .returnBundle(Bundle.class).execute();

        Assert.assertNotNull(bundle);
    }

    @Then("^the result should be a valid FHIR Bundle$")
    public void the_result_should_be_a_valid_FHIR_Bundle() throws Throwable {
        Assert.assertNotNull(bundle);
    }

    @Then("^the results should be valid CareConnect Profiles$")
    public void the_results_should_be_valid_CareConnect_Profiles() throws Throwable {



        ValidationResult result = validator.validateWithResult(bundle);

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



    @AfterClass
    public static void afterClass() throws Exception {
        ourLog.info("STOP - After Class CALLED");
        ourServer.stop();
    }

    @After
    public static void after() throws Exception {
        ourLog.info("STOP - After CALLED");
       // ourServer.stop();
    }


    @Before
    public static void beforeClass() throws Exception {
		/*
		 * This runs under maven, and I'm not sure how else to figure out the target directory from code..
		 */
         if (ourServer == null) {

            ourLog.info("START - CALLED Creating Server");

            String path = FHIRServerTest.class.getClassLoader().getResource(".keep_hapi-fhir-jpaserver-example").getPath();
            ourLog.info("Properties Path = " + path);

            path = new File(path).getParent();
            path = new File(path).getParent();
            path = new File(path).getParent();

            ourLog.info("Project base path is: {}", path);

            ourPort = RandomServerPortProvider.findFreePort();
            ourServer = new Server(ourPort);

            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setContextPath("/careconnect-ri");
            webAppContext.setDescriptor(path + "/src/main/webapp/WEB-INF/web.xml");
            webAppContext.setResourceBase(path + "/target/careconnect-ri");
            webAppContext.setParentLoaderPriority(true);

            ourServer.setHandler(webAppContext);
            ourServer.start();

            ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
            ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
            ourServerBase = "http://localhost:" + ourPort + "/careconnect-ri/DSTU2";
            client = ourCtx.newRestfulGenericClient(ourServerBase);
            client.registerInterceptor(new LoggingInterceptor(true));


             FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
             validator.registerValidatorModule(instanceValidator);

             IValidationSupport valSupport = new CareConnectValidation();
             ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), valSupport);
             instanceValidator.setValidationSupport(support);
        }
        else {
             ourLog.info("START - CALLED NOT Creating Server");
         }
    }
}

package uk.nhs.careconnect.ri.gateway.http;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.validation.FhirValidator;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hl7.fhir.dstu3.model.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.slf4j.Logger;

import java.io.File;


public class InterceptorRESTfulSteps {

    private static IGenericClient client;
    private static final FhirContext ourCtx = FhirContext.forDstu3();
    private static final Logger ourLog = org.slf4j.LoggerFactory.getLogger(FHIRServerTest.class);

    private static int ourPort;

    private static Server ourServer;
    private static String ourServerBase;

    private static FhirContext ctxFHIR = FhirContext.forDstu3();
    private static FhirValidator validator = ctxFHIR.newValidator();

    Bundle bundle;

    Patient patient;


    @Given("^Patient Search by familyName kanfeld$")
    public void patient_Search_by_familyName_kanfeld() throws Throwable {
    //    ourLog.info("GIVEN - Patient Search by familyName");

        bundle = client.search().forResource(Patient.class)
                .where(new StringClientParam("family").matches().value("kanfeld"))
                .returnBundle(Bundle.class).execute();


    }

    @Then("^the result should be a valid FHIR Bundle$")
    public void the_result_should_be_a_valid_FHIR_Bundle() throws Throwable {
        Assert.assertNotNull(bundle);
    }

    @Given("^I Patient GET Patient (\\d+)$")
    public void i_Patient_GET_Patient(Long Id) throws Throwable {
        patient = client.read().resource(Patient.class).withId(Id).execute();

    }

    @Then("^the result should be a valid FHIR Patient$")
    public void the_result_should_be_a_valid_FHIR_Patient() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        Assert.assertNotNull(patient);
    }


    @AfterClass
    public static void afterClass() throws Exception {
     //   ourLog.info("STOP - After Class CALLED");
        ourServer.stop();
    }

    @After
    public static void after() throws Exception {
     //   ourLog.info("STOP - After CALLED");
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
            webAppContext.setContextPath("/careconnect");
            webAppContext.setDescriptor(path + "/src/main/webapp/WEB-INF/web.xml");
            webAppContext.setResourceBase(path + "/target/careconnect");
            webAppContext.setParentLoaderPriority(true);

            ourServer.setHandler(webAppContext);
            ourServer.start();

            ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
            ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
            ourServerBase = "http://localhost:" + ourPort + "/careconnect/STU3";
            client = ourCtx.newRestfulGenericClient(ourServerBase);
            client.registerInterceptor(new LoggingInterceptor(true));


        }
        else {
             ourLog.info("START - CALLED NOT Creating Server");
         }
    }
}

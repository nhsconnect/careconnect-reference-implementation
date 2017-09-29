package uk.nhs.careconnect.ri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.validation.FhirValidator;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.Before;
import cucumber.api.java.After;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.hl7.fhir.instance.model.*;

import org.junit.Assert;
import org.junit.AfterClass;
import org.slf4j.Logger;

import java.io.File;


public class ResourceRESTfulSteps {

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
    //    ourLog.info("GIVEN - Patient Search by familyName");

        bundle = client.search().forResource(Patient.class)
                .where(new StringClientParam("family").matches().value("kanfeld"))
                .returnBundle(Bundle.class).execute();

        Assert.assertNotNull(bundle);
    }

    @Given("^Organisation Search by name moir$")
    public void organisation_Search_by_name_moir() throws Throwable {
      //  ourLog.info("GIVEN - Organization Search by name");

        bundle = client.search().forResource(Organization.class)
                .where(new StringClientParam("name").matches().value("moir"))
                .returnBundle(Bundle.class).execute();

        Assert.assertNotNull(bundle);
    }

    @Given("^Practitioner Search by name Bhatia$")
    public void practitioner_Search_by_name_Bhatia() throws Throwable {
        bundle = client.search().forResource(Practitioner.class)
                .where(new StringClientParam("name").matches().value("Bhatia"))
                .returnBundle(Bundle.class).execute();

        Assert.assertNotNull(bundle);
    }

    @Given("^Location Search by name Long$")
    public void location_Search_by_name_Long() throws Throwable {
        bundle = client.search().forResource(Location.class)
                .where(new StringClientParam("name").matches().value("Long Eaton"))
                .returnBundle(Bundle.class).execute();

        Assert.assertNotNull(bundle);
    }


    @Then("^the result should be a valid FHIR Bundle$")
    public void the_result_should_be_a_valid_FHIR_Bundle() throws Throwable {
        Assert.assertNotNull(bundle);
    }

    @Then("^the results should be valid CareConnect Profiles$")
    public void the_results_should_be_valid_CareConnect_Profiles() throws Throwable {


/* TODO STU3
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
        */
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
            webAppContext.setContextPath("/careconnect-ri");
            webAppContext.setDescriptor(path + "/src/main/webapp/WEB-INF/web.xml");
            webAppContext.setResourceBase(path + "/target/careconnect-ri");
            webAppContext.setParentLoaderPriority(true);

            ourServer.setHandler(webAppContext);
            ourServer.start();

            ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
            ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
            ourServerBase = "http://localhost:" + ourPort + "/careconnect-ri/STU3";
            client = ourCtx.newRestfulGenericClient(ourServerBase);
            client.registerInterceptor(new LoggingInterceptor(true));

/* TODO STU3
             FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
             validator.registerValidatorModule(instanceValidator);

             IValidationSupport valSupport = new CareConnectValidation();
             ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), valSupport);
             instanceValidator.setValidationSupport(support);

             */
        }
        else {
             ourLog.info("START - CALLED NOT Creating Server");
         }
    }
}

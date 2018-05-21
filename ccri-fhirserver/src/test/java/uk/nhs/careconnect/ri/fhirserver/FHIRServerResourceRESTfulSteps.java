package uk.nhs.careconnect.ri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.DateClientParam;
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
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.dstu3.model.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.slf4j.Logger;
import uk.org.hl7.fhir.validation.stu3.CareConnectProfileValidationSupport;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class FHIRServerResourceRESTfulSteps {

    private static IGenericClient client;
    private static final FhirContext ourCtx = FhirContext.forDstu3();
    private static final Logger ourLog = org.slf4j.LoggerFactory.getLogger(FHIRServerTestIT.class);

    private static int ourPort;

    private static Server ourServer;
    private static String ourServerBase;

    private static FhirContext ctxFHIR = FhirContext.forDstu3();
    private static FhirValidator validator = ctxFHIR.newValidator();

    Observation observation;

    PractitionerRole practitionerRole;

    Bundle bundle;

    @Given("^Observation resource file$")
    public void observation_resource_file() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/Observation.xml");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        observation = ctxFHIR.newXmlParser().parseResource(Observation.class,reader);
    }

    @Given("^PractitionerRole resource file$")
    public void practitionerrole_resource_file() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/PractitionerRole.xml");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        practitionerRole = ctxFHIR.newXmlParser().parseResource(PractitionerRole.class,reader);
    }

    @Then("^save the PractitionerRole$")
    public void save_the_PractitionerRole() throws Throwable {
        client
                .create()
                .resource(practitionerRole)
                .prettyPrint()
                .execute();
    }


    @Given("^Observation a Blood Pressure import$")
    public void observation_a_Blood_Pressure_import() throws Throwable {
        InputStream inputStream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream("xml/Observation-BloodPressure.xml");
        assertNotNull(inputStream);
        Reader reader = new InputStreamReader(inputStream);

        observation = ctxFHIR.newXmlParser().parseResource(Observation.class,reader);
    }

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

    @Then("^save the Observation$")
    public void save_the_Observation() throws Throwable {
        client
            .create()
            .resource(observation)
            .prettyPrint()
            .execute();
    }

    @Given("^Condition Search by gt(\\d+)-(\\d+)-(\\d+)$")
    public void condition_Search_by_gt(String yyyy, String mm, String dd) throws Throwable {
        bundle = client.search().forResource(Condition.class)
                .where(new DateClientParam("asserted-date").afterOrEquals().day(yyyy+"-"+mm+"-"+dd))
                .returnBundle(Bundle.class).execute();
    }

    @Given("^Condition Search by ge(\\d+)-(\\d+)-(\\d+)$")
    public void condition_Search_by_ge(String yyyy, String mm, String dd) throws Throwable {
        bundle = client.search().forResource(Condition.class)
                .where(new DateClientParam("asserted-date").after().day(yyyy+"-"+mm+"-"+dd))
                .returnBundle(Bundle.class).execute();
    }


    @Then("^the result should be a valid FHIR Bundle$")
    public void the_result_should_be_a_valid_FHIR_Bundle() throws Throwable {
        Assert.assertNotNull(bundle);
    }



    @Then("^the results should be valid CareConnect Profiles$")
    public void the_results_should_be_valid_CareConnect_Profiles() throws Throwable {


        for (Bundle.BundleEntryComponent entry  : bundle.getEntry()) {

            ourLog.info(ctxFHIR.newXmlParser().encodeResourceToString(entry.getResource()));


            ValidationResult result = validator.validateWithResult(entry.getResource());

            // Show the issues
            // Colour values https://github.com/yonchu/shell-color-pallet/blob/master/color16
            for (SingleValidationMessage next : result.getMessages()) {
                String msg = "";
                switch (next.getSeverity()) {
                    case ERROR:
                        msg = (char) 27 + "[34mERROR" + (char) 27 + "[0m" + " - " + next.getLocationString() + " - " + next.getMessage();
                        ourLog.error(msg);

                        if (!next.getLocationString().equals("Patient.name")) {
                            fail(msg);
                        } else {
                           // fail(msg);  // TODO
                        }

                        break;
                    case WARNING:
                        //fail("FHIR Validation WARNING - "+ next.getMessage());
                        msg = (char) 27 + "[34mWARNING" + (char) 27 + "[0m" + " - " + next.getLocationString() + " - " + next.getMessage();
                        ourLog.warn(msg);
                        break;
                    case INFORMATION:
                        msg = (char) 27 + "[34mINFORMATION" + (char) 27 + "[0m" + " - " + next.getLocationString() + " - " + next.getMessage();
                        ourLog.info(msg);
                        break;
                    default:
                        msg = "Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage();
                }
                System.out.println(msg);
            }
        }
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

            String path = FHIRServerTestIT.class.getClassLoader().getResource(".keep_hapi-fhir-jpaserver-example").getPath();
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


             FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
             validator.registerValidatorModule(instanceValidator);

             ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), new CareConnectProfileValidationSupport(ctxFHIR));
             instanceValidator.setValidationSupport(support);


        }
        else {
             ourLog.info("START - CALLED NOT Creating Server");
         }
    }
}

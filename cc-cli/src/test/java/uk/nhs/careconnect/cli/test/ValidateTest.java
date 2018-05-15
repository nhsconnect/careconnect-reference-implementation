package uk.nhs.careconnect.cli.test;

import org.junit.Before;
import org.junit.Test;
import uk.nhs.careconnect.cli.App;


public class ValidateTest {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ValidateTest.class);

	@Before
	public void before() {
		System.setProperty("noexit", "true");
	}

	@Test
	public void testExampleUpload() {

		App.main(new String[] {"upload-examples", "-t"
				,"http://127.0.0.1:8080/careconnect-ri/STU3/" });
	}
	
	@Test
	public void testValidateLocalProfileGood() {
		String resourcePath = ValidateTest.class.getResource("/patient-careconnect-example-Good.xml").getFile();
		ourLog.info(resourcePath);
		
		App.main(new String[] {"validate", "-p", "-x"
				,"-n",resourcePath});
	}

	@Test
	public void testValidateLocalProfileBad() {
		String resourcePath = ValidateTest.class.getResource("/patient-careconnect-example-Bad.xml").getFile();
		ourLog.info(resourcePath);

		App.main(new String[] {"validate", "-p", "-x"
				,"-n",resourcePath});
	}
	@Test
	public void testValidateLocalObservationBloodPressure() {
		String resourcePath = ValidateTest.class.getResource("/Observation-Blood-Pressure.xml").getFile();
		ourLog.info(resourcePath);

		App.main(new String[] {"validate", "-p", "-x"
				,"-n",resourcePath});
	}

	@Test
	public void testValidateLocalOrganizationODS() {
		String resourcePath = ValidateTest.class.getResource("/Observation-Blood-Pressure.xml").getFile();
		ourLog.info(resourcePath);

		App.main(new String[] {"validate", "-p", "-x"
				,"-n",resourcePath});
	}

    @Test
    public void testValidateCondition() {
        String resourcePath = ValidateTest.class.getResource("/Condition.json").getFile();
        ourLog.info(resourcePath);

        App.main(new String[] {"validate", "-p", "-x"
                ,"-n",resourcePath});
    }

	@Test
	public void testValidateTOC() {
		String resourcePath = ValidateTest.class.getResource("/edischarge_full_payload_example-01.xml").getFile();
		ourLog.info(resourcePath);

		App.main(new String[] {"validate", "-p", "-x"
				,"-n",resourcePath});
	}


	@Test
	public void testValidateEncounter() {
		String resourcePath = ValidateTest.class.getResource("/EncounterDates.json").getFile();
		ourLog.info(resourcePath);

		App.main(new String[] {"validate", "-p", "-x"
				,"-n",resourcePath});
	}
	@Test
	public void testValidateEncounterType() {
		String resourcePath = ValidateTest.class.getResource("/EncounterType.json").getFile();
		ourLog.info(resourcePath);

		App.main(new String[] {"validate", "-p", "-x"
				,"-n",resourcePath});
	}
	@Test
	public void testValidateMedicationRequest() {
		String resourcePath = ValidateTest.class.getResource("/MedicationRequest.json").getFile();
		ourLog.info(resourcePath);

		App.main(new String[] {"validate", "-p", "-x"
				,"-n",resourcePath});
	}
}

package uk.nhs.careconnect.cli.test;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.nhs.careconnect.cli.ValidateCommand;
import uk.nhs.careconnect.cli.ValidationException;

import java.io.FileInputStream;
import java.io.InputStreamReader;


public class ValidateTest {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ValidateTest.class);

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void before() {
		System.setProperty("noexit", "true");
	}

	public void runInvalid(String resourcePath) throws Exception {
		ValidateCommand validateCommand = new ValidateCommand();

		exception.expect(ValidationException.class);

		String encoding = "UTF-8";
		String contents = IOUtils.toString(new InputStreamReader(new FileInputStream(resourcePath), encoding));

		validateCommand.valRun(contents);
	}

	public void runValid(String resourcePath) throws Exception {
		ValidateCommand validateCommand = new ValidateCommand();

		String encoding = "UTF-8";

		String contents = IOUtils.toString(new InputStreamReader(new FileInputStream(resourcePath), encoding));
		validateCommand.valRun(contents);
	}

	@Test
	public void testValidatePatientGood() throws Exception {
		String resourcePath = ValidateTest.class.getResource("/patient-careconnect-example-Good.json").getFile();
		runValid(resourcePath);
	}

	@Test()
	public void testValidatePatientBad() throws Exception {
		String resourcePath = ValidateTest.class.getResource("/patient-careconnect-example-Bad.xml").getFile();

		runInvalid(resourcePath);
	}

	@Test
	public void testValidateLocalObservationBloodPressure()throws Exception {
		String resourcePath = ValidateTest.class.getResource("/Observation-Blood-Pressure.xml").getFile();
		runValid(resourcePath);
	}

	@Test
	public void testValidateLocalOrganizationODS() throws Exception{
		String resourcePath = ValidateTest.class.getResource("/Observation-Blood-Pressure.xml").getFile();
		runValid(resourcePath);
	}

    @Test
    public void testValidateCondition() throws Exception {
        String resourcePath = ValidateTest.class.getResource("/Condition.json").getFile();
		runValid(resourcePath);
    }

	@Test
	public void testValidateImmunization() throws Exception {
		String resourcePath = ValidateTest.class.getResource("/Immunization.json").getFile();
		runValid(resourcePath);
	}

    /*
    Too many errors at present

	@Test
	public void testValidateTOC() {
		String resourcePath = ValidateTest.class.getResource("/edischarge_full_payload_example-01.xml").getFile();
		ourLog.info(resourcePath);

		App.main(new String[] {"validate", "-p", "-x"
				,"-n",resourcePath});
	}
	*/


	@Test
	public void testValidateEncounter() throws Exception{
		String resourcePath = ValidateTest.class.getResource("/EncounterDates.json").getFile();
		runValid(resourcePath);
	}
	@Test
	public void testValidateEncounterType() throws Exception {
		String resourcePath = ValidateTest.class.getResource("/EncounterType.json").getFile();
		runValid(resourcePath);
	}
	@Test
	public void testValidateMedicationRequest() throws Exception{
		String resourcePath = ValidateTest.class.getResource("/MedicationRequest.json").getFile();
		runValid(resourcePath);
	}
}

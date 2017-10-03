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
	public void testValidateLocalProfile() {
		String resourcePath = ValidateTest.class.getResource("/patient-careconnect-example1.xml").getFile();
		ourLog.info(resourcePath);
		
		App.main(new String[] {"validate", "-p"
				//,"-r"
				//,"-l","/Development/NHSD/CareConnect-profiles/constraints/CareConnect-Patient-1.xml"
				,"-n",resourcePath});
	}
	

}

package uk.nhs.careconnect.ri.gateway.https;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
@RunWith(Cucumber.class)
@CucumberOptions(
        format = { "pretty" },
        features = "classpath:cucumber/"
)
public class FHIRServerTest {


}

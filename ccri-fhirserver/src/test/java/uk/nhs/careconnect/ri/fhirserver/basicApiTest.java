package uk.nhs.careconnect.ri.fhirserver;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        format = { "pretty", "html:target/cucumber" }
        , snippets= SnippetType.CAMELCASE
        , features = "classpath:cucumber/JPAPatientTest.feature"
)

public class basicApiTest {
}

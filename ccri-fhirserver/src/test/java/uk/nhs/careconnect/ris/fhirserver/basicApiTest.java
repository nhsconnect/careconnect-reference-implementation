package uk.nhs.careconnect.ris.fhirserver;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        format = { "pretty", "html:target/cucumber" }
        , snippets= SnippetType.CAMELCASE
        , features = "classpath:cucumber/basicApiTest.feature"
)

public class basicApiTest {
}

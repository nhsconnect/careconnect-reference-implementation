package uk.nhs.careconnect.ri.database.jpatest;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue={"uk.nhs.careconnect","cucumber.api.spring"},
        features = "classpath:cucumber/"
)
public class JPATestIT {



}

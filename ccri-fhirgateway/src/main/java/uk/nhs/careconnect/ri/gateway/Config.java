package uk.nhs.careconnect.ri.gateway;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by kevinmayfield on 21/07/2017.
 */



@Configuration
@EnableTransactionManagement()
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = "uk.nhs.careconnect.ri")
public class Config {

    @Bean
    public FhirContext getFhirContext() {
        return FhirContext.forDstu3();
    }

    @Value("${datasource.cleardown.cron:0 19 21 * * *}")
    private String cron;

}

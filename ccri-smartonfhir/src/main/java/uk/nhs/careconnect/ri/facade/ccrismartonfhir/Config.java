package uk.nhs.careconnect.ri.facade.ccrismartonfhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


/**
 * Created by kevinmayfield on 21/07/2017.
 */



@Configuration
/* @EnableTransactionManagement() */
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = "uk.nhs.careconnect.ri")
public class Config {

    @Value("${datasource.cleardown.cron:0 19 21 * * *}")
    private String cron;


    @Value("${fhir.oauth2.authorize:http://purple.testlab.nhs.uk:20080/authorize}")
    private String oauth2authorize;

    @Value("${fhir.oauth2.token:http://purple.testlab.nhs.uk:20080/token}")
    private String oauth2token;

    @Value("${fhir.oauth2.register:http://purple.testlab.nhs.uk:20080/register}")
    private String oauth2register;


    public String getOauth2authorize() {
        return this.oauth2authorize;
    }

    public String getOauth2token() {
        return this.oauth2token;
    }

    public String getOauth2register() {
        return this.oauth2register;
    }

    public String getCron() {
        return this.cron;
    }

    @Bean
    public FhirContext getFhirContext() {
        return FhirContext.forDstu3();
    }

    /**
     * This interceptor adds some pretty syntax highlighting in responses when a browser is detected
     */
    @Bean(autowire = Autowire.BY_TYPE)
    public IServerInterceptor responseHighlighterInterceptor() {
        ResponseHighlighterInterceptor retVal = new ResponseHighlighterInterceptor();
        return retVal;
    }
}

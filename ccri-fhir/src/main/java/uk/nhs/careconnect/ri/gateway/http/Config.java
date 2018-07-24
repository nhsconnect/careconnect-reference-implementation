package uk.nhs.careconnect.ri.gateway.http;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.beans.factory.annotation.Autowire;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;

/**
 * Created by kevinmayfield on 21/07/2017.
 */



@Configuration
@EnableTransactionManagement()
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = "uk.nhs.careconnect.ri")
public class Config {

    @Value("${datasource.cleardown.cron:0 19 21 * * *}")
    private String cron;

    @Value("${fhir.resource.serverBase}")
    private String serverBase;

    @Value("${fhir.resource.serverName}")
    private String serverName;

    @Value("${fhir.resource.serverVersion}")
    private String serverVersion;


    public String getServerBase() {
        return serverBase;
    }

    public String getServerName() {
        return this.serverName;
    }

    public String getServerVersion() {
        return this.serverVersion;
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

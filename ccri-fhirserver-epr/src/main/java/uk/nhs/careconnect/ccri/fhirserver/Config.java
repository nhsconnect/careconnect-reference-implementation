package uk.nhs.careconnect.ccri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.careconnect.ccri.fhirserver.provider.DatabaseBackedPagingProvider;


/**
 * Created by kevinmayfield on 21/07/2017.
 */



@Configuration
public class Config {

    /*  TODO REACTIVATE AFTER TESTING
    @Bean
    public FhirContext getServerFhirContext() {
        return FhirContext.forDstu3();
    }

    */

    @Bean(autowire = Autowire.BY_TYPE)
		public DatabaseBackedPagingProvider databaseBackedPagingProvider() {
        			DatabaseBackedPagingProvider retVal = new DatabaseBackedPagingProvider();
        			return retVal;
        }

    @Value("${datasource.serverBase}")
    private String serverBase;


}

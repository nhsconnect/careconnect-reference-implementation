package uk.nhs.careconnect.ri.interceptor;



import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContextNameStrategy;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//import io.fabric8.insight.log.log4j.Log4jLogQuery;


@Configuration
@ComponentScan
public class CamelConfig extends CamelConfiguration {


	@Override
	protected void setupCamelContext(CamelContext camelContext) throws Exception {

		camelContext.setNameStrategy(new DefaultCamelContextNameStrategy("Interceptor"));
	/*
		camelContext.addComponent("hl7fhir",new HAPIComponent());
*/
	}



}

package uk.nhs.careconnect.ri.fhirserver;



import org.apache.camel.CamelContext;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//import io.fabric8.insight.log.log4j.Log4jLogQuery;


@Configuration
@ComponentScan
public class CamelConfig extends CamelConfiguration {


	@Override
	protected void setupCamelContext(CamelContext camelContext) throws Exception {


		/*// setup the ActiveMQ component
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL("vm://localhost?broker.persistent=false&broker.useJmx=false");

		// and register it into the CamelContext
		JmsComponent answer = new JmsComponent();
		answer.setConnectionFactory(connectionFactory);
		camelContext.addComponent("jms", answer);
		*/

	}



}

package uk.nhs.careconnect.ri.interceptor;


import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@PropertySource("classpath:application.properties")
public class CamelRoute extends RouteBuilder {

	@Autowired
	protected Environment env;
	
	
    @Override
    public void configure() 
    {
     	

		
		from("direct:FHIRServer")
			.routeId("CCRI FHIR Server")
			.to("log:uk.nhs.careconnect?level=INFO&showHeaders=true&showHeaders=true")
			.to("http://localhost:8080/careconnect-ri/STU3?throwExceptionOnFailure=false&bridgeEndpoint=true")
			.convertBodyTo(InputStream.class);

    }
}

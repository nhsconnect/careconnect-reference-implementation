package uk.nhs.careconnect.ri.gateway;


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
/*
		from("hl7fhir:Patient")
				.routeId("Patient")
				.to("log:uk.nhs.careconnect?level=INFO&showHeaders=true&showHeaders=true")
				.to("http://localhost:8080/careconnect-ri/STU3?throwExceptionOnFailure=false&bridgeEndpoint=true")
				.convertBodyTo(InputStream.class);
*/
		from("direct:FHIRPatient")
			.routeId("DMZ Patient")
				.to("direct:HAPIServer");



		from("direct:HAPIServer")
            .routeId("INT HAPI Server")
				.to("log:uk.nhs.careconnect?level=INFO&showHeaders=true&showHeaders=true")
				.to("http://localhost:8080/careconnect-ri/STU3?throwExceptionOnFailure=false&bridgeEndpoint=true")
				.convertBodyTo(InputStream.class);

    }
}

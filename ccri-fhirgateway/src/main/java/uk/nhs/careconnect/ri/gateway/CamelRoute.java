package uk.nhs.careconnect.ri.gateway;


import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class CamelRoute extends RouteBuilder {

	@Autowired
	protected Environment env;

	@Value("${fhir.restserver.serverBase:}")
	private static String serverBase;
	
    @Override
    public void configure() 
    {

		from("direct:FHIRPatient")
			.routeId("DMZ Patient")
				.to("direct:HAPIServer");

		from("direct:HAPIServer")
            .routeId("INT HAPI Server")
				.to("log:uk.nhs.careconnect?level=INFO&showHeaders=true&showHeaders=true")
				// TODO line below needs to be turned into config
                .to(serverBase)
				.convertBodyTo(InputStream.class);

    }
}

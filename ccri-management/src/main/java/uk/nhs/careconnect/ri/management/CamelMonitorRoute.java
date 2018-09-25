package uk.nhs.careconnect.ri.management;


import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CamelMonitorRoute extends RouteBuilder {

	@Autowired
	protected Environment env;


	@Value("${jolokia.jmxendpoint.ccriserver}")
	private String jmxCCRIServer;

	@Value("${jolokia.jmxendpoint.ccridocument}")
	private String jmxCCRIDocument;

	@Value("${jolokia.jmxendpoint.ccriintegration}")
	private String jmxCCRIIntegration;

	@Value("${fhir.resource.serverBase}")
	private String serverBase;
	
    @Override
    public void configure() 
    {

		rest("/config")
				.get("/http").to("direct:hello");

		from("direct:hello")
				.transform().constant("{ \"fhirServer\" : \""+serverBase+"\" }");


		from("servlet:ccri-fhirserver?matchOnUriPrefix=true")
				.routeId("ccri-fhiserver-jokolia")
			.to(jmxCCRIServer);

		from("servlet:ccri-integration?matchOnUriPrefix=true")
				.routeId("ccri-integration-jokolia")
				.to(jmxCCRIIntegration);

		from("servlet:ccri-document?matchOnUriPrefix=true")
				.routeId("ccri-document-jokolia")
				.to(jmxCCRIDocument);


    }
}

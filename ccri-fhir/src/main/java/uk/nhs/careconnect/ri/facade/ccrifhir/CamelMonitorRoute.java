package uk.nhs.careconnect.ri.facade.ccrifhir;



import ca.uhn.fhir.context.FhirContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.lib.gateway.camel.interceptor.GatewayPostProcessor;
import uk.nhs.careconnect.ri.lib.gateway.camel.interceptor.GatewayPreProcessor;
import uk.nhs.careconnect.ri.lib.gateway.camel.processor.BinaryResource;
import uk.nhs.careconnect.ri.lib.gateway.camel.processor.BundleMessage;
import uk.nhs.careconnect.ri.lib.gateway.camel.processor.CompositionDocumentBundle;

import java.io.InputStream;

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
	
    @Override
    public void configure() 
    {

		from("servlet:ccri-fhirserver?matchOnUriPrefix=true")
			.to(jmxCCRIServer);

		from("servlet:ccri-integration?matchOnUriPrefix=true")
				.to(jmxCCRIIntegration);

		from("servlet:ccri-document?matchOnUriPrefix=true")
				.to(jmxCCRIDocument);


    }
}

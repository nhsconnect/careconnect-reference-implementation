package uk.nhs.careconnect.ri.management;


import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
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

	@Value("${ods.fhirbase}")
	private String odsServer;

	@Value("${nrls.fhirbase}")
	private String nrlsServer;

    @Override
    public void configure() 
    {
		Endpoint odsEndpoint = null;
    	try {
			odsEndpoint = setupSSLConext(getContext(),odsServer);
		} catch (Exception ex) {

		}

		Endpoint nrlsEndpoint = null;
		try {
			nrlsEndpoint = setupSSLConext(getContext(),nrlsServer);
		} catch (Exception ex) {

		}

		rest("/config")
				.get("/http").to("direct:hello");

		from("direct:hello")
				.transform().constant("{ \"fhirServer\" : \""+serverBase+"\" }");

/*
		from("servlet:nrls?matchOnUriPrefix=true")
				.routeId("nrls")
				// add in headers for NRLS
				.to(nrlsServer);

		// Just needs CORS support
		from("servlet:ods?matchOnUriPrefix=true")
				.routeId("ods")
				.to(odsEndpoint);
*/
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

	private Endpoint setupSSLConext(CamelContext camelContext, String serverBase) throws Exception {

		KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
		// Change this path to point to your truststore/keystore as jks files
		keyStoreParameters.setResource("/etc/ssl/demo.jks");
		keyStoreParameters.setPassword("password");

		KeyManagersParameters keyManagersParameters = new KeyManagersParameters();
		keyManagersParameters.setKeyStore(keyStoreParameters);
		keyManagersParameters.setKeyPassword("password");

		TrustManagersParameters trustManagersParameters = new TrustManagersParameters();
		trustManagersParameters.setKeyStore(keyStoreParameters);

		SSLContextParameters sslContextParameters = new SSLContextParameters();
		sslContextParameters.setKeyManagers(keyManagersParameters);
		sslContextParameters.setTrustManagers(trustManagersParameters);

		HttpComponent httpComponent = camelContext.getComponent("http4s", HttpComponent.class);
		httpComponent.setSslContextParameters(sslContextParameters);
		//This is important to make your cert skip CN/Hostname checks
		httpComponent.setX509HostnameVerifier(new AllowAllHostnameVerifier());

		return httpComponent.createEndpoint("http4s:"+serverBase+"?throwExceptionOnFailure=false&bridgeEndpoint=true");
	}
}

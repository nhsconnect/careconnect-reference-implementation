package uk.nhs.careconnect.ri.management;


import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.SSLContextParametersSecureProtocolSocketFactory;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.spi.RestConfiguration;
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


	private String gpcAuth="Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.ewogICJpc3MiOiAiaHR0cDovL2VjMi01NC0xOTQtMTA5LTE4NC5ldS13ZXN0LTEuY29tcHV0ZS5hbWF6b25hd3MuY29tLyMvc2VhcmNoIiwKICAic3ViIjogIjEiLAogICJhdWQiOiAiaHR0cHM6Ly9hdXRob3JpemUuZmhpci5uaHMubmV0L3Rva2VuIiwKICAiZXhwIjogMTUyMDc4MjIwMCwKICAiaWF0IjogMTUyMDc4MTkwMCwKICAicmVhc29uX2Zvcl9yZXF1ZXN0IjogImRpcmVjdGNhcmUiLAogICJyZXF1ZXN0ZWRfc2NvcGUiOiAib3JnYW5pemF0aW9uLyoucmVhZCIsCiAgInJlcXVlc3RpbmdfZGV2aWNlIjogewogICAgInJlc291cmNlVHlwZSI6ICJEZXZpY2UiLAogICAgImlkZW50aWZpZXIiOiBbCiAgICAgIHsKICAgICAgICAic3lzdGVtIjogIldlYiBJbnRlcmZhY2UiLAogICAgICAgICJ2YWx1ZSI6ICJHUCBDb25uZWN0IERlbW9uc3RyYXRvciIKICAgICAgfQogICAgXSwKICAgICJtb2RlbCI6ICJEZW1vbnN0cmF0b3IiLAogICAgInZlcnNpb24iOiAiMS4wIiwKICAgICJ1cmwiOiAiaHR0cDovL2VjMi01NC0xOTQtMTA5LTE4NC5ldS13ZXN0LTEuY29tcHV0ZS5hbWF6b25hd3MuY29tLyMvc2VhcmNoIgogIH0sCiAgInJlcXVlc3Rpbmdfb3JnYW5pemF0aW9uIjogewogICAgInJlc291cmNlVHlwZSI6ICJPcmdhbml6YXRpb24iLAogICAgImlkZW50aWZpZXIiOiBbCiAgICAgIHsKICAgICAgICAic3lzdGVtIjogImh0dHBzOi8vZmhpci5uaHMudWsvSWQvb2RzLW9yZ2FuaXphdGlvbi1jb2RlIiwKICAgICAgICAidmFsdWUiOiAiQTExMTExIgogICAgICB9CiAgICBdLAogICAgIm5hbWUiOiAiR1AgQ29ubmVjdCBEZW1vbnN0cmF0b3IiCiAgfSwKICAicmVxdWVzdGluZ19wcmFjdGl0aW9uZXIiOiB7CiAgICAicmVzb3VyY2VUeXBlIjogIlByYWN0aXRpb25lciIsCiAgICAiaWQiOiAiMSIsCiAgICAiaWRlbnRpZmllciI6IFsKICAgICAgewogICAgICAgICJzeXN0ZW0iOiAiaHR0cHM6Ly9maGlyLm5ocy51ay9zZHMtdXNlci1pZCIsCiAgICAgICAgInZhbHVlIjogIkcxMzU3OTEzNSIKICAgICAgfSwKICAgICAgewogICAgICAgICJzeXN0ZW0iOiAiaHR0cHM6Ly9maGlyLm5ocy51ay9JZC9zZHMtcm9sZS1wcm9maWxlLWlkIiwKICAgICAgICAidmFsdWUiOiAiMTExMTExMTExIgogICAgICB9LAogICAgICB7CiAgICAgICAgInN5c3RlbSI6ICJMb2NhbFVzZXJTeXN0ZW0iLAogICAgICAgICJ2YWx1ZSI6ICIxIgogICAgICB9CiAgICBdLAogICAgIm5hbWUiOiBbCiAgICAgIHsKICAgICAgICAiZmFtaWx5IjogIkRlbW9uc3RyYXRvciIsCiAgICAgICAgImdpdmVuIjogWwogICAgICAgICAgIkdQQ29ubmVjdCIKICAgICAgICBdLAogICAgICAgICJwcmVmaXgiOiBbCiAgICAgICAgICAiTXIiCiAgICAgICAgXQogICAgICB9CiAgICBdCiAgfQp9.";

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


	@Value("${gpc.fhirbase}")
	private String gpcServer;

    @Override
    public void configure() 
    {

		restConfiguration()
				.component("servlet")
				.contextPath("api")
				.dataFormatProperty("prettyPrint", "true")
				.enableCORS(true);

		try {
			setupSSLConext(getContext(), odsServer);
		} catch(Exception ex) {
			log.error(ex.getMessage());
		}


		rest("/config")
				.get("/http").to("direct:hello");

		from("direct:hello")
				.transform().constant("{ \"fhirServer\" : \""+serverBase+"\" }");

		rest("/fhir")
				.get("/ods/{path}").to("direct:ods")
				.get("/nrls/{path}").to("direct:nrls")
				.get("/gpc/{path}").to("direct:gpc");


		from("direct:nrls")
				.routeId("nrlsREST")
				// add in headers for NRLS
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						Message in = exchange.getIn();
						exchange.getIn().setHeader(Exchange.HTTP_PATH, in.getHeader("path"));
					}})
				.to(nrlsServer);

		from("direct:gpc")
				.routeId("gpcREST")
				// add in headers for GPC
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						Message in = exchange.getIn();
						exchange.getIn().setHeader(Exchange.HTTP_PATH, in.getHeader("path"));
						exchange.getIn().setHeader("Ssp-TraceID","1324");
						exchange.getIn().setHeader("Ssp-From","200000000359");
						exchange.getIn().setHeader("Ssp-To","200000000359");
						exchange.getIn().setHeader("Ssp-InteractionID","urn:nhs:names:services:gpconnect:fhir:rest:read:metadata-1");
						exchange.getIn().setHeader("Authorization",gpcAuth);
					}})
				.to(gpcServer);

		from("direct:ods")
				.routeId("odsREST")
				.log("ods called")
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						Message in = exchange.getIn();
						exchange.getIn().setHeader(Exchange.HTTP_PATH, in.getHeader("path"));
					}})
				.to(odsServer);


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

	private void setupSSLConext(CamelContext camelContext, String serverBase) throws Exception {

		KeyStoreParameters keyStoreParameters = new KeyStoreParameters();
		// Change this path to point to your truststore/keystore as jks files
		keyStoreParameters.setResource("keystore.jks");
		keyStoreParameters.setPassword("password");

		KeyManagersParameters keyManagersParameters = new KeyManagersParameters();
		keyManagersParameters.setKeyStore(keyStoreParameters);
		keyManagersParameters.setKeyPassword("password");

		TrustManagersParameters trustManagersParameters = new TrustManagersParameters();
		trustManagersParameters.setKeyStore(keyStoreParameters);

		SSLContextParameters sslContextParameters = new SSLContextParameters();
		sslContextParameters.setKeyManagers(keyManagersParameters);
		sslContextParameters.setTrustManagers(trustManagersParameters);

		HttpComponent httpComponent = camelContext.getComponent("https4", HttpComponent.class);
		httpComponent.setSslContextParameters(sslContextParameters);



	}
}

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


	private String gpcAuth="Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJpc3MiOiJodHRwczovL29yYW5nZS50ZXN0bGFiLm5ocy51ay9ncGNvbm5lY3QtZGVtb25zdHJhdG9yL3YxLyIsInN1YiI6IjEiLCJhdWQiOiJodHRwczovL2F1dGhvcml6ZS5maGlyLm5ocy5uZXQvdG9rZW4iLCJleHAiOjE1Mzk1MzA1OTcsImlhdCI6MTUzOTUzMDI5NywicmVhc29uX2Zvcl9yZXF1ZXN0IjoiZGlyZWN0Y2FyZSIsInJlcXVlc3RlZF9zY29wZSI6InBhdGllbnQvKi5yZWFkIiwicmVxdWVzdGluZ19kZXZpY2UiOnsicmVzb3VyY2VUeXBlIjoiRGV2aWNlIiwiaWQiOiIxIiwiaWRlbnRpZmllciI6W3sic3lzdGVtIjoiV2ViIEludGVyZmFjZSIsInZhbHVlIjoiR1AgQ29ubmVjdCBEZW1vbnN0cmF0b3IifV0sInR5cGUiOnsiY29kaW5nIjpbeyJzeXN0ZW0iOiJEZXZpY2VJZGVudGlmaWVyU3lzdGVtIiwiY29kZSI6IkRldmljZUlkZW50aWZpZXIifV19LCJtb2RlbCI6InYxIiwidmVyc2lvbiI6IjEuMSJ9LCJyZXF1ZXN0aW5nX29yZ2FuaXphdGlvbiI6eyJyZXNvdXJjZVR5cGUiOiJPcmdhbml6YXRpb24iLCJpZCI6IjEiLCJpZGVudGlmaWVyIjpbeyJzeXN0ZW0iOiJodHRwczovL2ZoaXIubmhzLnVrL0lkL29kcy1vcmdhbml6YXRpb24tY29kZSIsInZhbHVlIjoiW09EU0NvZGVdIn1dfSwicmVxdWVzdGluZ19wcmFjdGl0aW9uZXIiOnsicmVzb3VyY2VUeXBlIjoiUHJhY3RpdGlvbmVyIiwiaWQiOiIxIiwiaWRlbnRpZmllciI6W3sic3lzdGVtIjoiaHR0cHM6Ly9maGlyLm5ocy51ay9JZC9zZHMtdXNlci1pZCIsInZhbHVlIjoiRzEzNTc5MTM1In0seyJzeXN0ZW0iOiJsb2NhbFN5c3RlbSIsInZhbHVlIjoiMSJ9XSwibmFtZSI6W3siZmFtaWx5IjoiRGVtb25zdHJhdG9yIiwiZ2l2ZW4iOlsiR1BDb25uZWN0Il0sInByZWZpeCI6WyJNciJdfV19fQ.";

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
		log.info("Starting Camel Route MAIN FHIR Server = " + serverBase);


		rest("/config")
				.get("/http").to("direct:hello");

		from("direct:hello")
				.routeId("helloTest")
				.transform().constant("{ \"fhirServer\" : \""+serverBase+"\" }");

		rest("/fhir")
				.get("/ods/{path}").to("direct:ods")
				.get("/nrls/{path}").to("direct:nrls")
				.get("/gpc/{path}").to("direct:gpc")
				.get("/gpc/{path}/{id}").to("direct:gpc")
				.post("/gpc/{path}/{id}").to("direct:gpc");


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
				.log("gpc called")
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						Message in = exchange.getIn();
						exchange.getIn().setHeader(Exchange.HTTP_PATH, in.getHeader("path"));
						if (in.getHeader("id") != null) {
							exchange.getIn().setHeader(Exchange.HTTP_PATH, in.getHeader("path")+"/"+in.getHeader("id"));
						}
						exchange.getIn().setHeader("Ssp-TraceID",exchange.getIn().getMessageId());
						exchange.getIn().setHeader("Ssp-From","200000000359");
						exchange.getIn().setHeader("Ssp-To","200000000359");
						//log.info("dude!");
						if (in.getHeader("Accept") == null || in.getHeader("Accept").toString().isEmpty() || in.getHeader("Accept").toString().equals("*/*"))  {

							 if (in.getHeader(Exchange.CONTENT_TYPE) != null || !in.getHeader(Exchange.CONTENT_TYPE).toString().isEmpty()) {
								 in.setHeader("Accept", in.getHeader(Exchange.CONTENT_TYPE));
							 } else {

								 in.setHeader("Accept", "application/fhir+json");
							 }
						} else if (in.getHeader("Accept").toString().contains("xml")) {
							in.setHeader("Accept", "application/fhir+xml");
						} else if (in.getHeader("Accept").toString().contains("json")) {
							in.setHeader("Accept", "application/fhir+json");
						}

						if (in.getHeader(Exchange.CONTENT_TYPE) == null || in.getHeader(Exchange.CONTENT_TYPE).toString() != null) {
							in.setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
						} else if (in.getHeader(Exchange.CONTENT_TYPE).toString().contains("xml")) {
							in.setHeader(Exchange.CONTENT_TYPE, "application/fhir+xml");
						} else if (in.getHeader(Exchange.CONTENT_TYPE).toString().contains("json")) {
							in.setHeader(Exchange.CONTENT_TYPE, "application/fhir+json");
						}
						if (in.getHeader(Exchange.HTTP_QUERY) != null && (in.getHeader(Exchange.HTTP_QUERY)).toString() !=null) {
							in.setHeader(Exchange.HTTP_QUERY, in.getHeader(Exchange.HTTP_QUERY).toString().replace("&_format=xml",""));
							in.setHeader(Exchange.HTTP_QUERY, in.getHeader(Exchange.HTTP_QUERY).toString().replace("_format=xml",""));
						}
						//exchange.getIn().setHeader(Exchange.HTTP_QUERY, in.getHeader("path").toString().replace("|",""));
						exchange.getIn().setHeader("Authorization",gpcAuth);
						String type="search";
						if (in.getHeader("id") != null) type="read";

						switch (in.getHeader("path").toString()) {
							case "metadata":
								exchange.getIn().setHeader("Ssp-InteractionID", "urn:nhs:names:services:gpconnect:fhir:rest:read:metadata-1");
								break;
							case "Patient":
								if (exchange.getIn().getHeader(Exchange.HTTP_METHOD).toString().equals("GET")) {
									exchange.getIn().setHeader("Ssp-InteractionID", "urn:nhs:names:services:gpconnect:fhir:rest:" + type + ":patient-1");
								} else {
									exchange.getIn().setHeader("Ssp-InteractionID","urn:nhs:names:services:gpconnect:fhir:operation:gpc.getstructuredrecord-1");
								}
								break;
							case "Appointment":
								exchange.getIn().setHeader("Ssp-InteractionID", "urn:nhs:names:services:gpconnect:fhir:rest:"+type+":patient_appointments-1");
								break;
							case "Slot":
								exchange.getIn().setHeader("Ssp-InteractionID", "urn:nhs:names:services:gpconnect:fhir:rest:"+type+":slot-1");
								break;
							case "Practitioner":
								exchange.getIn().setHeader("Ssp-InteractionID", "urn:nhs:names:services:gpconnect:fhir:rest:"+type+":practitioner-1");
								break;
							case "Organization":
								exchange.getIn().setHeader("Ssp-InteractionID", "urn:nhs:names:services:gpconnect:fhir:rest:"+type+":organization-1");
								break;
						}
					}})
				.to("log:uk.nhs.careconnect.facade.gpc?level=INFO&showHeaders=true&showExchangeId=true")
				.to(gpcServer)
				.to("log:uk.nhs.careconnect.facade.gpc?level=INFO&showHeaders=true&showExchangeId=true");

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

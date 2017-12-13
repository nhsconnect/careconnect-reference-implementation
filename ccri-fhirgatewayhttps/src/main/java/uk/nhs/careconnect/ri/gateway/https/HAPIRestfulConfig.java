package uk.nhs.careconnect.ri.gateway.https;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.util.VersionUtil;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import uk.nhs.careconnect.ri.gateway.https.oauth2.OAuth2Interceptor;
import uk.nhs.careconnect.ri.lib.ServerInterceptor;
import uk.nhs.careconnect.ri.gatewaylib.provider.*;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.TimeZone;

public class HAPIRestfulConfig extends RestfulServer {

	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HAPIRestfulConfig.class);

	private WebApplicationContext myAppCtx;

	@Value("${fhir.resource.serverBase}")
	private String serverBase;

    @Value("${fhir.resource.serverName}")
    private String serverName;

    @Value("${fhir.resource.serverVersion}")
    private String serverVersion;

	@Value("${fhir.oauth2.authorize:https://auth.hspconsortium.org/authorize}")
	private String oauth2authorize = "http://purple.testlab.nhs.uk:20080/authorize";

	@Value("${fhir.oauth2.token:https://auth.hspconsortium.org/token}")
	private String oauth2token = "http://purple.testlab.nhs.uk:20080/token";

	@Value("${fhir.oauth2.register:https://auth.hspconsortium.org/register}")
	private String oauth2register = "http://purple.testlab.nhs.uk:20080/register";

    @Override
	public void addHeadersToResponse(HttpServletResponse theHttpResponse) {
		theHttpResponse.addHeader("X-Powered-By", "HAPI FHIR " + VersionUtil.getVersion() + " RESTful Server (NHS Care Connect STU3)");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() throws ServletException {
		super.initialize();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));


		FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU3;
		setFhirContext(new FhirContext(fhirVersion));

		// Get the spring context from the web container (it's declared in web.xml)
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();

        if (serverBase != null && !serverBase.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBase));
        }


		setResourceProviders(Arrays.asList(
				 myAppCtx.getBean(PatientResourceProvider.class)
               	,myAppCtx.getBean(OrganisationResourceProvider.class)
                ,myAppCtx.getBean(PractitionerResourceProvider.class)
                ,myAppCtx.getBean(LocationResourceProvider.class)
               	,myAppCtx.getBean(PractitionerRoleResourceProvider.class)
      			,myAppCtx.getBean(ObservationResourceProvider.class) // Sprint 4 addition KGM
				,myAppCtx.getBean(EncounterResourceProvider.class) // R3 addition KGM
				,myAppCtx.getBean(ConditionResourceProvider.class) // R3 addition KGM
				,myAppCtx.getBean(ProcedureResourceProvider.class) // R3 addition KGM
				,myAppCtx.getBean(AllergyIntoleranceResourceProvider.class) // R3 addition KGM
				,myAppCtx.getBean(MedicationRequestResourceProvider.class) // R3 addition KGM
				,myAppCtx.getBean(MedicationStatementResourceProvider.class) // R3 addition KGM
				,myAppCtx.getBean(ImmunizationResourceProvider.class) // R3 addition KGM
				,myAppCtx.getBean(EpisodeOfCareResourceProvider.class) // TO DO Remove me for live KGM
		));

        // Replace built in conformance provider (CapabilityStatement)
        setServerConformanceProvider(new CareConnectConformanceProvider(oauth2authorize
				,oauth2token
				,oauth2register));

        setServerName(serverName);
        setServerVersion(serverVersion);


		// This is the format for each line. A number of substitution variables may
		// be used here. See the JavaDoc for LoggingInterceptor for information on
		// what is available.

		ServerInterceptor gatewayInterceptor = new ServerInterceptor(ourLog);
		registerInterceptor(new OAuth2Interceptor());  // Add OAuth2 Security Filter
		registerInterceptor(gatewayInterceptor);

		//gatewayInterceptor.setLoggerName("ccri.FHIRGateway");
		//gatewayInterceptor.setLogger(ourLog);

		setDefaultPrettyPrint(true);
		setDefaultResponseEncoding(EncodingEnum.JSON);

		FhirContext ctx = getFhirContext();
		// Remove as believe due to issues on docker ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
	}

	/**
	 * This interceptor adds some pretty syntax highlighting in responses when a browser is detected
	 */
	@Bean(autowire = Autowire.BY_TYPE)
	public IServerInterceptor responseHighlighterInterceptor() {
		ResponseHighlighterInterceptor retVal = new ResponseHighlighterInterceptor();
		return retVal;
	}



}

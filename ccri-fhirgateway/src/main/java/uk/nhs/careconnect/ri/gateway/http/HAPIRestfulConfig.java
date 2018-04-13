package uk.nhs.careconnect.ri.gateway.http;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.util.VersionUtil;
import org.hl7.fhir.dstu3.model.Composition;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import uk.nhs.careconnect.ri.gatewaylib.provider.*;
import uk.nhs.careconnect.ri.lib.ServerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.TimeZone;


public class HAPIRestfulConfig extends RestfulServer {

	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HAPIRestfulConfig.class);

	private WebApplicationContext myAppCtx;

	@Value("${fhir.resource.serverBase}")
	private String serverBase;

    @Value("${fhir.resource.serverName}")
    private String serverName;

    @Value("${fhir.resource.serverVersion}")
    private String serverVersion;


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
			//	,myAppCtx.getBean(CompositionResourceProvider.class) // Composition exploration
                ,myAppCtx.getBean(DocumentReferenceResourceProvider.class) // Unstructured
				,myAppCtx.getBean(BinaryResourceProvider.class) // Unstructured
				,myAppCtx.getBean(MedicationResourceProvider.class)
				,myAppCtx.getBean(BundleResourceProvider.class) // Supports uploading resources
				// ,myAppCtx.getBean(EpisodeOfCareResourceProvider.class) // TO DO Remove me for live KGM
		));

        // Replace built in conformance provider (CapabilityStatement)
        setServerConformanceProvider(new CareConnectConformanceProvider( ));

        setServerName(serverName);
        setServerVersion(serverVersion);


		// This is the format for each line. A number of substitution variables may
		// be used here. See the JavaDoc for LoggingInterceptor for information on
		// what is available.

		ServerInterceptor gatewayInterceptor = new ServerInterceptor(log);
		registerInterceptor(gatewayInterceptor);
		//gatewayInterceptor.setLoggerName("ccri.FHIRGateway");
		//gatewayInterceptor.setLogger(ourLog);

		// This paging provider is not robust KGM 24/11/2017

		// Mocking of a database Paging Provider is in server

		FifoMemoryPagingProvider pp = new FifoMemoryPagingProvider(10);
		pp.setDefaultPageSize(10);
		pp.setMaximumPageSize(100);
		setPagingProvider(pp);

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

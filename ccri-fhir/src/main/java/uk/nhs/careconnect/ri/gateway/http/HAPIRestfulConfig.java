package uk.nhs.careconnect.ri.gateway.http;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.util.VersionUtil;
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
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(uk.nhs.careconnect.ri.gateway.http.HAPIRestfulConfig.class);

	private WebApplicationContext myAppCtx;

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
		Config cfg = myAppCtx.getBean(uk.nhs.careconnect.ri.gateway.http.Config.class);
  
		String serverBase = cfg.getServerBase();
		String serverName = cfg.getServerName();
		String serverVersion = cfg.getServerVersion();

		log.info("serverBase: " + serverBase);
		log.info("serverName: " + serverName);
		log.info("serverVersion: " + serverVersion);

        if (serverBase != null && !serverBase.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBase));
        }


		setResourceProviders(Arrays.asList(

				// Care Connect API providers START

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
				,myAppCtx.getBean(DocumentReferenceResourceProvider.class) // Unstructured
				,myAppCtx.getBean(BinaryResourceProvider.class) // Unstructured
				,myAppCtx.getBean(MedicationResourceProvider.class)

				// Care Connect API providers END

				// Support for NHS Digital National Projects  START

				// NOT FOR LIVE - Use ENVIRONEMNT VARIABLE

				,myAppCtx.getBean(QuestionnaireResourceProvider.class)
				,myAppCtx.getBean(QuestionnaireResponseResourceProvider.class)
				,myAppCtx.getBean(ListResourceProvider.class)
				,myAppCtx.getBean(RelatedPersonResourceProvider.class)
				,myAppCtx.getBean(CarePlanResourceProvider.class)
				,myAppCtx.getBean(HealthcareServiceResourceProvider.class)
				,myAppCtx.getBean(ReferralRequestResourceProvider.class)
				,myAppCtx.getBean(CareTeamResourceProvider.class)
				,myAppCtx.getBean(MedicationDispenseResourceProvider.class)
				,myAppCtx.getBean(GoalResourceProvider.class)
				,myAppCtx.getBean(RiskAssessmentResourceProvider.class)
				,myAppCtx.getBean(ClinicalImpressionResourceProvider.class)
				,myAppCtx.getBean(ConsentResourceProvider.class)
				,myAppCtx.getBean(BundleResourceProvider.class) // Supports uploading resources
				,myAppCtx.getBean(EpisodeOfCareResourceProvider.class) // TO DO Remove me for live KGM

				// Support for NHS Digital Natinal Projects  END

				// NOT FOR LIVE


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
}

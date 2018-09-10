package uk.nhs.careconnect.ri.facade.ccrifhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.util.VersionUtil;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import uk.nhs.careconnect.ri.gatewaylib.provider.*;
import uk.nhs.careconnect.ri.lib.ServerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.TimeZone;


public class CcriTieServerHAPIConfig extends RestfulServer {

	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CcriTieServerHAPIConfig.class);

	private ApplicationContext applicationContext;

	CcriTieServerHAPIConfig(ApplicationContext context) {
		this.applicationContext = context;
	}

	@Value("http://127.0.0.1/STU3")
	private String serverBase;

    @Value("${fhir.resource.serverName}")
    private String serverName;

    @Value("${fhir.resource.serverVersion}")
    private String serverVersion;


    @Override
	public void addHeadersToResponse(HttpServletResponse theHttpResponse) {
		theHttpResponse.addHeader("X-Powered-By", "HAPI FHIR " + VersionUtil.getVersion() + " RESTful Server (INTEROPen Care Connect STU3)");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() throws ServletException {
		super.initialize();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));


		FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU3;
		setFhirContext(new FhirContext(fhirVersion));

	     if (serverBase != null && !serverBase.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBase));
        }

        if (applicationContext == null ) log.info("Context is null");

		setResourceProviders(Arrays.asList(
				applicationContext.getBean(PatientResourceProvider.class)
				,applicationContext.getBean(OrganisationResourceProvider.class)
				,applicationContext.getBean(PractitionerResourceProvider.class)
				,applicationContext.getBean(LocationResourceProvider.class)
				,applicationContext.getBean(PractitionerRoleResourceProvider.class)
				,applicationContext.getBean(ObservationResourceProvider.class) // Sprint 4 addition KGM
				,applicationContext.getBean(EncounterResourceProvider.class) // R3 addition KGM
				,applicationContext.getBean(ConditionResourceProvider.class) // R3 addition KGM
				,applicationContext.getBean(ProcedureResourceProvider.class) // R3 addition KGM
				,applicationContext.getBean(AllergyIntoleranceResourceProvider.class) // R3 addition KGM
				,applicationContext.getBean(MedicationRequestResourceProvider.class) // R3 addition KGM
				,applicationContext.getBean(MedicationStatementResourceProvider.class) // R3 addition KGM
				,applicationContext.getBean(ImmunizationResourceProvider.class) // R3 addition KGM
				,applicationContext.getBean(DocumentReferenceResourceProvider.class) // Unstructured
				,applicationContext.getBean(BinaryResourceProvider.class) // Unstructured
				,applicationContext.getBean(MedicationResourceProvider.class)

				,applicationContext.getBean(HealthcareServiceResourceProvider.class)
		));

		// Replace built in conformance provider (CapabilityStatement)
		setServerConformanceProvider(new CareConnectConformanceProvider(applicationContext));

        setServerName(serverName);
        setServerVersion(serverVersion);

		ServerInterceptor gatewayInterceptor = new ServerInterceptor(log);
		registerInterceptor(gatewayInterceptor);

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

package uk.nhs.careconnect.ccri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import uk.nhs.careconnect.ccri.fhirserver.provider.*;
import uk.nhs.careconnect.ri.lib.server.ServerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.Arrays;
import java.util.TimeZone;

@WebServlet(urlPatterns = { "/ccri-fhirserver/*" }, displayName = "FHIR Server")
public class CCRIFHIRServerHAPIConfig extends RestfulServer {

    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CCRIFHIRServerHAPIConfig.class);

  

    private ApplicationContext applicationContext;

    CCRIFHIRServerHAPIConfig(ApplicationContext context) {
        this.applicationContext = context;
    }

    private FhirContext ctx;

    @Value("${datasource.serverBase}")
    private String serverBase;


    @SuppressWarnings("unchecked")
    @Override
    protected void initialize() throws ServletException {
        super.initialize();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Get the spring context from the web container (it's declared in web.xml)
        FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU3;
        setFhirContext(new FhirContext(fhirVersion));

        if (serverBase != null && !serverBase.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBase));
        }

        if (applicationContext == null ) log.info("Context is null");

        AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(this);

        log.info("REST Servlet initialised with config: " + toString());

		/* 
         * We want to support FHIR DSTU2 format. This means that the server
		 * will use the DSTU2 bundle format and other DSTU2 encoding changes.
		 *
		 * If you want to use DSTU1 instead, change the following line, and change the 2 occurrences of dstu2 in web.xml to dstu1
		 */

        if (serverBase != null && !serverBase.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBase));
        }
        

		/*
		 * The BaseJavaConfigDstu2.java class is a spring configuration
		 * file which is automatically generated as a part of hapi-fhir-jpaserver-base and
		 * contains bean definitions for a resource provider for each resource type
		 */
        setResourceProviders(Arrays.asList(
                applicationContext.getBean(PatientProvider.class),
                applicationContext.getBean(OrganizationProvider.class),
                applicationContext.getBean(PractitionerProvider.class),
                applicationContext.getBean(LocationProvider.class),
                applicationContext.getBean(ValueSetProvider.class),
                applicationContext.getBean(StructureDefinitionProvider.class),
                applicationContext.getBean(CodeSystemProvider.class),
                applicationContext.getBean(ObservationProvider.class),
                applicationContext.getBean(PractitionerRoleProvider.class)
                ,applicationContext.getBean(EncounterProvider.class)
                ,applicationContext.getBean(EpisodeOfCareProvider.class)
                ,applicationContext.getBean(AllergyIntoleranceProvider.class)
                ,applicationContext.getBean(ConditionProvider.class)
                ,applicationContext.getBean(ProcedureProvider.class)
                ,applicationContext.getBean(ImmunizationProvider.class)
                ,applicationContext.getBean(MedicationRequestProvider.class)
                ,applicationContext.getBean(MedicationStatementProvider.class)
                // Basic implementation of reporting resources
                ,applicationContext.getBean(CompositionProvider.class)
                ,applicationContext.getBean(DocumentReferenceProvider.class)
                ,applicationContext.getBean(DiagnosticReportProvider.class)
                ,applicationContext.getBean(CarePlanProvider.class)
                ,applicationContext.getBean(MedicationProvider.class)
                ,applicationContext.getBean(ReferralRequestProvider.class)
                ,applicationContext.getBean(HealthcareServiceProvider.class)
                , applicationContext.getBean(EndpointProvider.class)
                , applicationContext.getBean(QuestionnaireProvider.class)
                , applicationContext.getBean(QuestionnaireResponseProvider.class)
                , applicationContext.getBean(ListProvider.class)
                , applicationContext.getBean(RelatedPersonProvider.class)
                , applicationContext.getBean(CareTeamProvider.class)
                , applicationContext.getBean(GoalProvider.class)
                , applicationContext.getBean(RiskAssessmentProvider.class)
                , applicationContext.getBean(MedicationDispenseProvider.class)
                , applicationContext.getBean(ClinicalImpressionProvider.class)
                , applicationContext.getBean(ConsentProvider.class)

                , applicationContext.getBean(ScheduleProvider.class)
                , applicationContext.getBean(SlotProvider.class)
                , applicationContext.getBean(AppointmentProvider.class)

        ));

        // Replace built in conformance provider (CapabilityStatement)
        setServerConformanceProvider(new CareConnectServerConformanceProvider());

        ServerInterceptor loggingInterceptor = new ServerInterceptor(log);
        registerInterceptor(loggingInterceptor);

        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("x-fhir-starter");
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Content-Type");

        config.addAllowedOrigin("*");

        config.addExposedHeader("Location");
        config.addExposedHeader("Content-Location");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Create the interceptor and register it
        CorsInterceptor interceptor = new CorsInterceptor(config);
        registerInterceptor(interceptor);

       // KGM 24/11/2017
        // Mocked for exploration only setPagingProvider(applicationContext.getBean(DatabaseBackedPagingProvider.class));

        // not fully tested registerProvider(applicationContext.getBean(TerminologyUploaderProvider.class));
        setDefaultPrettyPrint(true);
        setDefaultResponseEncoding(EncodingEnum.JSON);

        ctx = getFhirContext();
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


    @Override
    public String toString() {
        return "HAPIRestfulConfig{" +
                "serverBase='" + serverBase + '\'' +
                '}';
    }
}

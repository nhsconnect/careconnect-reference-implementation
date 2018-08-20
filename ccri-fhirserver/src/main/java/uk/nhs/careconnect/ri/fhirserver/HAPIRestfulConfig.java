package uk.nhs.careconnect.ri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Medication;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import uk.nhs.careconnect.ri.fhirserver.provider.*;
import uk.nhs.careconnect.ri.lib.ServerInterceptor;

import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.TimeZone;

public class HAPIRestfulConfig extends RestfulServer {

    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HAPIRestfulConfig.class);

    private WebApplicationContext myAppCtx;

    private FhirContext ctx;

    @Value("${datasource.serverBase}")
    private String serverBase;


    @SuppressWarnings("unchecked")
    @Override
    protected void initialize() throws ServletException {
        super.initialize();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Get the spring context from the web container (it's declared in web.xml)
        myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();

        AutowireCapableBeanFactory autowireCapableBeanFactory = myAppCtx.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(this);

        ourLog.info("REST Servlet initialised with config: " + toString());

		/* 
         * We want to support FHIR DSTU2 format. This means that the server
		 * will use the DSTU2 bundle format and other DSTU2 encoding changes.
		 *
		 * If you want to use DSTU1 instead, change the following line, and change the 2 occurrences of dstu2 in web.xml to dstu1
		 */

        if (serverBase != null && !serverBase.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBase));
        }

        FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU3;
        setFhirContext(new FhirContext(fhirVersion));

		/*
		 * The BaseJavaConfigDstu2.java class is a spring configuration
		 * file which is automatically generated as a part of hapi-fhir-jpaserver-base and
		 * contains bean definitions for a resource provider for each resource type
		 */
        setResourceProviders(Arrays.asList(
                myAppCtx.getBean(PatientProvider.class),
                myAppCtx.getBean(OrganizationProvider.class),
                myAppCtx.getBean(PractitionerProvider.class),
                myAppCtx.getBean(LocationProvider.class),
                myAppCtx.getBean(ValueSetProvider.class),
                myAppCtx.getBean(StructureDefinitionProvider.class),
                myAppCtx.getBean(CodeSystemProvider.class),
                myAppCtx.getBean(ObservationProvider.class),
                myAppCtx.getBean(PractitionerRoleProvider.class)
                ,myAppCtx.getBean(EncounterProvider.class)
                ,myAppCtx.getBean(EpisodeOfCareProvider.class)
                ,myAppCtx.getBean(AllergyIntoleranceProvider.class)
                ,myAppCtx.getBean(ConditionProvider.class)
                ,myAppCtx.getBean(ProcedureProvider.class)
                ,myAppCtx.getBean(ImmunizationProvider.class)
                ,myAppCtx.getBean(MedicationRequestProvider.class)
                ,myAppCtx.getBean(MedicationStatementProvider.class)
                // Basic implementation of reporting resources
                ,myAppCtx.getBean(CompositionProvider.class)
                ,myAppCtx.getBean(DocumentReferenceProvider.class)
                ,myAppCtx.getBean(DiagnosticReportProvider.class)
                ,myAppCtx.getBean(CarePlanProvider.class)
                ,myAppCtx.getBean(MedicationProvider.class)
                ,myAppCtx.getBean(ReferralRequestProvider.class)
                ,myAppCtx.getBean(HealthcareServiceProvider.class)
                , myAppCtx.getBean(EndpointProvider.class)
                , myAppCtx.getBean(QuestionnaireProvider.class)
                , myAppCtx.getBean(QuestionnaireResponseProvider.class)
                , myAppCtx.getBean(ListProvider.class)
                , myAppCtx.getBean(RelatedPersonProvider.class)
                , myAppCtx.getBean(CareTeamProvider.class)
                , myAppCtx.getBean(GoalProvider.class)
                , myAppCtx.getBean(RiskAssessmentProvider.class)
                , myAppCtx.getBean(MedicationDispenseProvider.class)
                , myAppCtx.getBean(ClinicalImpressionProvider.class)

        ));

        // Replace built in conformance provider (CapabilityStatement)
        setServerConformanceProvider(new CareConnectServerConformanceProvider());

        ServerInterceptor loggingInterceptor = new ServerInterceptor(ourLog);
        registerInterceptor(loggingInterceptor);

       // KGM 24/11/2017
        // Mocked for exploration only setPagingProvider(myAppCtx.getBean(DatabaseBackedPagingProvider.class));

        // not fully tested registerProvider(myAppCtx.getBean(TerminologyUploaderProvider.class));
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

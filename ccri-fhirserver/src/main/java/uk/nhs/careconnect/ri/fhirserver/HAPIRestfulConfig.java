package uk.nhs.careconnect.ri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import uk.nhs.careconnect.ri.common.ServerInterceptor;
import uk.nhs.careconnect.ri.fhirserver.provider.*;

import javax.servlet.ServletException;
import java.util.Arrays;

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

        ));

        ServerInterceptor loggingInterceptor = new ServerInterceptor(ourLog);
        registerInterceptor(loggingInterceptor);

        //loggingInterceptor.setLoggerName("ccri.FHIRServer");
        //loggingInterceptor.setLogger(ourLog);



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

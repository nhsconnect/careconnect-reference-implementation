package uk.nhs.careconnect.ccri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ca.uhn.fhir.validation.FhirValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import uk.nhs.careconnect.ccri.fhirserver.interceptor.CCRequestValidatingInterceptor;
import uk.nhs.careconnect.ccri.fhirserver.interceptor.ServerInterceptor;
import uk.nhs.careconnect.ccri.fhirserver.oauth2.OAuth2Interceptor;
import uk.nhs.careconnect.ccri.fhirserver.stu3.provider.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@WebServlet(urlPatterns = { "/ccri-fhir/*" }, displayName = "FHIR Server")
public class JpaRestfulServerSTU3 extends RestfulServer {

    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JpaRestfulServerSTU3.class);

  

    private ApplicationContext applicationContext;

    JpaRestfulServerSTU3(ApplicationContext context) {
        this.applicationContext = context;
    }

    private static FhirContext ctx;


    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String serverPath;

    // @Value("#{'${some.server.url}'.split(',')}")

    @Value("#{'${ccri.EPR_resources}'.split(',')}")
    private List<String> EPR_resources;

    @Value("#{'${ccri.EPRCareConnectAPI_resources}'.split(',')}")
    private List<String>  EPRCareConnectAPI_resources;

    @Value("#{'${ccri.MPI_resources}'.split(',')}")
    private List<String>  MPI_resources;

    @Value("#{'${ccri.DocumentRegistry_resources}'.split(',')}")
    private List<String>  DocumentRegistry_resources;

    @Value("#{'${ccri.HealthProviderDirectory_resources}'.split(',')}")
    private List<String>  HealthProviderDirectory_resources;

    @Value("#{'${ccri.GOV_resources}'.split(',')}")
    private List<String>  GOV_resources;

    @Value("#{'${ccri.TerminologyServices_resources}'.split(',')}")
    private List<String>  TerminologyServices_resources;

    @Value("#{'${ccri.DocumentRepository_resources}'.split(',')}")
    private List<String>  DocumentRepository_resources;

    @Value("#{'${ccri.DocumentRepository_CareConnectAPI_resources}'.split(',')}")
    private List<String>  DocumentRepository_CareConnectAPI_resources;

    @Value("#{'${ccri.Messaging_resources}'.split(',')}")
    private List<String>  Messaging_resources;

    @Value("#{'${ccri.AggregationService_resources}'.split(',')}")
    private List<String>  AggregationService_resources;

    @Value("#{'${ccri.NRLSAdaptor_resources}'.split(',')}")
    private List<String>  NRLSAdaptor_resources;

    @Value("#{'${ccri.GPConnectAdaptor_resources}'.split(',')}")
    private List<String>  GPConnectAdaptor_resources;



    @SuppressWarnings("unchecked")
    @Override
    protected void initialize() throws ServletException {
        super.initialize();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Get the spring context from the web container (it's declared in web.xml)
        FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU3;
        setFhirContext(new FhirContext(fhirVersion));
        String serverBase = HapiProperties.getServerAddress();
        if (serverBase != null && !serverBase.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBase));
        }

        if (applicationContext == null ) log.info("Context is null");

        AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(this);

        log.info("REST Servlet initialised with config: {}", toString());

		/* 
         * We want to support FHIR DSTU2 format. This means that the server
		 * will use the DSTU2 bundle format and other DSTU2 encoding changes.
		 *
		 * If you want to use DSTU1 instead, change the following line, and change the 2 occurrences of dstu2 in web.xml to dstu1
		 */



        /*
         * The BaseJavaConfigDstu2.java class is a spring configuration
         * file which is automatically generated as a part of hapi-fhir-jpaserver-base and
         * contains bean definitions for a resource provider for each resource type
         */

        // Initilising the permissions

        List<String> permissions = null;
        switch(HapiProperties.getServerRole())
        {
            case "EPR" :
                permissions = EPR_resources;
                break;
            case "EPRCareConnectAPI" :
                permissions =  EPRCareConnectAPI_resources;
                break;
            case "GOV" :
                permissions =  GOV_resources;
                break;
            case "MPI" :
                permissions = MPI_resources;
                break;
            case "DocumentRegistry" :
                permissions =  DocumentRegistry_resources;
                break;
            case "HealthProviderDirectory" :
                permissions = HealthProviderDirectory_resources;
                break;
            case "TerminologyServices" :
                permissions =  TerminologyServices_resources;
                break;
            case "DocumentRepository" :
                permissions = DocumentRepository_resources;
                break;
            case "DocumentRepository_CareConnectAPI" :
                permissions =  DocumentRepository_CareConnectAPI_resources;
                break;
            case "Messaging" :
                permissions = Messaging_resources;
                break;
            case "AggregationService" :
                permissions =  AggregationService_resources;
                break;
            case "NRLSAdaptor" :
                permissions = NRLSAdaptor_resources;
                break;
            case "GPConnectAdaptor" :
                permissions =  GPConnectAdaptor_resources;
                break;
            default:
        }


        Class<?> classType = null;
        log.info("Resource count {}", permissions.size());

        List<IResourceProvider> permissionlist = new ArrayList<>();
        for (String permission : permissions) {
            try {
                classType = Class.forName("uk.nhs.careconnect.ccri.fhirserver.stu3.provider." + permission + "Provider");
                log.info("class methods {}", classType.getMethods()[4].getName() );
            } catch (ClassNotFoundException  e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            permissionlist.add((IResourceProvider) applicationContext.getBean(classType));
        }



        setResourceProviders(permissionlist);


        // Replace built in conformance provider (CapabilityStatement)
        setServerConformanceProvider(new CareConnectServerConformanceProvider());

        setServerName(HapiProperties.getServerName());
        setServerVersion(HapiProperties.getSoftwareVersion());
        setImplementationDescription(HapiProperties.getSoftwareImplementationDesc());


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

        ServerInterceptor gatewayInterceptor = new ServerInterceptor(log);
        if (HapiProperties.getSecurityOauth()) {
            registerInterceptor(new OAuth2Interceptor());  // Add OAuth2 Security Filter
        }
        registerInterceptor(gatewayInterceptor);

        FifoMemoryPagingProvider pp = new FifoMemoryPagingProvider(10);
        pp.setDefaultPageSize(10);
        pp.setMaximumPageSize(100);
        setPagingProvider(pp);

        setDefaultPrettyPrint(true);
        setDefaultResponseEncoding(EncodingEnum.JSON);

        ctx = getFhirContext();


        if (HapiProperties.getValidationFlag()) {

            CCRequestValidatingInterceptor requestInterceptor = new CCRequestValidatingInterceptor(log, (FhirValidator) applicationContext.getBean("fhirValidatorSTU3"), ctx);

            registerInterceptor(requestInterceptor);
        }

        registerInterceptor( new ResponseHighlighterInterceptor());

        // Remove as believe due to issues on docker ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
    }



    @Override
    public String toString() {
        return "HAPIRestfulConfig{" +
                "serverBase='" + HapiProperties.getServerBase() + '\'' +
                '}';
    }
}

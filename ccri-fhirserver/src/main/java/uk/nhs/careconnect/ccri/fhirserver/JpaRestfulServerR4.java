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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import uk.nhs.careconnect.ccri.fhirserver.interceptor.ServerInterceptor;
import uk.nhs.careconnect.ccri.fhirserver.oauth2.OAuth2Interceptor;
import uk.nhs.careconnect.ccri.fhirserver.r4.provider.CareConnectServerConformanceR4Provider;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@WebServlet(urlPatterns = { "/ccri-fhir/*" }, displayName = "FHIR Server")
public class JpaRestfulServerR4 extends RestfulServer {

    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JpaRestfulServerR4.class);



    private static ApplicationContext applicationContext;

    JpaRestfulServerR4(ApplicationContext context) {
        this.applicationContext = context;
    }

    private FhirContext ctx;


    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String serverPath;

    // @Value("#{'${some.server.url}'.split(',')}")




    @SuppressWarnings("unchecked")
    @Override
    protected void initialize() throws ServletException {
        super.initialize();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Get the spring context from the web container (it's declared in web.xml)
        FhirVersionEnum fhirVersion = FhirVersionEnum.R4;
        setFhirContext(new FhirContext(fhirVersion));
        String serverBase = HapiProperties.getServerAddress();
        if (serverBase != null && !serverBase.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverBase));
        }

        if (applicationContext == null ) log.info("Context is null");

        AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(this);

        List<IResourceProvider> permissionlist = new ArrayList<>();
        Class<?> classType = null;
        try {
            classType = Class.forName("uk.nhs.careconnect.ccri.fhirserver.r4.provider.ObservationDefinitionProvider");
            log.info("class methods " + classType.getMethods()[4].getName() );
        } catch (ClassNotFoundException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        permissionlist.add((IResourceProvider) applicationContext.getBean(classType));
        setResourceProviders(permissionlist);

        // Replace built in conformance provider (CapabilityStatement)
        setServerConformanceProvider(new CareConnectServerConformanceR4Provider());

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

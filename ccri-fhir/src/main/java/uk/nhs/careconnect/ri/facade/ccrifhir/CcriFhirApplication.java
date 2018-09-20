package uk.nhs.careconnect.ri.facade.ccrifhir;

import ca.uhn.fhir.context.FhirContext;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContextNameStrategy;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uk.nhs.careconnect.ri.lib.server.CorsFilter;

@SpringBootApplication
@ComponentScan({"uk.nhs.careconnect.ri.facade.ccrifhir","uk.nhs.careconnect.ri"})
@PropertySource("classpath:application.properties")
public class CcriFhirApplication extends SpringBootServletInitializer {

    @Autowired
    ApplicationContext context;

    @Value("${ccri.software.version}")
    String softwareVersion;

    @Value("${ccri.software.name}")
    String softwareName;

    @Value("${ccri.server}")
    String server;

    @Value("${ccri.guide}")
    String guide;

    @Value("${ccri.server.base}")
    String serverBase;

    public static void main(String[] args) {
        System.setProperty("hawtio.authenticationEnabled", "false");
        System.setProperty("management.security.enabled","false");
        System.setProperty("server.port", "8183");
        System.setProperty("server.context-path", "/ccri-fhir");
        System.setProperty("management.contextPath","");


        // This works but hawtio doesn't respect it System.setProperty("server.context-path", "/ccri-fhir");
        // Suspect hawtio api works but not the app
        SpringApplication.run(CcriFhirApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean ServletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new CcriTieServerHAPIConfig(context), "/STU3/*");
        registration.setName("FhirServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }


    @Bean
    public FhirContext getFhirContext() {

        System.setProperty("ccri.server.base",this.serverBase);
        System.setProperty("ccri.software.name",this.softwareName);
        System.setProperty("ccri.software.version",this.softwareVersion);
        System.setProperty("ccri.guide",this.guide);
        System.setProperty("ccri.server",this.server);
        return FhirContext.forDstu3();
    }

    @Bean
    CorsConfigurationSource
    corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    @Bean
    public FilterRegistrationBean corsFilter() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter());
        bean.setOrder(0);
        return bean;
    }

    @Bean
    CamelContextConfiguration contextConfiguration() {
        return new CamelContextConfiguration() {

            @Override
            public void beforeApplicationStart(CamelContext camelContext) {

                camelContext.setNameStrategy(new DefaultCamelContextNameStrategy("CcriFHIR"));

            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {

            }
        };
    }


}

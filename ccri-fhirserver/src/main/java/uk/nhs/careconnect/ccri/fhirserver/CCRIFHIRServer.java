package uk.nhs.careconnect.ccri.fhirserver;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.nhs.careconnect.ccri.fhirserver.support.CorsFilter;

@SpringBootApplication(exclude = {ElasticsearchAutoConfiguration.class})
@EnableTransactionManagement()
@EnableSwagger2
@PropertySource("classpath:application.properties")
@ComponentScan({"uk.nhs.careconnect.ccri","uk.nhs.careconnect.ri.database","uk.nhs.careconnect.ri.stu3.dao","uk.nhs.careconnect.ri.r4.dao"})
public class CCRIFHIRServer {

    @Autowired
    ApplicationContext context;

    public static void main(String[] args) {

        System.setProperty("hawtio.authenticationEnabled", "false");
        System.setProperty("management.security.enabled","false");
        System.setProperty("management.contextPath","");

        SpringApplication.run(CCRIFHIRServer.class, args);
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new JpaRestfulServerSTU3(context), "/STU3/*");
        registration.setName("FhirServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationR4Bean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new JpaRestfulServerR4(context), "/R4/*");
        registration.setName("FhirServletR4");
        registration.setLoadOnStartup(2);
        return registration;
    }

    @Bean(name="stu3ctx")
    @Primary
    public FhirContext getStu3FhirContext() {

        return FhirContext.forDstu3();
    }

    @Bean(name="r4ctx")
    public FhirContext getR4FhirContext() {

        return FhirContext.forR4();
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
}

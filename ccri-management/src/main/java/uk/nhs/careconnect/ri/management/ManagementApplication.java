package uk.nhs.careconnect.ri.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class ManagementApplication {

    public static void main(String[] args) {

        System.setProperty("hawtio.authenticationEnabled", "false");
        System.setProperty("hawtio.role","MANAGER");
        System.setProperty("management.security.enabled","false");
        System.setProperty("server.port", "8187");
        System.setProperty("server.context-path", "/ccri");
        System.setProperty("management.contextPath","");
        System.setProperty("spring.mvc.static-path-pattern","/resources/**");

        SpringApplication.run(ManagementApplication.class, args);
    }
}

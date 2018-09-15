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
        System.setProperty("server.context-path", "");
        System.setProperty("management.contextPath","");

        SpringApplication.run(ManagementApplication.class, args);
    }
}

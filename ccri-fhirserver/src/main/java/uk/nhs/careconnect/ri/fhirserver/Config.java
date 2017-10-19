package uk.nhs.careconnect.ri.fhirserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;

/**
 * Created by kevinmayfield on 21/07/2017.
 */



@Configuration
@EnableTransactionManagement()
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = "uk.nhs.careconnect.ri")
public class Config {

    @Value("${datasource.cleardown.cron:0 19 21 * * *}")
    private String cron;

    @Value("${datasource.serverBase}")
    private String serverBase;

    @PostConstruct
    private void printConfig(){
        System.out.println("Started with Config:" +  this.toString());
    }

    @Override
    public String toString() {
        return "Config{" +
                "cron='" + cron + '\'' +
                ", serverBase='" + serverBase + '\'' +
                '}';
    }
}

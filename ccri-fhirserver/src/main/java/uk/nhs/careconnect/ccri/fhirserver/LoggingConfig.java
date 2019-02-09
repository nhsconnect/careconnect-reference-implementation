package uk.nhs.careconnect.ccri.fhirserver;

import io.hawt.log.log4j.Log4jLogQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;


@Configuration
public class LoggingConfig {
    // See config details from http://hawt.io/plugins/logs/


    @Bean(name = "LogQuery", destroyMethod = "stop", initMethod = "start")
    @Scope("singleton")
    @Lazy(false)
    public Log4jLogQuery log4jLogQuery() {
        Log4jLogQuery log4jLogQuery = new Log4jLogQuery();
        return log4jLogQuery;
    }


}

package uk.nhs.careconnect.ri.facade.ccrifhir;

import org.springframework.context.annotation.Configuration;


@Configuration
public class LoggingConfig {
    // See config details from http://hawt.io/plugins/logs/

    /* TODO Need to active this in order for hawtio to support logging.

    @Bean(name = "LogQuery", destroyMethod = "stop", initMethod = "start")
    @Scope("singleton")
    @Lazy(false)
    public Log4jLogQuery log4jLogQuery() {
        Log4jLogQuery log4jLogQuery = new Log4jLogQuery();
        return log4jLogQuery;
    }

*/

}
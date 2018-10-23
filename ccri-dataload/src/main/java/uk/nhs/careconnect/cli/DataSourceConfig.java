package uk.nhs.careconnect.cli;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

@Configuration
@EnableScheduling
@PropertySource("classpath:application.properties")

public class DataSourceConfig {

    @Value("${datasource.vendor:h2}")
    private String vendor;

    @Value("${datasource.host:mem}")
    private String host;

    @Value("${datasource.path:db1}")
    private String path;

    @Value("${datasource.username:}")
    private String username;

    @Value("${datasource.password:}")
    private String password;

    @Value("${datasource.showSql:false}")
    private boolean showSql;

    @Value("${datasource.showDdl:true}")
    private boolean showDdl;

    @Value("${datasource.dialect:org.hibernate.dialect.DerbyTenSevenDialect}")
    private String dialect;

    @Value("${datasource.driver:org.apache.derby.jdbc.EmbeddedDriver}")
    private String driverName;

    @Value("${flyway.locations}")
    private String flywayLocations;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataSourceConfig.class);



    @Bean()
    public DataSource dataSource() {
        final BasicDataSource dataSource = new BasicDataSource();
        System.out.println("In Data Source");
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl("jdbc:" + vendor + ":" + host + ":" + path);

        System.out.println(dataSource.getUrl());
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setValidationQuery("select 1 as dbcp_connection_test");



        return dataSource;
    }

    //@Bean(initMethod = "migrate")
    @Bean
    public Flyway flyway() {

        Flyway flyway = new Flyway();
      //  flyway.setBaselineOnMigrate(true);
        log.info("FLYWAY Locations = "+ flywayLocations);
        flyway.setLocations(flywayLocations);
        flyway.setDataSource(dataSource());
        flyway.migrate();
        return flyway;
    }

/*
    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }
*/


    private Properties jpaProperties() {
        Properties extraProperties = new Properties();
        extraProperties.put("hibernate.dialect", dialect);
        extraProperties.put("hibernate.format_sql", "true");
        extraProperties.put("hibernate.show_sql", showSql);
        extraProperties.put("hibernate.hbm2ddl.auto", "update");
        extraProperties.put("hibernate.jdbc.batch_size", "20");
        extraProperties.put("hibernate.jdbc.time_zone","UTC");
        extraProperties.put("hibernate.cache.use_query_cache", "false");
        extraProperties.put("hibernate.cache.use_second_level_cache", "false");
        extraProperties.put("hibernate.cache.use_structured_entries", "false");
        extraProperties.put("hibernate.cache.use_minimal_puts", "false");

        extraProperties.put("hibernate.c3p0.min_size","5");
        extraProperties.put("hibernate.c3p0.max_size","20");
        extraProperties.put("hibernate.c3p0.timeout","300");
        extraProperties.put("hibernate.c3p0.max_statements","50");
        extraProperties.put("hibernate.c3p0.idle_test_period","3000");
        //extraProperties.put("hibernate.connection.isolation", String.valueOf(Connection.TRANSACTION_SERIALIZABLE));

        // 2017-10-10 KGM added to resolve mysql wait_timeout issue
        extraProperties.put("current_session_context_class","thread");

        extraProperties.put("hibernate.connection.driver_class",driverName);
        extraProperties.put("hibernate.connection.url","jdbc:" + vendor + ":" + host + ":" + path);
        extraProperties.put("hibernate.connection.username",username);
        extraProperties.put("hibernate.connection.password",password);
        extraProperties.put("hibernate.connection.provider_class","org.hibernate.connection.C3P0ConnectionProvider");

        return extraProperties;
    }
/*
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        PlatformTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);

        return transactionManager;
    }
*/
/*
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
    }
*/
/*
    @Bean
    public RefreshData getRefreshData() {
        return new RefreshData();
    }
    */
}

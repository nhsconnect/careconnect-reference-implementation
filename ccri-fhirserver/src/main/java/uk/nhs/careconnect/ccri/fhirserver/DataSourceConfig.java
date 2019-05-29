package uk.nhs.careconnect.ccri.fhirserver;

import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableScheduling
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory",
                       transactionManagerRef = "transactionManager",
                       basePackages = "uk.nhs.careconnect.ri.database")
public class DataSourceConfig {



    @Value("${flyway.locations}")
    private String flywayLocations;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataSourceConfig.class);



    //@Bean()
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        final BasicDataSource dataSource = new BasicDataSource();
        System.out.println("In Data Source");
        dataSource.setDriverClassName(HapiProperties.getDataSourceDriver());

        dataSource.setUrl(HapiProperties.getDataSourceUrl());

        System.out.println(dataSource.getUrl());
        dataSource.setUsername(HapiProperties.getDataSourceUsername());
        dataSource.setPassword(HapiProperties.getDataSourcePassword());

        dataSource.setValidationQuery("select 1 as dbcp_connection_test");



        return dataSource;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway() {

        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        log.info("FLYWAY Locations = "+ flywayLocations);
        flyway.setLocations(flywayLocations);
        flyway.setDataSource(dataSource());
       // flyway.repair();
        return flyway;
    }


    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }



    @Bean
    public EntityManagerFactory entityManagerFactory() {
      //  final Database database = Database.valueOf(vendor.toUpperCase());

        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPersistenceUnitName("CCRI_PERSISTENCE_UNIT");
       // factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("uk.nhs.careconnect.ri.database.entity");
        factory.setDataSource(dataSource());
        factory.setPersistenceProvider(new HibernatePersistenceProvider());
        factory.setJpaProperties(jpaProperties());
        factory.afterPropertiesSet();


        return factory.getObject();
    }
    private Properties jpaProperties() {
        Properties extraProperties = new Properties();
        extraProperties.put("hibernate.dialect", HapiProperties.getHibernateDialect());
        extraProperties.put("hibernate.format_sql", "true");
        extraProperties.put("hibernate.show_sql", HapiProperties.getHibernateShowSql());
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

/*
        extraProperties.put("hibernate.connection.driver_class",driverName);
        if (path != null && !path.isEmpty()) {
            extraProperties.put("hibernate.connection.url","jdbc:" + vendor + ":" + host + ":" + path);
        } else {
            extraProperties.put("hibernate.connection.url","jdbc:" + vendor + ":" + host);
        }
        extraProperties.put("hibernate.connection.username",username);
        extraProperties.put("hibernate.connection.password",password);
*/
       // extraProperties.put("hibernate.connection.provider_class","org.hibernate.connection.C3P0ConnectionProvider");
       // revisit for JMX monitoring extraProperties.put("hibernate.generate_statistics","true");

        return extraProperties;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        PlatformTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);

        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
    }


}

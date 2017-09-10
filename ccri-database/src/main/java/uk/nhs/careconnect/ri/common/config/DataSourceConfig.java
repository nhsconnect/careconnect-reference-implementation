package uk.nhs.careconnect.ri.common.config;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import java.util.Properties;

@Configuration
@EnableScheduling
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory",
                       transactionManagerRef = "transactionManager",
                       basePackages = "uk.nhs.careconnect.ri")
public class DataSourceConfig {

    @Value("${datasource.vendor:derby}")
    private String vendor;

    @Value("${datasource.host:directory}")
    private String host;

    @Value("${datasource.path:target/jpaserver_derby_files;create=true}")
    private String path;

    @Value("${datasource.username:}")
    private String username;

    @Value("${datasource.password:}")
    private String password;

    @Value("${datasource.showSql:false}")
    private boolean showSql;

    @Value("${datasource.showDdl:false}")
    private boolean showDdl;

    @Value("${datasource.dialect:org.hibernate.dialect.DerbyTenSevenDialect}")
    private String dialect;

    @Value("${datasource.driver:org.apache.derby.jdbc.EmbeddedDriver}")
    private String driverName;

    //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataSourceConfig.class);


    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        final DataSource dataSource = new DataSource();
        System.out.println("In Data Source");
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl("jdbc:" + vendor + ":" + host + ":" + path);
    //    log.info(dataSource.getUrl());
        System.out.println(dataSource.getUrl());
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setValidationQuery("select 1 as dbcp_connection_test");
        //dataSource.setTestOnBorrow(true);


        return dataSource;
    }

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }


    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        final Database database = Database.valueOf(vendor.toUpperCase());

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(showSql);
        vendorAdapter.setGenerateDdl(showDdl);
        vendorAdapter.setDatabase(database);


        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("uk.nhs.careconnect.ri.entity");
        factory.setDataSource(dataSource);
        factory.setJpaProperties(jpaProperties());
        factory.afterPropertiesSet();

        return factory.getObject();
    }
    private Properties jpaProperties() {
        Properties extraProperties = new Properties();
        extraProperties.put("hibernate.dialect", dialect);
        extraProperties.put("hibernate.format_sql", "true");
        extraProperties.put("hibernate.show_sql", showSql);
        extraProperties.put("hibernate.hbm2ddl.auto", "update");
        extraProperties.put("hibernate.jdbc.batch_size", "20");
        extraProperties.put("hibernate.cache.use_query_cache", "false");
        extraProperties.put("hibernate.cache.use_second_level_cache", "false");
        extraProperties.put("hibernate.cache.use_structured_entries", "false");
        extraProperties.put("hibernate.cache.use_minimal_puts", "false");
        extraProperties.put("hibernate.search.default.directory_provider", "filesystem");
        extraProperties.put("hibernate.search.default.indexBase", "target/lucenefiles");
        extraProperties.put("hibernate.search.lucene_version", "LUCENE_CURRENT");
//		extraProperties.put("hibernate.search.default.worker.execution", "async");
        return extraProperties;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public RefreshData getRefreshData() {
        return new RefreshData();
    }
}

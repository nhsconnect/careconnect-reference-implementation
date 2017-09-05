package uk.nhs.careconnectapi.securityConfig;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

//import io.fabric8.insight.log.log4j.Log4jLogQuery;


@Configuration
@PropertySource("classpath:careconnectapi.properties")
public class ResourceConfig  {
	
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	// private static final Logger log = LoggerFactory.getLogger(ResourceConfig.class);

	  @Autowired
	  protected Environment env;
			  	  
	  
			/*
	  @Bean(name= "resourceDataSource")
	  public DataSource dataSource() {
		  SimpleDriverDataSource ds = new SimpleDriverDataSource();
		  try {
		     	 @SuppressWarnings("unchecked")
		         Class<? extends Driver> driverClass = (Class<? extends Driver>) Class.forName(env.getProperty("PAS.Driver"));
		         ds.setDriverClass(driverClass);	      
		    } catch (Exception e) {
		       log.error("Error loading driver class", e);
		    }
		    
			ds.setUrl(env.getProperty("PAS.url"));
			ds.setUsername(env.getProperty("PAS.username"));
			ds.setPassword(env.getProperty("PAS.password"));
				
			return ds;
		}
		*/


	  @Autowired
	  Log4jLogQuery log4jLogQuery;

}

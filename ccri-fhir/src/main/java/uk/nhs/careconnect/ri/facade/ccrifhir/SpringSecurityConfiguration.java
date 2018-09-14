package uk.nhs.careconnect.ri.facade.ccrifhir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${jolokia.username}")
    private String jolokiaUsername;

    @Value("${jolokia.password}")
    private String jolokiaPassword;

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(jolokiaUsername)
                .password(jolokiaPassword)
                .roles("ACTUATOR");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/").permitAll().and().csrf().disable();

        http
                .authorizeRequests()
                .antMatchers("/error").permitAll()
                .antMatchers("/jolokia/**").hasRole("ACTUATOR")
                .and().httpBasic();
    }
}

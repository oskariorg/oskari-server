package fi.nls.oskari.spring.security.database;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.SpringEnvHelper;
import fi.nls.oskari.spring.security.OskariLoginFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Database-based authentication for Oskari.
 */
@Profile(SpringEnvHelper.PROFILE_LOGIN_DB)
@Configuration
@EnableWebSecurity
public class OskariDatabaseSecurityConfig {

    private static final Logger log = LogFactory.getLogger(OskariDatabaseSecurityConfig.class);
    private final SpringEnvHelper env;

    private final OskariAuthenticationProvider oskariAuthenticationProvider;

    private final OskariAuthenticationSuccessHandler oskariAuthenticationSuccessHandler;
    @Autowired
    public OskariDatabaseSecurityConfig(SpringEnvHelper env,
                                        OskariAuthenticationProvider oskariAuthenticationProvider,
                                        OskariAuthenticationSuccessHandler oskariAuthenticationSuccessHandler) {
        this.env = env;
        this.oskariAuthenticationProvider = oskariAuthenticationProvider;
        this.oskariAuthenticationSuccessHandler = oskariAuthenticationSuccessHandler;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring database login");

        // Add custom authentication provider
        http.authenticationProvider(oskariAuthenticationProvider);

        // Disable frame options and CSRF for embedded maps
        http.headers(headers -> headers.frameOptions().disable());
        http.csrf(csrf -> csrf.disable());

        //http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        // Configure form login
        http.formLogin(form -> form
            .loginProcessingUrl(env.getLoginUrl())
            .passwordParameter(env.getParam_password())
            .usernameParameter(env.getParam_username())
            .failureHandler(new OskariLoginFailureHandler("/?loginState=failed"))
            .successHandler(oskariAuthenticationSuccessHandler)
            .loginPage("/")
        );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}

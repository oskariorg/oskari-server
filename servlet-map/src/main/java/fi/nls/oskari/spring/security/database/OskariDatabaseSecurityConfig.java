package fi.nls.oskari.spring.security.database;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.SpringEnvHelper;
import fi.nls.oskari.spring.security.OskariLoginFailureHandler;
import org.oskari.spring.OskariSpringSecurityDsl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
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
        http.with(OskariSpringSecurityDsl.oskariCommonDsl(),
                (dsl) -> dsl
                        .setLogoutUrl(env.getLogoutUrl())
                        .setLogoutSuccessUrl(env.getLoggedOutPage())
        );

        // Add custom authentication provider
        http.authenticationProvider(oskariAuthenticationProvider);
        http.authorizeHttpRequests(
                // the user can access any url without logging in (guests can see geoportal)
                // but we want to be explicit about it to have the user available on any request
                authorize -> authorize.anyRequest().permitAll())
                .formLogin(form -> form
                        .loginProcessingUrl(env.getLoginUrl())
                        .passwordParameter(env.getParam_password())
                        .usernameParameter(env.getParam_username())
                        .failureHandler(new OskariLoginFailureHandler("/?loginState=failed"))
                        .successHandler(oskariAuthenticationSuccessHandler)
                        .loginPage("/")
                );

        return http.build();
    }
}

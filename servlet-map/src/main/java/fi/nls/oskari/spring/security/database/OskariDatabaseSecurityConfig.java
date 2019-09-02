package fi.nls.oskari.spring.security.database;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.SpringEnvHelper;
import fi.nls.oskari.spring.security.OskariLoginFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Database based authentication on Oskari
 */
@Profile(SpringEnvHelper.PROFILE_LOGIN_DB)
@Configuration
@EnableWebSecurity
@Order(1)
public class OskariDatabaseSecurityConfig extends WebSecurityConfigurerAdapter {
    private Logger log = LogFactory.getLogger(OskariDatabaseSecurityConfig.class);

    @Autowired
    private SpringEnvHelper env;

    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuring database login");
/*
 * We want to permitAll since guests can access everything. What makes the login work is that:
 * - loginProcessingUrl is the login form action url
 * - passwordParameter/usernameParameter matches the login form fields
 * - loginPage might not be needed since we permit all URLs
 */
        http.authenticationProvider( new OskariAuthenticationProvider() );
        http.headers().frameOptions().disable();

        // 3rd party cookie blockers don't really work with cookie based CSRF protection on embedded maps.
        // Configure nginx to attach SameSite-flag to cookies instead.
        http.csrf().disable();

        // IMPORTANT! Only antMatch for processing url, otherwise SAML security filters are passed even if both are active
        http.formLogin()
            .loginProcessingUrl(env.getLoginUrl())
            .passwordParameter(env.getParam_password())
            .usernameParameter(env.getParam_username())
            .failureHandler(new OskariLoginFailureHandler("/?loginState=failed"))
            .successHandler(new OskariAuthenticationSuccessHandler())
            .loginPage("/");
    }
}

package fi.nls.oskari.spring.security;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.SpringEnvHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Common security config for logging out.
 */
@Configuration
@EnableWebSecurity
@Order(2)
public class OskariCommonSecurityConfig extends WebSecurityConfigurerAdapter {

    private Logger log = LogFactory.getLogger(OskariCommonSecurityConfig.class);

    @Autowired
    private SpringEnvHelper env;

    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuring common security options");

        final String logoutUrl = env.getLogoutUrl();

        // 3rd party cookie blockers don't really work with cookie based CSRF protection on embedded maps.
        // Configure nginx to attach SameSite-flag to cookies instead.
        http.csrf().disable();
        http
            .headers().frameOptions().disable()
            .and()
                // IMPORTANT! Only antMatch for logoutUrl, otherwise SAML security filters are passed even if active
                //            also server-extensions with custom config are affected
                // FIXME: When we want to use SAML singleLogout, we should disable this and call /saml/SingleLogout
                .antMatcher(logoutUrl)
                // NOTE! With CSRF enabled logout needs to happen with POST request
                .logout()
                .logoutUrl(logoutUrl)
                .deleteCookies("oskaristate","JSESSIONID", "CSRF-TOKEN")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/");
    }
}

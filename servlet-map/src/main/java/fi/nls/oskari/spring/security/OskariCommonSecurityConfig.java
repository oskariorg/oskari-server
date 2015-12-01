package fi.nls.oskari.spring.security;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.EnvHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Common security config for logging out.
 */
@Configuration
@EnableWebSecurity
@Order(2)
public class OskariCommonSecurityConfig extends WebSecurityConfigurerAdapter {

    private Logger log = LogFactory.getLogger(OskariCommonSecurityConfig.class);

    @Autowired
    private EnvHelper env;

    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuring common security options");

        http.csrf().disable();

        final String logoutUrl = env.getLogoutUrl();

        // IMPORTANT! Only antMatch for logoutUrl, otherwise SAML security filters are passed even if active
        // FIXME: When we want to use SAML singleLogout, we should disable this and call /saml/SingleLogout
        http
                .headers().frameOptions().disable()
            .antMatcher(logoutUrl)
            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(logoutUrl))
                .logoutUrl(logoutUrl)
                .invalidateHttpSession(true)
                .deleteCookies("oskaristate")
                .logoutSuccessUrl("/");
    }
}

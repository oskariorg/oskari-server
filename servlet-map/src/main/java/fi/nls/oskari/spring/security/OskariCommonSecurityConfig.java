package fi.nls.oskari.spring.security;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.SpringEnvHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Common security config for logging out.
 */
@Configuration
@EnableWebSecurity
@Order(2)
public class OskariCommonSecurityConfig {

    private Logger log = LogFactory.getLogger(OskariCommonSecurityConfig.class);

    private final SpringEnvHelper env;

    public OskariCommonSecurityConfig(SpringEnvHelper env) {
        this.env = env;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring common security options");

        final String logoutUrl = env.getLogoutUrl();

        // 3rd party cookie blockers don't really work with cookie-based CSRF protection on embedded maps.
        // Configure nginx to attach SameSite-flag to cookies instead.
        http.csrf(csrf -> csrf.disable());

        http.headers(headers -> headers.frameOptions().disable())
            .logout(logout -> logout
                .logoutUrl(logoutUrl)
                .deleteCookies("oskaristate", "JSESSIONID", "CSRF-TOKEN")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/")
            );

        return http.build();
    }
}

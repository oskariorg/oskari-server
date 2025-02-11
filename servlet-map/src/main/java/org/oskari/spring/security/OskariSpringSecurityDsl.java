package org.oskari.spring.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import jakarta.servlet.Filter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class OskariSpringSecurityDsl extends AbstractHttpConfigurer<OskariSpringSecurityDsl, HttpSecurity> {
    private boolean disableFrameOpts = true;
    private boolean disableUnnecessarySessions = true;
    private boolean disableHSTS = false;
    private boolean useCommonLogout = true;
    private Filter loginFilter = null;
    private Filter preAuthFilter = null;
    private String logoutUrl = "/logout";
    private String logoutSuccessUrl = "/";

    @Override
    public void init(HttpSecurity http) throws Exception {
        // any method that adds another configurer
        // must be done in the init method

        disableCSRF(http);
        if (disableFrameOpts) {
            disableFrameOptions(http);
        }
        if (disableHSTS) {
            disableHSTS(http);
        }
        if (disableUnnecessarySessions) {
            disableUnnecessarySessions(http);
        }
        if (useCommonLogout) {
            configLogout(http);
        }
    }

    public static OskariSpringSecurityDsl oskariCommonDsl() {
        return new OskariSpringSecurityDsl();
    }
    @Override
    public void configure(HttpSecurity http) throws Exception {

        if (loginFilter != null) {
            http.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);
        }

        if (preAuthFilter != null) {
            http.addFilterBefore(preAuthFilter, AbstractPreAuthenticatedProcessingFilter.class);
        }
    }

    public OskariSpringSecurityDsl setAllowMapsToBeEmbedded(boolean disableFrameOpts) {
        this.disableFrameOpts = disableFrameOpts;
        return this;
    }

    public OskariSpringSecurityDsl setDisableUnnecessarySessions(boolean disableUnnecessarySessions) {
        this.disableUnnecessarySessions = disableUnnecessarySessions;
        return this;
    }

    public OskariSpringSecurityDsl setDisableHSTS(boolean disableHSTS) {
        this.disableHSTS = disableHSTS;
        return this;
    }

    public OskariSpringSecurityDsl setLoginFilter(Filter loginFilter) {
        this.loginFilter = loginFilter;
        return this;
    }

    public OskariSpringSecurityDsl setPreAuthFilter(Filter preAuthFilter) {
        this.preAuthFilter = preAuthFilter;
        return this;
    }

    public OskariSpringSecurityDsl setUseCommonLogout(boolean useCommonLogout) {
        this.useCommonLogout = useCommonLogout;
        return this;
    }

    public OskariSpringSecurityDsl setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
        return this;
    }

    public OskariSpringSecurityDsl setLogoutSuccessUrl(String logoutSuccessUrl) {
        this.logoutSuccessUrl = logoutSuccessUrl;
        return this;
    }

    public void configLogout(HttpSecurity http) throws Exception {
        configLogout(http, logoutUrl, logoutSuccessUrl);
    }
    public static void configLogout(HttpSecurity http, String pLogoutUrl, String pLogoutSuccessUrl) throws Exception {
        http.logout(logout -> logout
                .logoutUrl(pLogoutUrl)
                .deleteCookies("oskaristate", "JSESSIONID", "CSRF-TOKEN")
                .invalidateHttpSession(true)
                .logoutSuccessUrl(pLogoutSuccessUrl)
        );
    }

    public static void disableFrameOptions(HttpSecurity http) throws Exception {
        // we need to disable frame options or embedding maps doesn't work
        http.headers(headers ->
                // Disable frame options and CSRF for embedded maps
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
    }

    public static void disableCSRF(HttpSecurity http) throws Exception {
        // 3rd party cookie blockers don't really work with cookie based CSRF protection on embedded maps.
        // Configure nginx/httpd to attach SameSite-flag to cookies instead.
        http.csrf(AbstractHttpConfigurer::disable);
    }

    public static void disableHSTS(HttpSecurity http) throws Exception {
        http.headers(headers ->
            // Disable HSTS header, we don't want to force HTTPS for ALL requests
            headers.httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable));
    }

    public static void disableUnnecessarySessions(HttpSecurity http) throws Exception {
        // Don't create unnecessary sessions
        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
    }

}

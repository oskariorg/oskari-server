package org.oskari.spring;

import fi.nls.oskari.util.EnvHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Profiles;

@Component
public class SpringEnvHelper {

    public static final String PROFILE_LOGIN_DB = "LoginDatabase";

    // login related properties
    private final boolean handleLoginForm;
    private final String loggedOutPage;
    private final String param_username;
    private final String param_password;
    // path to geoportal map
    private final String mapUrl;

    @Autowired
    private Environment springEnvironment;

    public SpringEnvHelper() {
        mapUrl = PropertyUtil.get("oskari.map.url", "/");
        // login related properties
        loggedOutPage = PropertyUtil.get("auth.loggedout.page", PropertyUtil.get("oskari.map.url", "/"));
        handleLoginForm = PropertyUtil.getOptional("oskari.request.handleLoginForm", true);
        param_username = PropertyUtil.get("auth.login.field.user", "j_username");
        param_password = PropertyUtil.get("auth.login.field.pass", "j_password");
    }

    public boolean isDBLoginEnabled() {
        return isProfileEnabled(PROFILE_LOGIN_DB);
    }

    public boolean isProfileEnabled(String profile) {
        return springEnvironment.acceptsProfiles(Profiles.of(profile));
    }

    public boolean isHandleLoginForm() {
        return handleLoginForm;
    }

    public String getLoggedOutPage() {
        return loggedOutPage;
    }

    public String getParam_username() {
        return param_username;
    }

    public String getParam_password() {
        return param_password;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public String getLoginUrl() {
        String url = EnvHelper.getLoginUrl();
        if(url != null && !url.isEmpty()) {
            return url;
        }
        return "/j_security_check";
    }
    public boolean isRegistrationAllowed() {
        return EnvHelper.isRegistrationAllowed();
    }
    public String getRegisterUrl() {
        return EnvHelper.getRegisterUrl();
    }
    public String getLogoutUrl() {
        return EnvHelper.getLogoutUrl();
    }
}

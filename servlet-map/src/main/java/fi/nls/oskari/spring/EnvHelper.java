package fi.nls.oskari.spring;

import fi.nls.oskari.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 5.11.2014
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
@Component
public class EnvHelper {

    public static final String PROFILE_LOGIN_DB = "LoginDatabase";
    public static final String PROFILE_LOGIN_SAML = "LoginSAML";

    // login related properties
    private boolean handleLoginForm;
    private String loginUrl;
    private String loginUrlSAML = "/saml/login";
    private String logoutUrl;
    private String registerUrl;
    private String loggedOutPage;
    private String param_username;
    private String param_password;

    private String mapUrl;

    @Autowired
    private Environment springEnvironment;

    public EnvHelper() {
        mapUrl = PropertyUtil.get("oskari.map.url", "/");
        // login related properties
        logoutUrl = PropertyUtil.get("auth.logout.url", "/logout");
        registerUrl = PropertyUtil.get("auth.register.url", "/user");
        loggedOutPage = PropertyUtil.get("auth.loggedout.page", PropertyUtil.get("oskari.map.url", "/"));
        handleLoginForm = PropertyUtil.getOptional("oskari.request.handleLoginForm", true);
        loginUrl = PropertyUtil.get("auth.login.url", "/j_security_check");
        param_username = PropertyUtil.get("auth.login.field.user", "j_username");
        param_password = PropertyUtil.get("auth.login.field.pass", "j_password");
    }

    public boolean isSAMLEnabled() {
        return springEnvironment.acceptsProfiles(PROFILE_LOGIN_SAML);
    }
    public boolean isDBLoginEnabled() {
        return springEnvironment.acceptsProfiles(PROFILE_LOGIN_DB);
    }

    public boolean isHandleLoginForm() {
        return handleLoginForm;
    }

    public String getLoginUrl() {
        return loginUrl;
    }
    public String getRegisterUrl() {
        return registerUrl;
    }

    public String getLoginUrlSAML() {
        return loginUrlSAML;
    }

    public String getLogoutUrl() {
        return logoutUrl;
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
}

package fi.nls.oskari.map.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;
import java.util.List;
import java.util.Locale;

/**
 * Wrapper request that can be used to override request values but defaults to actual if setters haven't been not called.
 * User: SMAKINEN
 * Date: 7.5.2014
 * Time: 15:14
 */
public class OskariRequest extends HttpServletRequestWrapper {

    private Locale locale;
    private Principal principal;
    private List<String> roles;

    public OskariRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public Locale getLocale() {
        if(locale == null) {
            return super.getLocale();
        }
        return locale;
    }

    @Override
    public Principal getUserPrincipal() {
        if(principal == null) {
            return super.getUserPrincipal();
        }
        return principal;
    }


    public boolean isUserInRole(String role) {
        if (roles == null) {
            return super.isUserInRole(role);
        }
        return roles.contains(role);
    }

    public void setRoles(List<String> param) {
        roles = param;
    }

    public void setLocale(final Locale param) {
        locale = param;
    }

    public void setUserPrincipal(final Principal param) {
        principal = param;
    }

    public void setUserPrincipal(final String username) {
        principal = new Principal() {
            @Override
            public String getName() {
                return username;
            }
        };
    }

}

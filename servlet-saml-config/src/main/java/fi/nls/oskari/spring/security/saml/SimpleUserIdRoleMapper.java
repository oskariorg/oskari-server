package fi.nls.oskari.spring.security.saml;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.springframework.security.saml.SAMLCredential;

import java.util.Set;

/**
 * Maps Oskari user roles based on SAMLCredential nameID value.
 * Example configuration:
 * - oskari.saml.mapper=fi.nls.oskari.spring.security.saml.SimpleUserIdRoleMapper
 * - oskari.saml.mapper.role.User=*
 * - oskari.saml.mapper.role.Admin=oskari, zakarfin
 *
 * The above would map role named 'User' to all users (attribute value * means any value)
 * and role named 'Admin' to users where name id has a value of either 'oskari' or 'zakarfin'
 */
public class SimpleUserIdRoleMapper extends SimpleRoleMapper {

    private Logger log = LogFactory.getLogger(SimpleUserIdRoleMapper.class);

    /**
     * Clears any previously assigned roles and maps new ones based on role mapping
     */
    public void mapUser(SAMLCredential credential, User user) throws Exception {
        super.mapUser(credential, user);
        final String value = credential.getNameID().getValue();
        Set<Role> roles = getRolesForValue(value);
        for(Role role : roles) {
            user.addRole(role);
        }
    }
}

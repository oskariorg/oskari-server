package fi.nls.oskari.spring.security.saml;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.security.saml.SAMLCredential;

import java.util.Set;

/**
 * Maps Oskari user roles based on SAMLCredential attribute value.
 * Example configuration:
 * - oskari.saml.mapper=fi.nls.oskari.spring.security.saml.SimpleAttributeRoleMapper
 * - oskari.saml.mapper.attribute=FirstName
 * - oskari.saml.mapper.role.User=*
 * - oskari.saml.mapper.role.Admin=Sami, Matti
 *
 * The above would map role named 'User' to all users (attribute value * means any value)
 * and role named 'Admin' to users where attribute FirstName has a value of either 'Sami' or 'Matti'
 */
public class SimpleAttributeRoleMapper extends SimpleRoleMapper {

    private Logger log = LogFactory.getLogger(SimpleAttributeRoleMapper.class);
    private String attributeName = null;

    /**
     * Initializes the role mapping based on properties
     */
    public void init() {
        super.init();
        attributeName = PropertyUtil.get("oskari.saml.mapper.attribute");
    }

    /**
     * Clears any previously assigned roles and maps new ones based on role mapping
     */
    public void mapUser(SAMLCredential credential, User user) throws Exception {
        super.mapUser(credential, user);
        final String value = credential.getAttributeAsString(attributeName);
        Set<Role> roles = getRolesForValue(value);
        for(Role role : roles) {
            user.addRole(role);
        }
    }
}

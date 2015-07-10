package fi.nls.oskari.spring.security.saml;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.security.saml.SAMLCredential;

import java.util.*;

/**
 * Baseclass for attribute/userId role mappers.
 */
public abstract class SimpleRoleMapper implements OskariUserMapper {
    private Logger log = LogFactory.getLogger(SimpleRoleMapper.class);

    protected Map<String, Set<Role>> roleMap = new HashMap<String, Set<Role>>();
    protected String DEFAULT_ROLE_MAPPING = "*";
    private final static String PREFIX_PROPERTY = "oskari.saml.mapper.role.";

    /**
     * Initializes the role mapping based on properties
     */
    public void init() {
        // ensure that default roles is never <null>
        roleMap.put(DEFAULT_ROLE_MAPPING, new HashSet<Role>());
        final List<String> propsList = PropertyUtil.getPropertyNamesStartingWith(PREFIX_PROPERTY);
        final int len = PREFIX_PROPERTY.length();
        for(String prop : propsList) {
            final String roleName = prop.substring(len);
            String[] attrValues = PropertyUtil.getCommaSeparatedList(prop);
            mapAttributeValuesForRoles(getRoleByName(roleName), attrValues);
        }
    }

    /**
     * Adds entries to the role mapping for each value. Adds the role to a set of previously
     * mapped roles or creates a new mapping if one doesn't exist.
     * @param role
     * @param values
     */
    protected void mapAttributeValuesForRoles(final Role role, final String[] values) {
        if(role == null) {
            return;
        }
        for(String value : values) {
            Set<Role> roleSet = roleMap.get(value);
            if(roleSet == null) {
                roleSet = new HashSet<>();
                roleMap.put(value, roleSet);
            }
            roleSet.add(role);
        }
    }

    /**
     * Clears any previously assigned roles.
     */
    public void mapUser(SAMLCredential credential, User user) throws Exception {
        if(roleMap.isEmpty()) {
            // init on first call
            init();
        }
        // clear default role
        user.clearRoles();
    }
    /**
     * Combines any specific roles and default roles to a new set of roles to be
     * added to user.
     * @param value
     * @return
     */
    protected Set<Role> getRolesForValue(final String value) {
        Set<Role> result = new HashSet<>();
        Set<Role> roles = roleMap.get(value);
        Set<Role> defaultRoles = roleMap.get(DEFAULT_ROLE_MAPPING);
        if(roles != null) {
            result.addAll(roles);
        }
        result.addAll(defaultRoles);
        return result;
    }

    /**
     * Finds a Role object corresponding the name of the role.
     * @param roleName
     * @return
     */
    protected Role getRoleByName(final String roleName) {
        try {
            return UserService.getInstance().getRoleByName(roleName);
        }
        catch (Exception ignored) { }
        log.error("Couldn't get role for name", roleName, "- Rolemapping will fail!");
        return null;
    }
}

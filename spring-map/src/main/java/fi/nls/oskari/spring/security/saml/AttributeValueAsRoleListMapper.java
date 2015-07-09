package fi.nls.oskari.spring.security.saml;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.security.saml.SAMLCredential;

import java.util.StringTokenizer;

/**
 * Maps Oskari user roles based on SAMLCredential attribute value.
 * Example configuration:
 * - oskari.saml.mapper=fi.nls.oskari.spring.security.saml.AttributeValueAsRoleListMapper
 * - oskari.saml.mapper.attribute=RolesAsArray
 * - oskari.saml.mapper.attribute.delimiter=,
 * - oskari.saml.mapper.attribute.rolePrefix=ROLE_
 * - oskari.saml.mapper.attribute.clearPreviousRoles=true
 *
 * The above would;
 *  - read the attributed with name 'RolesAsArray' (as specified in oskari.saml.mapper.attribute)
 *  - use the value of the attribute as a list or rolenames delimited by comma (as specified in oskari.saml.mapper.attribute.delimiter) (optional - defaults to ,)
 *  - prefix each rolename from attribute with ROLE_ (as specified in oskari.saml.mapper.attribute.rolePrefix) (optional - defaults to "")
 *  - clear any previous roles from User and only use SAML-specific ones (as specified in oskari.saml.mapper.attribute.clearPreviousRoles) (optional - defaults to false to keep default logged in role)
 */
public class AttributeValueAsRoleListMapper implements OskariUserMapper {

    private Logger log = LogFactory.getLogger(AttributeValueAsRoleListMapper.class);
    private String attributeName = null;
    private String delimiter = null;
    private String rolePrefix = null;
    private boolean clearPreviousRoles = false;

    public AttributeValueAsRoleListMapper() {
        init();
    }

    /**
     * Initializes the role mapping based on properties
     */
    public void init() {
        attributeName = PropertyUtil.get("oskari.saml.mapper.attribute");
        delimiter = PropertyUtil.get("oskari.saml.mapper.attribute.delimiter", ",");
        rolePrefix = PropertyUtil.get("oskari.saml.mapper.attribute.rolePrefix", "");
        clearPreviousRoles = PropertyUtil.getOptional("oskari.saml.mapper.attribute.clearPreviousRoles", false);
    }

    /**
     * Maps roles for user based on configured attributes value
     */
    public void mapUser(SAMLCredential credential, User user) throws Exception {
        if(clearPreviousRoles) {
            user.clearRoles();
        }
        final String value = credential.getAttributeAsString(attributeName);
        if(value == null) {
            log.debug("Mapped attribute:", attributeName, "doesn't exist. Couldn't get roles!");
            return;
        }
        final StringTokenizer tokenizer = new StringTokenizer(value, delimiter);
        while(tokenizer.hasMoreTokens()) {
            final String rolename = tokenizer.nextToken().trim();
            user.addRole(-1, rolePrefix + rolename);
        }
        log.debug("Mapped attribute:", attributeName, "with value:", value, "to roles:",user.getRoles());
    }
}

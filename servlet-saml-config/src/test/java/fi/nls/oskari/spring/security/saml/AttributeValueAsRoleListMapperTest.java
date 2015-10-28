package fi.nls.oskari.spring.security.saml;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.saml.SAMLCredential;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AttributeValueAsRoleListMapperTest {

    /*
     * - oskari.saml.mapper.attribute=RolesAsArray
     * - oskari.saml.mapper.attribute.delimiter=,
     * - oskari.saml.mapper.attribute.rolePrefix=ROLE_
     * - oskari.saml.mapper.attribute.clearPreviousRoles=true
     */
    final String ATTR_NAME = "RolesAttrib";

    private AttributeValueAsRoleListMapper mapper = new AttributeValueAsRoleListMapper();
    @Before
    public void setup() throws Exception {
        PropertyUtil.addProperty("oskari.saml.mapper.attribute", ATTR_NAME);
    }

    @After
    public void afterTest() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testMapUserWithNoAttributes() throws Exception {

        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", Collections.<String, String>emptyMap());
        mapper.init();
        User user = new User();
        mapper.mapUser(credential, user);
        assertEquals(user.getRoles().size(), 0);
    }
    @Test
    public void testMapUserSingleRoleAttribute() throws Exception {
        Map<String, String> map  = new HashMap<>();
        map.put(ATTR_NAME, "role1");
        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", map);
        mapper.init();
        User user = new User();
        mapper.mapUser(credential, user);
        assertEquals(user.getRoles().size(), 1);
        assertEquals(user.getRoles().iterator().next().getName(), "role1");
    }

    @Test
    public void testMapUserMultipleRolesAttribute() throws Exception {
        Map<String, String> map  = new HashMap<>();
        map.put(ATTR_NAME, "role1,role2,role3");
        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", map);
        mapper.init();
        User user = new User();
        mapper.mapUser(credential, user);
        assertEquals(user.getRoles().size(), 3);
        for(Role role: user.getRoles()) {
            assertTrue("role1".equals(role.getName()) || "role2".equals(role.getName()) || "role3".equals(role.getName()));
        }
    }

    @Test
    public void testMapUserMultipleRolesAttributePrefixAndDelimiter() throws Exception {
        Map<String, String> map  = new HashMap<>();
        map.put(ATTR_NAME, "role1 |role2| role3");
        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", map);
        PropertyUtil.addProperty("oskari.saml.mapper.attribute.delimiter", "|");
        PropertyUtil.addProperty("oskari.saml.mapper.attribute.rolePrefix", "ROLE_");
        mapper.init();
        User user = new User();
        mapper.mapUser(credential, user);
        assertEquals(user.getRoles().size(), 3);
        for(Role role: user.getRoles()) {
            assertTrue("ROLE_role1".equals(role.getName()) ||  "ROLE_role2".equals(role.getName()) || "ROLE_role3".equals(role.getName()));
        }
    }
}
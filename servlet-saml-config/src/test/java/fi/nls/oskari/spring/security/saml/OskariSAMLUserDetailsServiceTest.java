package fi.nls.oskari.spring.security.saml;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.user.DatabaseUserService;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.saml.SAMLCredential;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class OskariSAMLUserDetailsServiceTest {

    private OskariSAMLUserDetailsService service = null;


    @Before
    public void setup() throws Exception {
        PropertyUtil.addProperty("oskari.user.service", DatabaseUserService.class.getName());
        service = new OskariSAMLUserDetailsService();
    }

    @After
    public void afterTest() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testPersistentNameId() throws Exception {
        Map<String, String> map = new HashMap<>();
        //map.put(ATTR_NAME, "role1,role2,role3");
        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", map);
        User user = service.determineUniqueId(credential);
        assertEquals("Nameid should be used as screenname", user.getScreenname(), "test name id");
    }


    @Test(expected = RuntimeException.class)
    public void testTransientNameIdWithNoAttribsOrConfig() throws Exception {
        Map<String, String> map = new HashMap<>();
        //map.put(ATTR_NAME, "role1,role2,role3");
        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", map, "transient");
        service.determineUniqueId(credential);
    }

    @Test
    public void testTransientNameIdWithNoConfig() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("email", "oskari@nls.fi");
        //PropertyUtil.addProperty("oskari.saml.credential.");
        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", map, "transient");
        User user = service.determineUniqueId(credential);
        assertEquals("Email should be used as screenname", user.getScreenname(), "oskari@nls.fi");
    }

    @Test(expected = RuntimeException.class)
    public void testTransientNameIdWithEmptyEmail() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("email", "");
        //PropertyUtil.addProperty("oskari.saml.credential.");
        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", map, "transient");
        service.determineUniqueId(credential);
    }

    @Test
    public void testTransientNameIdWithConfig() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("email", "oskari@nls.fi");
        map.put("uniqueAttr", "snowflake");
        PropertyUtil.addProperty(OskariSAMLUserDetailsService.PROP_UNIQUE_ID_ATTR_NAME, "uniqueAttr");
        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", map, "transient");
        User user = service.determineUniqueId(credential);
        assertEquals("Email should be used as screenname", user.getScreenname(), "snowflake");
    }

    @Test
    public void testTransientNameIdWithEmailConfig() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("someEmailAttr", "oskari@nls.fi");
        PropertyUtil.addProperty("oskari.saml.credential.email", "someEmailAttr");
        // need to create service again to use the email mapping
        service = new OskariSAMLUserDetailsService();
        SAMLCredential credential = SAMLCredentialHelper.createCredential("test name id", map, "transient");
        User user = service.determineUniqueId(credential);
        assertEquals("Email should be used as screenname", user.getScreenname(), "oskari@nls.fi");
    }

}
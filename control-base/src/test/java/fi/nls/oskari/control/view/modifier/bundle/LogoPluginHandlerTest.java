package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.JSONTestHelper;
import fi.nls.test.util.ResourceHelper;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

/**
 * Created by SMAKINEN on 29.5.2015.
 */
public class LogoPluginHandlerTest extends TestCase {

    private LogoPluginHandler handler = new LogoPluginHandler();


    @After
    public void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testSetupLogoPluginConfigNullValue() {
        JSONObject modified = handler.setupLogoPluginConfig(null);
        assertNull("Should return null if given null", modified);
    }

    @Test
    public void testSetupLogoPluginConfigWrongPlugin() {
        JSONObject pluginConfig = JSONHelper.createJSONObject(LogoPluginHandler.KEY_ID, "Wrong plugin");
        JSONObject modified = handler.setupLogoPluginConfig(pluginConfig);
        assertNull("Should return null if given wrong plugin", modified);
    }

    @Test
    public void testSetupLogoPluginConfigNoProperties() {
        JSONObject pluginConfig = ResourceHelper.readJSONResource("LogoPluginConfig-empty.json", this);
        JSONObject modified = handler.setupLogoPluginConfig(pluginConfig);
        JSONTestHelper.shouldEqual(modified, ResourceHelper.readJSONResource("LogoPluginConfig-empty-expected.json", this));
    }

    @Test
    public void testSetupLogoPluginConfigSimpleProperties() throws Exception {
        PropertyUtil.addProperty("oskari.map.url", "/");
        PropertyUtil.addProperty("oskari.map.terms.url", "http://my.map.net/terms");
        JSONObject pluginConfig = ResourceHelper.readJSONResource("LogoPluginConfig-empty.json", this);
        JSONObject modified = handler.setupLogoPluginConfig(pluginConfig);
        JSONTestHelper.shouldEqual(modified, ResourceHelper.readJSONResource("LogoPluginConfig-expected-config-simple.json", this));
    }

    @Test
    public void testSetupLogoPluginConfigLocalizedProperties() throws Exception {
        PropertyUtil.addProperty("oskari.map.url.en", "/map");
        PropertyUtil.addProperty("oskari.map.url.fi", "/kartta");
        PropertyUtil.addProperty("oskari.map.terms.url.en", "/terms");
        PropertyUtil.addProperty("oskari.map.terms.url.fi", "/ehdot");
        JSONObject pluginConfig = ResourceHelper.readJSONResource("LogoPluginConfig-empty.json", this);
        JSONObject modified = handler.setupLogoPluginConfig(pluginConfig);
        JSONTestHelper.shouldEqual(modified, ResourceHelper.readJSONResource("LogoPluginConfig-expected-config-localized.json", this));
    }

    @Test
    public void testSetupLogoPluginExistingMapUrl() throws Exception {
        // provide properties
        PropertyUtil.addProperty("oskari.map.url.en", "/map");
        PropertyUtil.addProperty("oskari.map.url.fi", "/kartta");
        PropertyUtil.addProperty("oskari.map.terms.url.en", "/terms");
        PropertyUtil.addProperty("oskari.map.terms.url.fi", "/ehdot");
        // this provides existing mapurl
        JSONObject pluginConfig = ResourceHelper.readJSONResource("LogoPluginConfig-existing-config.json", this);
        JSONObject modified = handler.setupLogoPluginConfig(pluginConfig);
        // check that only terms url has been updated
        JSONTestHelper.shouldEqual(modified, ResourceHelper.readJSONResource("LogoPluginConfig-existing-config-expected.json", this));
    }
}

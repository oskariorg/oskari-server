package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.JSONTestHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by SMAKINEN on 29.5.2015.
 */
public class LogoPluginHandlerTest {

    private LogoPluginHandler handler = new LogoPluginHandler();


    @AfterEach
    public void tearDown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testSetupLogoPluginConfigNullValue() {
        JSONObject plugin = null;
        boolean success = handler.modifyPlugin(plugin, null, null);
        Assertions.assertFalse(success, "Should return false if given null");
    }

    @Test
    public void testSetupLogoPluginConfigWrongPlugin() {
        JSONObject pluginConfig = JSONHelper.createJSONObject(LogoPluginHandler.KEY_ID, "Wrong plugin");
        boolean success = handler.modifyPlugin(pluginConfig, null,  null);
        Assertions.assertFalse(success, "Should return false if given wrong plugin");
    }

    @Test
    public void testSetupLogoPluginConfigNoProperties() {
        JSONObject pluginConfig = ResourceHelper.readJSONResource("LogoPluginConfig-empty.json", this);
        handler.modifyPlugin(pluginConfig, null,  null);
        JSONTestHelper.shouldEqual(pluginConfig, ResourceHelper.readJSONResource("LogoPluginConfig-empty-expected.json", this));
    }

    @Test
    public void testSetupLogoPluginConfigSimpleProperties() throws Exception {
        PropertyUtil.addProperty("oskari.map.url", "/");
        PropertyUtil.addProperty("oskari.map.terms.url", "http://my.map.net/terms");
        JSONObject pluginConfig = ResourceHelper.readJSONResource("LogoPluginConfig-empty.json", this);
        handler.modifyPlugin(pluginConfig, null,  null);
        JSONTestHelper.shouldEqual(pluginConfig, ResourceHelper.readJSONResource("LogoPluginConfig-expected-config-simple.json", this));
    }

    @Test
    public void testSetupLogoPluginConfigLocalizedProperties() throws Exception {
        PropertyUtil.addProperty("oskari.map.url.en", "/map");
        PropertyUtil.addProperty("oskari.map.url.fi", "/kartta");
        PropertyUtil.addProperty("oskari.map.terms.url.en", "/terms");
        PropertyUtil.addProperty("oskari.map.terms.url.fi", "/ehdot");
        JSONObject pluginConfig = ResourceHelper.readJSONResource("LogoPluginConfig-empty.json", this);
        handler.modifyPlugin(pluginConfig, null,  null);
        JSONTestHelper.shouldEqual(pluginConfig, ResourceHelper.readJSONResource("LogoPluginConfig-expected-config-localized.json", this));
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
        handler.modifyPlugin(pluginConfig, null,  null);
        // check that only terms url has been updated
        JSONTestHelper.shouldEqual(pluginConfig, ResourceHelper.readJSONResource("LogoPluginConfig-existing-config-expected.json", this));
    }
}

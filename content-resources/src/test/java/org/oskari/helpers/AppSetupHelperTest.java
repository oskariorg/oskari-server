package org.oskari.helpers;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppSetupHelperTest {

    @Test
    public void readViewFile() throws Exception {
        // direct from root as the new way with no expectation
        assertEquals("root", AppSetupHelper.readViewFile("/test.json").optString("name"));
        assertEquals("apps", AppSetupHelper.readViewFile("/json/apps/custom/appsetup.json").optString("name"));

        // new "assumed path" /json/apps (should return the same custom apps as direct path to /json/apps)
        JSONObject apps = AppSetupHelper.readViewFile("custom/appsetup.json");
        assertEquals("apps", apps.optString("name"));

        // for older "assumed path" /json/views
        JSONObject views = AppSetupHelper.readViewFile("appsetup.json");
        assertEquals("views", views.optString("name"));

        // Because the search path only has test.json at root
        assertEquals("root", AppSetupHelper.readViewFile("test.json").optString("name"));

    }
}
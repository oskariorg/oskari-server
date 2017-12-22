package fi.nls.oskari.util;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.view.GetAppSetupHandler;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 30.10.2017.
 */
public class EnvHelperTest extends JSONActionRouteTest {

    @Test
    public void testIsSecure() {
        // no params test
        assertFalse("isSecure should be false with default params", EnvHelper.isSecure(createActionParams()));

        // TRUE CASES
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ActionConstants.PARAM_SECURE, "true");
        assertTrue("isSecure should be true for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'true'", EnvHelper.isSecure(createActionParams(parameters)));

        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "True");
        assertTrue("isSecure should be true for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'True'", EnvHelper.isSecure(createActionParams(parameters)));

        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "TRUE");
        assertTrue("isSecure should be true for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'TRUE'", EnvHelper.isSecure(createActionParams(parameters)));

        // FALSE CASES
        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "false");
        assertFalse("isSecure should be false for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'false'", EnvHelper.isSecure(createActionParams(parameters)));

        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "1");
        assertFalse("isSecure should be false for request with param '" + ActionConstants.PARAM_SECURE  + "' with value '1'", EnvHelper.isSecure(createActionParams(parameters)));

        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "yes");
        assertFalse("isSecure should be false for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'yes'", EnvHelper.isSecure(createActionParams(parameters)));
    }

}
package fi.nls.oskari.util;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SMAKINEN on 30.10.2017.
 */
public class EnvHelperTest extends JSONActionRouteTest {

    @Test
    public void testIsSecure() {
        // no params test
        Assertions.assertFalse(EnvHelper.isSecure(createActionParams()), "isSecure should be false with default params");

        // TRUE CASES
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ActionConstants.PARAM_SECURE, "true");
        Assertions.assertTrue(EnvHelper.isSecure(createActionParams(parameters)), "isSecure should be true for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'true'");

        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "True");
        Assertions.assertTrue(EnvHelper.isSecure(createActionParams(parameters)), "isSecure should be true for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'True'");

        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "TRUE");
        Assertions.assertTrue(EnvHelper.isSecure(createActionParams(parameters)), "isSecure should be true for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'TRUE'");

        // FALSE CASES
        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "false");
        Assertions.assertFalse(EnvHelper.isSecure(createActionParams(parameters)), "isSecure should be false for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'false'");

        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "1");
        Assertions.assertFalse(EnvHelper.isSecure(createActionParams(parameters)), "isSecure should be false for request with param '" + ActionConstants.PARAM_SECURE  + "' with value '1'");

        parameters.clear();
        parameters.put(ActionConstants.PARAM_SECURE, "yes");
        Assertions.assertFalse(EnvHelper.isSecure(createActionParams(parameters)), "isSecure should be false for request with param '" + ActionConstants.PARAM_SECURE  + "' with value 'yes'");
    }

}
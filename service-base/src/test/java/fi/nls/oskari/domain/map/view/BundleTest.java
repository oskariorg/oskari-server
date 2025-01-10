package fi.nls.oskari.domain.map.view;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BundleTest {

    @Test
    public void testConfig() {
        // tests that getConfig and getConfigJSON stays in sync
        Bundle bundle = new Bundle();
        final String INITIAL_CONFIG = JSONHelper.createJSONObject("test", "initial").toString();
        bundle.setConfig(INITIAL_CONFIG);

        final JSONObject initialConfigJSON = bundle.getConfigJSON();
        Assertions.assertEquals(INITIAL_CONFIG, initialConfigJSON.toString(), "Initial config should match config json");

        final String config = bundle.getConfig();
        final JSONObject testConfig = JSONHelper.createJSONObject("test", "modified");
        JSONHelper.putValue(bundle.getConfigJSON(), "mod", testConfig);

        Assertions.assertFalse(config.equals(bundle.getConfig()), "Config should have been changed from initial");

        final JSONObject currentConfig = JSONHelper.createJSONObject(bundle.getConfig());
        Assertions.assertTrue(JSONHelper.isEqual(currentConfig, bundle.getConfigJSON()), "Config as JSON should have been changed from initial");

        bundle.setConfig(INITIAL_CONFIG);
        Assertions.assertEquals(INITIAL_CONFIG, bundle.getConfigJSON().toString(), "Initial config should match config json after setting with setConfig");
    }

    @Test
    public void testState() {
        // tests that getState and getStateJSON stays in sync
        Bundle bundle = new Bundle();
        final String INITIAL_STATE = JSONHelper.createJSONObject("test", "initial").toString();
        bundle.setState(INITIAL_STATE);

        final JSONObject initialStateJSON = bundle.getStateJSON();
        Assertions.assertEquals(INITIAL_STATE, initialStateJSON.toString(), "Initial state should match state json");

        final String state = bundle.getState();
        final JSONObject testState = JSONHelper.createJSONObject("test", "modified");
        JSONHelper.putValue(bundle.getStateJSON(), "mod", testState);

        Assertions.assertFalse(state.equals(bundle.getState()), "State should have been changed from initial");

        final JSONObject currentState = JSONHelper.createJSONObject(bundle.getState());
        Assertions.assertTrue(JSONHelper.isEqual(currentState, bundle.getStateJSON()), "State as JSON should have been changed from initial");

        bundle.setState(INITIAL_STATE);
        Assertions.assertEquals(INITIAL_STATE, bundle.getStateJSON().toString(), "Initial config should match config json after setting with setConfig");
    }
}

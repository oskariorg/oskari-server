package fi.nls.oskari.domain.map.view;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class BundleTest {

    @Test
    public void testConfig() {
        // tests that getConfig and getConfigJSON stays in sync
        Bundle bundle = new Bundle();
        final String INITIAL_CONFIG = JSONHelper.createJSONObject("test", "initial").toString();
        bundle.setConfig(INITIAL_CONFIG);

        final JSONObject initialConfigJSON = bundle.getConfigJSON();
        assertEquals("Initial config should match config json", INITIAL_CONFIG, initialConfigJSON.toString());

        final String config = bundle.getConfig();
        final JSONObject testConfig = JSONHelper.createJSONObject("test", "modified");
        JSONHelper.putValue(bundle.getConfigJSON(), "mod", testConfig);

        assertFalse("Config should have been changed from initial", config.equals(bundle.getConfig()));

        final JSONObject currentConfig = JSONHelper.createJSONObject(bundle.getConfig());
        assertTrue("Config as JSON should have been changed from initial", JSONHelper.isEqual(currentConfig, bundle.getConfigJSON()));

        bundle.setConfig(INITIAL_CONFIG);
        assertEquals("Initial config should match config json after setting with setConfig", INITIAL_CONFIG, bundle.getConfigJSON().toString());
    }

    @Test
    public void testState() {
        // tests that getState and getStateJSON stays in sync
        Bundle bundle = new Bundle();
        final String INITIAL_STATE = JSONHelper.createJSONObject("test", "initial").toString();
        bundle.setState(INITIAL_STATE);

        final JSONObject initialStateJSON = bundle.getStateJSON();
        assertEquals("Initial state should match state json", INITIAL_STATE, initialStateJSON.toString());

        final String state = bundle.getState();
        final JSONObject testState = JSONHelper.createJSONObject("test", "modified");
        JSONHelper.putValue(bundle.getStateJSON(), "mod", testState);

        assertFalse("State should have been changed from initial", state.equals(bundle.getState()));

        final JSONObject currentState = JSONHelper.createJSONObject(bundle.getState());
        assertTrue("State as JSON should have been changed from initial", JSONHelper.isEqual(currentState, bundle.getStateJSON()));

        bundle.setState(INITIAL_STATE);
        assertEquals("Initial config should match config json after setting with setConfig", INITIAL_STATE, bundle.getStateJSON().toString());
    }
}

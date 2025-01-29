package fi.nls.oskari.control.view;

import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.JSONTestHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetAppSetupTestHelper {
    private static final String KEY_ENV = "env";
    private static final String KEY_STYLE = "oskariStyle";
    private static final String KEY_MARKERS = "svgMarkers";
    private static final String KEY_MARKER_DATA = "data";

    // to test and remove content from AppSetup json response which aren't in test resource file
    public static void verifyResponseContent (final String resourceName, final JSONObject response) {
        final JSONObject expectedResult = ResourceHelper.readJSONResource(resourceName, GetAppSetupTestHelper.class);

        JSONObject env = JSONHelper.getJSONObject(response,KEY_ENV);

        JSONObject style = JSONHelper.getJSONObject(env, KEY_STYLE);
        // tests can't get default style from db (VectorStyleService) so it should fallback to:
        JSONTestHelper.shouldEqual(style, WFSLayerOptions.getDefaultOskariStyle());

        JSONArray markers = JSONHelper.getJSONArray(env, KEY_MARKERS);
        assertTrue(markers.length() > 0, "Response env should have markers");
        for (int i = 0; i < markers.length(); i++) {
            String data = JSONHelper.getJSONObject(markers, i).optString(KEY_MARKER_DATA);
            assertFalse(data.isEmpty(), "Every marker should have data");
        }
        // remove content which aren't in resource file before testing equality
        env.remove(KEY_STYLE);
        env.remove(KEY_MARKERS);
        JSONTestHelper.shouldEqual(response, expectedResult);
    }
}

package fi.nls.test.view;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONObject;

/**
 * @author SMAKINEN
 * Helper class for creating mock bundles
 */
public class BundleTestHelper {
    /**
     * Populates a bundle object from resource file JSON
     * @param bundle
     * @return
     */
    public static Bundle loadBundle(final String bundle) {
        String name = bundle;
        if(bundle.indexOf('.') != -1) {
            name = bundle.substring(bundle.lastIndexOf(".") + 1);
        }
        return loadBundle(bundle, name);
    }

    /**
     * Populates a bundle object from resource file JSON giving it a specific instance name.
     * @param bundle
     * @param bundleInstanceName
     * @return
     */
    public static Bundle loadBundle(final String bundle, final String bundleInstanceName) {
        final Bundle b = new Bundle();
        final String jsonString = ResourceHelper.readStringResource("/views/bundles/" + bundle + ".json");
        try {
            JSONObject obj = JSONHelper.createJSONObject(jsonString);
            b.setStartup(obj.getJSONObject("startup").toString());
            b.setConfig(obj.getJSONObject("config").getJSONObject("conf").toString());
            b.setState(obj.getJSONObject("config").getJSONObject("state").toString());
            if(bundle.indexOf('.') != -1) {
                b.setName(bundle.substring(bundle.lastIndexOf(".") + 1));
            }
            b.setBundleinstance(bundleInstanceName);

        } catch (Exception e) {
            throw new RuntimeException("Couldn't create bundle " + bundle, e);
        }
        return b;
    }

}

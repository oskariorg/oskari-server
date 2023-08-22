package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ParamHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Configure bundles that should be removed from appsetup for mobile clients.
 * Configure comma-separated bundle ids in oskari-ext.properties with:
 *  actionhandler.GetAppSetup.desktopOnly.bundles = publisher2, analyse, statsgrid, mydata, userguide, myplaces3, printout, myplacesimport, feedbackService, coordinatetransformation
 *  actionhandler.GetAppSetup.mobileOnly.bundles = mobileuserguide
 */
@OskariViewModifier("mobile")
public class MobileParamHandler extends ParamHandler {

    private String[] desktopBundles = null;
    private String[] mobileBundles = null;


    public void init() {
        super.init();
        desktopBundles = PropertyUtil.getCommaSeparatedList("actionhandler.GetAppSetup.desktopOnly.bundles");
        mobileBundles = PropertyUtil.getCommaSeparatedList("actionhandler.GetAppSetup.mobileOnly.bundles");
    }

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if (params.getParamValue() == null) {
            return false;
        }
        if (ViewTypes.PUBLISHED.equals(params.getView().getType())) {
            // don't modify embedded maps
            return false;
        }

        boolean isMobile = params.getParamValue().equalsIgnoreCase("true");
        if (!isMobile) {
            // only modify when client requests mobile version
            return false;
        }
        List<String> bundleIds = getBundleIdsAsList(params.getStartupSequence());
        // remove bundles that are not supported in mobile mode
        for (String bundleId : desktopBundles) {
            int index = bundleIds.indexOf(bundleId);
            if (index != -1) {
                // bundle is included -> drop it for mobile users
                // drop bundle from startup and index list
                params.getStartupSequence().remove(index);
                bundleIds.remove(index);
            }
        }
        // add bundles for mobile client, this can be used to inject lighter replacements for desktop bundles
        for (String bundleId : mobileBundles) {
            if (!bundleIds.contains(bundleId)) {
                params.getStartupSequence().put(getBundleForStartupSeq(bundleId));
            }
        }
        return false;
    }

    private JSONObject getBundleForStartupSeq(String bundleid) {
        Bundle b = new Bundle();
        b.setName(bundleid);
        return JSONHelper.createJSONObject(b.getStartup());
    }

    private List<String> getBundleIdsAsList(JSONArray startupSequence) {
        List<String> list = new ArrayList<>(startupSequence.length());
        for(int i = 0; i < startupSequence.length(); ++i) {
            JSONObject item = startupSequence.optJSONObject(i);
            if (item == null) {
                list.add(null);
            }
            else {
                list.add(item.optString("bundlename"));
            }
        }
        return list;
    }
}

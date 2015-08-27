package fi.nls.oskari.db;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 27.6.2014
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class BundleHelper {

    private static final Logger LOG = LogFactory.getLogger(BundleHelper.class);
    private static final BundleService SERVICE = new BundleServiceIbatisImpl();

    private static final String BUNDLE_STARTUP_TEMPLATE =
            "{%n" +
                    "    \"title\": \"%s\",%n" +
                    "    \"bundleinstancename\": \"%s\",%n" +
                    "    \"bundlename\": \"%s\",%n" +
                    "    \"metadata\": {%n" +
                    "        \"Import-Bundle\": {%n" +
                    "            \"%s\": {%n" +
                    "                \"bundlePath\": \"/Oskari/packages/%s/bundle/\"%n" +
                    "            }%n" +
                    "        }%n" +
                    "    }%n" +
                    "}";

    private BundleHelper() {

    }

    public static String getDefaultBundleStartup(String namespace, final String bundleid, String title) {
        if(bundleid == null) {
            throw new RuntimeException("Missing bundleid");
        }
        final String ns = namespace == null ? "framework" : namespace;
        final String label = title == null ? bundleid : title;
        return String.format(BUNDLE_STARTUP_TEMPLATE, label, bundleid, bundleid, bundleid, ns);
    }

    public static boolean isBundleRegistered(final String id) {
        return SERVICE.getBundleTemplateByName(id) != null;
    }

    public static void registerBundle(final Bundle bundle) {
        if(isBundleRegistered(bundle.getName())) {
            // already registered
            LOG.info("Bundle", bundle.getName(), "already registered - Skipping!");
            return;
        }
        SERVICE.addBundleTemplate(bundle);
    }
}

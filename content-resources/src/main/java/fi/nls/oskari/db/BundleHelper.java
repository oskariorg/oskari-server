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
    private static final BundleService service = new BundleServiceIbatisImpl();

    private BundleHelper() {}

    public static boolean isBundleRegistered(final String id) {
        return service.getBundleTemplateByName(id) != null;
    }

    public static void registerBundle(final Bundle bundle) {
        if(isBundleRegistered(bundle.getName())) {
            // already registered
            LOG.info("Bundle", bundle.getName(), "already registered - Skipping!");
            return;
        }
        service.addBundleTemplate(bundle);
    }
}

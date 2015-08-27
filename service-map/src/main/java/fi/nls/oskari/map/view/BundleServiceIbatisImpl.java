package fi.nls.oskari.map.view;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

public class BundleServiceIbatisImpl extends BaseIbatisService<Bundle> implements
        BundleService {

    private static final Logger log = LogFactory.getLogger(BundleServiceIbatisImpl.class);
    private Cache<Bundle> bundleCache = CacheManager.getCache(getClass().getName());

    @Override
    protected String getNameSpace() {
        return "Bundle";
    }

    public Bundle getBundleTemplateByName(final String name) {
        log.debug("Finding bundle template by name:", name);
        Bundle bundle = bundleCache.get(name);
        if(bundle != null) {
            // return a clone so the template remains immutable from outside
            return bundle.clone();
        }
        bundle = queryForObject(getNameSpace() + ".find-by-name", name);
        if(bundle == null) {
            log.debug("Requested bundle not registered in db:", name);
        }
        // Not caching other than forced bundle caches on purpose.
        return bundle;
    }

    public long addBundleTemplate(final Bundle bundle) {
        log.debug("Adding bundle:", bundle);
        final Long id = queryForObject(getNameSpace() + ".add-bundle", bundle);
        bundle.setBundleId(id);
        log.debug("Got bundle id:", id);
        return id;
    }

    /**
     * Preloads and caches the bundle template by bundleid(name)
     * @param bundleid
     */
    public void forceBundleTemplateCached(final String bundleid) {
        final Bundle bundle = getBundleTemplateByName(bundleid);
        if(bundle != null) {
            bundleCache.put(bundleid, bundle);
        }
    }
}

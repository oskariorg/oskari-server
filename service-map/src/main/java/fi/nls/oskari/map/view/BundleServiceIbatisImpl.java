package fi.nls.oskari.map.view;


import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

public class BundleServiceIbatisImpl extends BaseIbatisService<Bundle> implements
        BundleService {

    private static final Logger log = LogFactory.getLogger(BundleServiceIbatisImpl.class);

    @Override
    protected String getNameSpace() {
        return "Bundle";
    }

    public Bundle getBundleTemplateByName(final String name) {
        log.debug("Finding bundle template by name:", name);
        Bundle bundle = queryForObject(getNameSpace() + ".find-by-name", name);
        if(bundle == null) {
            log.debug("Requested bundle not registered in db:", name);
        }
        else {
            log.debug("Found bundle template:", bundle);
        }
        return bundle;
    }

    public long addBundleTemplate(final Bundle bundle) {
        log.debug("Adding bundle:", bundle);
        final Long id = queryForObject(getNameSpace() + ".add-bundle", bundle);
        bundle.setBundleId(id);
        log.debug("Got bundle id:", id);
        return id;
    }
}

package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.service.db.BaseService;


public interface BundleService extends BaseService<Bundle> {

    Bundle getBundleTemplateByName(final String name);

    long addBundleTemplate(final Bundle bundle);

    void forceBundleTemplateCached(final String bundleid);
}

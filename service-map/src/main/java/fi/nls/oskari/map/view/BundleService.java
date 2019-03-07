package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.service.OskariComponent;

public abstract class BundleService extends OskariComponent {

    public abstract Bundle getBundleTemplateByName(final String name);
    public abstract long addBundleTemplate(final Bundle bundle);
    public abstract void forceBundleTemplateCached(final String bundleid);
}

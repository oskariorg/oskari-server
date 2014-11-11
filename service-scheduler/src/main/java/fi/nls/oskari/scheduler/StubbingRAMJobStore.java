package fi.nls.oskari.scheduler;

import org.quartz.simpl.RAMJobStore;

/**
 * Because both Liferay and our custom map portlet need to use Quartz for scheduling,
 * but unfortunately Liferay provides its own Quartz configuration in its portal.properties,
 * which we can only partially override in our portlet.properties (we can't remove properties
 * Liferay has already set, but we can override them with stub values), we need these stub
 * classes.
 */
public class StubbingRAMJobStore extends RAMJobStore {

    public void setTablePrefix(final String tablePrefix) {
        // ignore, this is just a stub to overcome Liferay limitations combined with the special
        // way of passing configuration parameters to the Oskari components
    }

    public void setUseProperties(final boolean useProperties) {
        // stub
    }

    public void setIsClustered(final boolean isClustered) {
        // stub
    }

    public void setDataSource(final Object dataSource) {
        // stub
    }

    public void setDataSource(final boolean dataSource) {
        // stub
    }

}

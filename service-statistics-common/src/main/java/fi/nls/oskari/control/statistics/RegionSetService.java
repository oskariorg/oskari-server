package fi.nls.oskari.control.statistics;

import java.util.List;

import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.service.OskariComponent;

public abstract class RegionSetService extends OskariComponent {

    public abstract List<RegionSet> getRegionSets();
    public abstract RegionSet getRegionSet(long id);

}

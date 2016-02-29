package fi.nls.oskari.domain.geo.comparator;

import fi.nls.oskari.domain.geo.Point;

import java.util.Comparator;

public class LonComparator implements Comparator<Point> {

    @Override
    public int compare(final Point arg0, final Point arg1) {
        final double diff = arg0.getLon() - arg1.getLon();

        if (diff < 0) {
            return -1;
        } else if (diff > 0) {
            return 1;
        }
        return 0;

    }

}

package org.geotools.mif.util;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;

public class NOPFilter implements CoordinateSequenceFilter {

    @Override
    public void filter(CoordinateSequence seq, int i) {
        // NOP
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean isGeometryChanged() {
        return false;
    }

}

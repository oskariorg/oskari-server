package org.geotools.mif.util;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;

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

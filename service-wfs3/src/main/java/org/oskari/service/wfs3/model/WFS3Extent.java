package org.oskari.service.wfs3.model;

import java.time.Instant;
import java.util.Arrays;

public class WFS3Extent {

    private final String crs = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
    private double[] spatial;
    private final String trs = "http://www.opengis.net/def/uom/ISO-8601/0/Gregorian";
    private Instant[] temporal;

    public String getCrs() {
        return crs;
    }

    public double[] getSpatial() {
        return spatial;
    }

    public void setSpatial(double[] spatial) {
        this.spatial = spatial;
    }

    public String getTrs() {
        return trs;
    }

    public Instant[] getTemporal() {
        return temporal;
    }

    public void setTemporal(Instant[] temporal) {
        this.temporal = temporal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WFS3Extent other = (WFS3Extent) obj;
        if (crs == null) {
            if (other.crs != null)
                return false;
        } else if (!crs.equals(other.crs))
            return false;
        if (!Arrays.equals(spatial, other.spatial))
            return false;
        if (!Arrays.equals(temporal, other.temporal))
            return false;
        if (trs == null) {
            if (other.trs != null)
                return false;
        } else if (!trs.equals(other.trs))
            return false;
        return true;
    }

}

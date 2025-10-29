package org.oskari.ogcapi;

import java.util.Arrays;

public class OGCAPISpatialExtent {

    private double[][] bbox;
    private String crs;

    public double[][] getBbox() {
        return bbox;
    }

    public void setBbox(double[][] bbox) {
        this.bbox = bbox;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(bbox);
        result = prime * result + ((crs == null) ? 0 : crs.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OGCAPISpatialExtent other = (OGCAPISpatialExtent) obj;
        if (!Arrays.deepEquals(bbox, other.bbox))
            return false;
        if (crs == null) {
            if (other.crs != null)
                return false;
        } else if (!crs.equals(other.crs))
            return false;
        return true;
    }

}

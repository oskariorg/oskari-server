package org.oskari.ogcapi;

public class OGCAPIExtent {

    private OGCAPISpatialExtent spatial;
    // No support for temporal extent

    public OGCAPISpatialExtent getSpatial() {
        return spatial;
    }

    public void setSpatial(OGCAPISpatialExtent spatial) {
        this.spatial = spatial;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((spatial == null) ? 0 : spatial.hashCode());
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
        OGCAPIExtent other = (OGCAPIExtent) obj;
        if (spatial == null) {
            if (other.spatial != null)
                return false;
        } else if (!spatial.equals(other.spatial))
            return false;
        return true;
    }

}

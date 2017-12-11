package fi.nls.oskari.myplaces;

import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.domain.map.MyPlace;

public class MyPlaceWithGeometry extends MyPlace {

    private Geometry geometry;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

}

package org.geotools.mif;

import java.util.HashMap;
import java.util.Map;

import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CRSFactory;
import org.geotools.api.referencing.crs.GeographicCRS;
import org.geotools.api.referencing.cs.CSFactory;
import org.geotools.api.referencing.cs.CoordinateSystemAxis;
import org.geotools.api.referencing.cs.EllipsoidalCS;
import org.geotools.api.referencing.datum.DatumFactory;
import org.geotools.api.referencing.datum.Ellipsoid;
import org.geotools.api.referencing.datum.GeodeticDatum;
import org.geotools.api.referencing.datum.PrimeMeridian;

public enum MIFDatum {

    WGS_84(104, DefaultEllipsoid.WGS84, 0, 0, 0, 0, 0, 0, 0),
    EUREF_89(115, DefaultEllipsoid.GRS80, 0, 0, 0, 0, 0, 0, 0),
    KKJ(1016, DefaultEllipsoid.INTERNATIONAL_1924, -96.062, -82.428, -121.753, 4.801, 0.345, -1.376, 1.496),
    ;

    public final int id;
    public final Ellipsoid ellipsoid;
    public final PrimeMeridian primeMeridian;
    public final BursaWolfParameters toWGS84;

    private MIFDatum(int id, Ellipsoid ellipsoid,
            double dx, double dy, double dz,
            double ex, double ey, double ez,
            double ppm) {
        this.id = id;
        this.ellipsoid = ellipsoid;
        this.primeMeridian = DefaultPrimeMeridian.GREENWICH;
        this.toWGS84 = new BursaWolfParameters(DefaultGeodeticDatum.WGS84);
        toWGS84.dx = dx;
        toWGS84.dy = dy;
        toWGS84.dz = dz;
        toWGS84.ex = ex;
        toWGS84.ey = ey;
        toWGS84.ez = ez;
        toWGS84.ppm = ppm;
    }

    public static MIFDatum find(int id) {
        for (MIFDatum it : values()) {
            if (it.id == id) {
                return it;
            }
        }
        return null;
    }

    public GeographicCRS toGeographicCRS() throws FactoryException {
        DatumFactory datumFactory = ReferencingFactoryFinder.getDatumFactory(null);
        Map<String, Object> map = new HashMap<>();
        map.put("name", name());
        map.put(DefaultGeodeticDatum.BURSA_WOLF_KEY, toWGS84);
        GeodeticDatum datum = datumFactory.createGeodeticDatum(map, ellipsoid, primeMeridian);

        CSFactory csFactory = ReferencingFactoryFinder.getCSFactory(null);
        map.clear();
        map.put("name", "<long>, <lat>");
        CoordinateSystemAxis axisLon = DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE;
        CoordinateSystemAxis axisLat = DefaultCoordinateSystemAxis.GEODETIC_LATITUDE;
        EllipsoidalCS cs = csFactory.createEllipsoidalCS(map, axisLon, axisLat);

        CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
        map.clear();
        map.put("name", name());
        return crsFactory.createGeographicCRS(map, datum, cs);
    }

}

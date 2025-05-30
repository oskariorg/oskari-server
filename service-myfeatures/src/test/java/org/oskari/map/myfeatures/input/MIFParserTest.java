package org.oskari.map.myfeatures.input;

import fi.nls.oskari.service.ServiceException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.io.File;
import java.net.URISyntaxException;

public class MIFParserTest {

    @Test
    public void testKenroAlueMaarajat() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("kenro_alue_maarajat.MIF").toURI());
        MIFParser parser = new MIFParser();
        SimpleFeatureCollection fc = parser.parse(file, null, CRS.decode("EPSG:2393", true));
        Assertions.assertEquals(2, fc.size());
        SimpleFeatureIterator it = fc.features();
        try {
            SimpleFeature f = it.next();
            Polygon geom = (Polygon) f.getDefaultGeometry();
            Assertions.assertEquals(5, geom.getNumPoints());
            Assertions.assertEquals(0, geom.getNumInteriorRing());
            LineString ext = geom.getExteriorRing();
            Coordinate c;
            c = ext.getCoordinateN(0);
            Assertions.assertEquals(3644472.125, c.x, 1e-7);
            Assertions.assertEquals(7289630.034, c.y, 1e-7);
            c = ext.getCoordinateN(1);
            Assertions.assertEquals(3644097.725, c.x, 1e-7);
            Assertions.assertEquals(7293078.173, c.y, 1e-7);

            f = it.next();
            MultiPolygon mp = (MultiPolygon) f.getDefaultGeometry();
            Assertions.assertEquals(49 + 5, mp.getNumPoints());
            Assertions.assertEquals(2, mp.getNumGeometries());
            geom = (Polygon) mp.getGeometryN(1);
            Assertions.assertEquals(0, geom.getNumInteriorRing());
            ext = geom.getExteriorRing();
            c = ext.getCoordinateN(0);
            Assertions.assertEquals(3383004.736, c.x, 1e-7);
            Assertions.assertEquals(6672746.505, c.y, 1e-7);
            c = ext.getCoordinateN(1);
            Assertions.assertEquals(3382883.26, c.x, 1e-7);
            Assertions.assertEquals(6675036.999, c.y, 1e-7);
        } finally {
            it.close();
        }
    }

}

package org.oskari.map.userlayer.input;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import fi.nls.oskari.service.ServiceException;

public class MIFParserTest {

    @Test
    public void testKenroAlueMaarajat() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("kenro_alue_maarajat.MIF").toURI());
        MIFParser parser = new MIFParser();
        SimpleFeatureCollection fc = parser.parse(file, null, CRS.decode("EPSG:2393", true));
        assertEquals(2, fc.size());
        SimpleFeatureIterator it = fc.features();
        try {
            SimpleFeature f = it.next();
            Polygon geom = (Polygon) f.getDefaultGeometry();
            assertEquals(5, geom.getNumPoints());
            assertEquals(0, geom.getNumInteriorRing());
            LineString ext = geom.getExteriorRing();
            Coordinate c;
            c = ext.getCoordinateN(0);
            assertEquals(3644472.125, c.x, 1e-7);
            assertEquals(7289630.034, c.y, 1e-7);
            c = ext.getCoordinateN(1);
            assertEquals(3644097.725, c.x, 1e-7);
            assertEquals(7293078.173, c.y, 1e-7);

            f = it.next();
            MultiPolygon mp = (MultiPolygon) f.getDefaultGeometry();
            assertEquals(49 + 5, mp.getNumPoints());
            assertEquals(2, mp.getNumGeometries());
            geom = (Polygon) mp.getGeometryN(1);
            assertEquals(0, geom.getNumInteriorRing());
            ext = geom.getExteriorRing();
            c = ext.getCoordinateN(0);
            assertEquals(3383004.736, c.x, 1e-7);
            assertEquals(6672746.505, c.y, 1e-7);
            c = ext.getCoordinateN(1);
            assertEquals(3382883.26, c.x, 1e-7);
            assertEquals(6675036.999, c.y, 1e-7);
        } finally {
            it.close();
        }
    }

}

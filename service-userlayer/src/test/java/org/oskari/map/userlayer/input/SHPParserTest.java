package org.oskari.map.userlayer.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import fi.nls.oskari.service.ServiceException;

public class SHPParserTest {

    @Test
    public void testParse() throws ServiceException, URISyntaxException, NoSuchAuthorityCodeException, FactoryException {
        SimpleFeature shp16 = getSHP16();
        Geometry geom = (Geometry) shp16.getDefaultGeometry();
        assertTrue(geom instanceof MultiPolygon);
        MultiPolygon mp = (MultiPolygon) geom;
        assertEquals(14, mp.getNumGeometries());
        Polygon p = (Polygon) mp.getGeometryN(0);
        CoordinateSequence exterior = p.getExteriorRing().getCoordinateSequence();
        Coordinate first = exterior.getCoordinate(0);
        assertEquals(194831.89, first.x, 1e6);
        assertEquals(6947066.665, first.y, 1e6);
        assertTrue(Double.isNaN(first.z));
    }

    public static SimpleFeature getSHP16() throws URISyntaxException, ServiceException, NoSuchAuthorityCodeException, FactoryException {
        SimpleFeatureCollection fc = parse("SHP2017.shp");
        List<SimpleFeature> features = collectToList(fc);
        assertEquals("There are 21 Features in the file", 21, features.size());
        CoordinateReferenceSystem crs = fc.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
        assertEquals("Projection is correctly determined from .prj file", "EPSG:3067", CRS.toSRS(crs));
        return features.stream()
                .filter(f -> ((String) f.getAttribute("Sairaanhoi")).equals("16"))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException());
    }

    private static SimpleFeatureCollection parse(String resourcePath) throws URISyntaxException, ServiceException, NoSuchAuthorityCodeException, FactoryException {
        SHPParser parser = new SHPParser();
        File file = new File(SHPParserTest.class.getResource(resourcePath).toURI());
        return parser.parse(file, null, CRS.decode("EPSG:3067"));
    }

    private static List<SimpleFeature> collectToList(SimpleFeatureCollection fc) {
        List<SimpleFeature> features = new ArrayList<>();
        try (SimpleFeatureIterator it = fc.features()) {
            while (it.hasNext()) {
                features.add(it.next());
            }
        }
        return features;
    }

    @Test
    public void testParseToDifferentProjecion() throws ServiceException, URISyntaxException, NoSuchAuthorityCodeException, FactoryException {
        File file = new File(SHPParserTest.class.getResource("SHP2017.shp").toURI());
        SHPParser parser = new SHPParser();
        SimpleFeatureCollection fc = parser.parse(file, null, CRS.decode("EPSG:4326"));
        ReferencedEnvelope bounds = fc.getBounds();
        CoordinateReferenceSystem crs = bounds.getCoordinateReferenceSystem();
        assertEquals("Bounds is transformed correctly", "EPSG:4326", CRS.toSRS(crs));
        assertEquals(59.80841, bounds.getMinX(), 1e-5);
        assertEquals(19.47275, bounds.getMinY(), 1e-5);
        assertEquals(70.09210, bounds.getMaxX(), 1e-5);
        assertEquals(31.58671, bounds.getMaxY(), 1e-5);
    }
}

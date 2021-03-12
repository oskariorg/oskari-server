package org.oskari.map.userlayer.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import fi.nls.oskari.service.ServiceException;

public class GPKGParserTest {

    @Test
    public void testSampleFile() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("building.gpkg").toURI());
        GPKGParser parser = new GPKGParser();
        SimpleFeatureCollection fc = parser.parse(file, null, CRS.decode("EPSG:3067", true));
        assertEquals("building", fc.getSchema().getName().getLocalPart());
        assertEquals(10, fc.size());
        try (SimpleFeatureIterator it = fc.features()) {
            SimpleFeature f = getById(it, "building.7");
            assertEquals("cffb9437-2bed-4026-b091-8166ffcd8bb6:4", f.getAttribute("id"));
            assertEquals("functional", f.getAttribute("conditionOfConstruction"));
            assertEquals("http://inspire.ec.europa.eu/codelist/ConditionOfConstructionValue/functional", f.getAttribute("conditionOfConstruction_href"));
            assertEquals(Timestamp.from(Instant.parse("2021-02-08T01:32:54.052Z")), f.getAttribute("beginLifespanVersion"));
            assertEquals(100, ((Number) f.getAttribute("currentUse_percentage")).intValue());
            assertNull(f.getAttribute("numberOfFloorsAboveGround"));
            
            MultiPolygon mp = (MultiPolygon) f.getDefaultGeometry();
            assertEquals(1, mp.getNumGeometries());
            Polygon polygon = (Polygon) mp.getGeometryN(0);
            assertEquals(5, polygon.getNumPoints());
            Coordinate[] coordinates = mp.getCoordinates();
            Coordinate c;
            c = coordinates[0];
            assertEquals(320982.9245551036, c.x, 1e-7);
            assertEquals(6824744.274026728, c.y, 1e-7);
            c = coordinates[1];
            assertEquals(320982.8023068382, c.x, 1e-7);
            assertEquals(6824741.728986752, c.y, 1e-7);
            c = coordinates[2];
            assertEquals(320987.6547203095, c.x, 1e-7);
            assertEquals(6824741.475417921, c.y, 1e-7);
            c = coordinates[3];
            assertEquals(320987.7788878583, c.x, 1e-7);
            assertEquals(6824744.040390403, c.y, 1e-7);
            // Ring should be closed
            assertEquals(coordinates[0].x, coordinates[4].x, 0);
            assertEquals(coordinates[0].y, coordinates[4].y, 0);
        }
    }
    
    private SimpleFeature getById(SimpleFeatureIterator it, String id) {
        while (it.hasNext()) {
            SimpleFeature f = it.next();
            if (f.getID().equals(id)) {
                return f;
            }
        }
        return null;
    }

}

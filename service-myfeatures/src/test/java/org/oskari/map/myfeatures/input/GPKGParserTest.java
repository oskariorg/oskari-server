package org.oskari.map.myfeatures.input;

import fi.nls.oskari.service.ServiceException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Instant;

public class GPKGParserTest {

    @Test
    public void testSampleFile() throws URISyntaxException, NoSuchAuthorityCodeException, ServiceException, FactoryException {
        File file = new File(getClass().getResource("building.gpkg").toURI());
        GPKGParser parser = new GPKGParser();
        SimpleFeatureCollection fc = parser.parse(file, null, CRS.decode("EPSG:3067", true));
        Assertions.assertEquals("building", fc.getSchema().getName().getLocalPart());
        Assertions.assertEquals(10, fc.size());
        try (SimpleFeatureIterator it = fc.features()) {
            SimpleFeature f = getById(it, "building.7");
            Assertions.assertEquals("cffb9437-2bed-4026-b091-8166ffcd8bb6:4", f.getAttribute("id"));
            Assertions.assertEquals("functional", f.getAttribute("conditionOfConstruction"));
            Assertions.assertEquals("http://inspire.ec.europa.eu/codelist/ConditionOfConstructionValue/functional", f.getAttribute("conditionOfConstruction_href"));
            Assertions.assertEquals(Timestamp.from(Instant.parse("2021-02-08T01:32:54.052Z")), f.getAttribute("beginLifespanVersion"));
            Assertions.assertEquals(100, ((Number) f.getAttribute("currentUse_percentage")).intValue());
            Assertions.assertNull(f.getAttribute("numberOfFloorsAboveGround"));
            
            MultiPolygon mp = (MultiPolygon) f.getDefaultGeometry();
            Assertions.assertEquals(1, mp.getNumGeometries());
            Polygon polygon = (Polygon) mp.getGeometryN(0);
            Assertions.assertEquals(5, polygon.getNumPoints());
            Coordinate[] coordinates = mp.getCoordinates();
            Coordinate c;
            c = coordinates[0];
            Assertions.assertEquals(320982.9245551036, c.x, 1e-7);
            Assertions.assertEquals(6824744.274026728, c.y, 1e-7);
            c = coordinates[1];
            Assertions.assertEquals(320982.8023068382, c.x, 1e-7);
            Assertions.assertEquals(6824741.728986752, c.y, 1e-7);
            c = coordinates[2];
            Assertions.assertEquals(320987.6547203095, c.x, 1e-7);
            Assertions.assertEquals(6824741.475417921, c.y, 1e-7);
            c = coordinates[3];
            Assertions.assertEquals(320987.7788878583, c.x, 1e-7);
            Assertions.assertEquals(6824744.040390403, c.y, 1e-7);
            // Ring should be closed
            Assertions.assertEquals(coordinates[0].x, coordinates[4].x, 0);
            Assertions.assertEquals(coordinates[0].y, coordinates[4].y, 0);
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

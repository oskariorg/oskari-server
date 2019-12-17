package org.oskari.service.mvt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class SimpleFeaturesMVTEncoderTest {

    @Test
    public void pointFeaturesInBufferZoneWontBeRemoved() throws Exception {
        Coordinate[] ca = new Coordinate[5];
        ca[0] = new Coordinate(-50, -50);
        ca[1] = new Coordinate(150, -50);
        ca[2] = new Coordinate(150, 150);
        ca[3] = new Coordinate(-50, 150);
        ca[4] = new Coordinate(-50, -50);

        GeometryFactory gf = new GeometryFactory();
        List<Point> points = Arrays.stream(ca)
                .map(c -> gf.createPoint(c))
                .collect(Collectors.toList());

        double[] bbox = { 0, 0, 100, 100 };
        double[] largerBbox = { 0, 0, 4096, 4096 };

        SimpleFeatureTypeBuilder tBuilder = new SimpleFeatureTypeBuilder();
        tBuilder.setName("test");
        tBuilder.add("geom", Point.class);
        SimpleFeatureType featureType = tBuilder.buildFeatureType();
        DefaultFeatureCollection fc = new DefaultFeatureCollection("test", featureType);

        SimpleFeatureBuilder fBuilder = new SimpleFeatureBuilder(featureType);
        for (Point point : points) {
            fBuilder.set("geom", point);
            fc.add(fBuilder.buildFeature(null));
        }

        assertEquals(5, fc.size());

        List<Geometry> inTile = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, bbox, 4096, 256);
        assertEquals(0, inTile.size());

        List<Geometry> inLargeTile = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, largerBbox, 4096, 256);
        assertEquals(5, inLargeTile.size());

        List<Geometry> inLargeTileNoBuffer = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, largerBbox, 4096, 0);
        assertEquals(1, inLargeTileNoBuffer.size());
    }

    @Test
    public void whenFeaturePolygonFullyContainsTileExtentThenFeatureIsAccepted() {
        Coordinate[] ca = new Coordinate[5];
        ca[0] = new Coordinate(-50, -50);
        ca[1] = new Coordinate(150, -50);
        ca[2] = new Coordinate(150, 150);
        ca[3] = new Coordinate(-50, 150);
        ca[4] = new Coordinate(-50, -50);
        GeometryFactory gf = new GeometryFactory();
        LinearRing exterior = gf.createLinearRing(ca);
        Polygon p = gf.createPolygon(exterior);

        double[] bbox = { 0, 0, 100, 100 };
        SimpleFeatureTypeBuilder tBuilder = new SimpleFeatureTypeBuilder();
        tBuilder.setName("test");
        tBuilder.add("geom", Polygon.class);
        SimpleFeatureType featureType = tBuilder.buildFeatureType();

        SimpleFeatureBuilder fBuilder = new SimpleFeatureBuilder(featureType);
        fBuilder.set("geom", p);
        SimpleFeature f = fBuilder.buildFeature(null);

        DefaultFeatureCollection fc = new DefaultFeatureCollection("test");
        fc.add(f);

        List<Geometry> geom = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, bbox, 4096, 256);
        assertEquals(1, geom.size());
    }

    @Test
    public void whenPolygonInteriorRingFullyContainsTileExtentThenFeatureIsIgnored() {
        GeometryFactory gf = new GeometryFactory();

        Coordinate[] ca = new Coordinate[5];
        ca[0] = new Coordinate(-50, -50);
        ca[1] = new Coordinate(150, -50);
        ca[2] = new Coordinate(150, 150);
        ca[3] = new Coordinate(-50, 150);
        ca[4] = new Coordinate(-50, -50);
        LinearRing exterior = gf.createLinearRing(ca);

        ca[0] = new Coordinate(-10, -10);
        ca[1] = new Coordinate(-10, 110);
        ca[2] = new Coordinate(110, 110);
        ca[3] = new Coordinate(110, -10);
        ca[4] = new Coordinate(-10, -10);
        LinearRing interior = gf.createLinearRing(ca);

        Polygon p = gf.createPolygon(exterior, new LinearRing[] { interior });

        double[] bbox = { 0, 0, 100, 100 };
        SimpleFeatureTypeBuilder tBuilder = new SimpleFeatureTypeBuilder();
        tBuilder.setName("test");
        tBuilder.add("geom", Polygon.class);
        SimpleFeatureType featureType = tBuilder.buildFeatureType();

        SimpleFeatureBuilder fBuilder = new SimpleFeatureBuilder(featureType);
        fBuilder.set("geom", p);
        SimpleFeature f = fBuilder.buildFeature(null);

        DefaultFeatureCollection fc = new DefaultFeatureCollection("test");
        fc.add(f);

        List<Geometry> geom = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, bbox, 4096, 256);
        assertEquals(0, geom.size());
    }

    @Test
    public void whenPolygonIsClippedItRemains() {
        GeometryFactory gf = new GeometryFactory();

        Coordinate[] ca = new Coordinate[4];
        ca[0] = new Coordinate(-20,  10);
        ca[1] = new Coordinate( 20, -10);
        ca[2] = new Coordinate(-20, -10);
        ca[3] = new Coordinate(-20,  10);
        LinearRing exterior = gf.createLinearRing(ca);

        Polygon p = gf.createPolygon(exterior, null);

        double[] bbox = { 0, 0, 100, 100 };
        SimpleFeatureTypeBuilder tBuilder = new SimpleFeatureTypeBuilder();
        tBuilder.setName("test");
        tBuilder.add("geom", Polygon.class);
        SimpleFeatureType featureType = tBuilder.buildFeatureType();

        SimpleFeatureBuilder fBuilder = new SimpleFeatureBuilder(featureType);
        fBuilder.set("geom", p);
        SimpleFeature f = fBuilder.buildFeature(null);

        DefaultFeatureCollection fc = new DefaultFeatureCollection("test");
        fc.add(f);

        List<Geometry> geom = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, bbox, 4096, 0);
        assertEquals(1, geom.size());
    }

    @Test
    public void whenPolygonWithAnInteriorRingContainingOneCornerPointIsClippedItRemains() {
        GeometryFactory gf = new GeometryFactory();

        Coordinate[] ca = new Coordinate[5];
        ca[0] = new Coordinate(-10, -10);
        ca[1] = new Coordinate(110, -10);
        ca[2] = new Coordinate(110, 110);
        ca[3] = new Coordinate(-10, 110);
        ca[4] = new Coordinate(-10, -10);
        LinearRing exterior = gf.createLinearRing(ca);

        ca = new Coordinate[4];
        ca[0] = new Coordinate( 90, -10);
        ca[1] = new Coordinate(110, -10);
        ca[2] = new Coordinate( 90,  10);
        ca[3] = new Coordinate( 90, -10);
        LinearRing interior = gf.createLinearRing(ca);

        Polygon p = gf.createPolygon(exterior, new LinearRing[] { interior });

        double[] bbox = { 0, 0, 100, 100 };
        SimpleFeatureTypeBuilder tBuilder = new SimpleFeatureTypeBuilder();
        tBuilder.setName("test");
        tBuilder.add("geom", Polygon.class);
        SimpleFeatureType featureType = tBuilder.buildFeatureType();

        SimpleFeatureBuilder fBuilder = new SimpleFeatureBuilder(featureType);
        fBuilder.set("geom", p);
        SimpleFeature f = fBuilder.buildFeature(null);

        DefaultFeatureCollection fc = new DefaultFeatureCollection("test");
        fc.add(f);

        List<Geometry> geom = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, bbox, 4096, 0);
        assertEquals(1, geom.size());
        assertEquals(Polygon.class, geom.get(0).getClass());
    }

    public void verySmallInteriorRingsAreDeleted() {
        GeometryFactory gf = new GeometryFactory();

        Coordinate[] ca = new Coordinate[5];
        ca[0] = new Coordinate(-10, -10);
        ca[1] = new Coordinate(110, -10);
        ca[2] = new Coordinate(110, 110);
        ca[3] = new Coordinate(-10, 110);
        ca[4] = new Coordinate(-10, -10);
        LinearRing exterior = gf.createLinearRing(ca);

        ca = new Coordinate[4];
        ca[0] = new Coordinate(0, 0);
        ca[1] = new Coordinate(10, 0.05);
        ca[2] = new Coordinate(9,  0.04);
        ca[3] = new Coordinate(0, 0);
        LinearRing interior = gf.createLinearRing(ca);

        Polygon p = gf.createPolygon(exterior, new LinearRing[] { interior });

        double[] bbox = { 0, 0, 100, 100 };
        SimpleFeatureTypeBuilder tBuilder = new SimpleFeatureTypeBuilder();
        tBuilder.setName("test");
        tBuilder.add("geom", Polygon.class);
        SimpleFeatureType featureType = tBuilder.buildFeatureType();

        SimpleFeatureBuilder fBuilder = new SimpleFeatureBuilder(featureType);
        fBuilder.set("geom", p);
        SimpleFeature f = fBuilder.buildFeature(null);

        DefaultFeatureCollection fc = new DefaultFeatureCollection("test");
        fc.add(f);

        List<Geometry> geom = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, bbox, 4096, 0);
        assertEquals(1, geom.size());
        assertEquals(Polygon.class, geom.get(0).getClass());
        assertNull(((Polygon) geom.get(0)).getInteriorRingN(0));
    }

    @Test
    public void testNahkela() throws Exception {
        WFSTileGrid grid = new WFSTileGrid(new double[] { -548576, 6291456, -548576 + (8192*256), 6291456 + (8192*256) }, 15);
        Map<String, Object> json;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("nahkela.json")) {
            ObjectMapper om = new ObjectMapper();
            json = om.readValue(in, new TypeReference<HashMap<String, Object>>() {});
        }
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3067");
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs);

        SimpleFeatureCollection sfc = GeoJSONReader2.toFeatureCollection(json, schema);
        double[] bbox = grid.getTileExtent(new TileCoord(10, 456, 826));
        List<Geometry> mvtGeoms = SimpleFeaturesMVTEncoder.asMVTGeoms(sfc, bbox, 4096, 256);
        assertEquals(1, mvtGeoms.size());
    }

    @Test
    public void testHyryla() throws Exception {
        WFSTileGrid grid = new WFSTileGrid(new double[] { -548576, 6291456, -548576 + (8192*256), 6291456 + (8192*256) }, 15);
        Map<String, Object> json;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("hyryla.json")) {
            ObjectMapper om = new ObjectMapper();
            json = om.readValue(in, new TypeReference<HashMap<String, Object>>() {});
        }
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3067");
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs);

        SimpleFeatureCollection sfc = GeoJSONReader2.toFeatureCollection(json, schema);
        double[] bbox = grid.getTileExtent(new TileCoord(11, 917, 1651));
        List<Geometry> mvtGeoms = SimpleFeaturesMVTEncoder.asMVTGeoms(sfc, bbox, 4096, 256);
        assertEquals(1, mvtGeoms.size());
    }
}

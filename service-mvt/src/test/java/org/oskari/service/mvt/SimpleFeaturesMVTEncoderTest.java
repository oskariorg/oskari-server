package org.oskari.service.mvt;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
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

        Assertions.assertEquals(5, fc.size());

        List<Geometry> inTile = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, bbox, 4096, 256);
        Assertions.assertEquals(0, inTile.size());

        List<Geometry> inLargeTile = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, largerBbox, 4096, 256);
        Assertions.assertEquals(5, inLargeTile.size());

        List<Geometry> inLargeTileNoBuffer = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, largerBbox, 4096, 0);
        Assertions.assertEquals(1, inLargeTileNoBuffer.size());
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
        Assertions.assertEquals(1, geom.size());
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
        Assertions.assertEquals(0, geom.size());
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
        Assertions.assertEquals(1, geom.size());
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
        Assertions.assertEquals(1, geom.size());
        Assertions.assertEquals(Polygon.class, geom.get(0).getClass());
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
        Assertions.assertEquals(1, geom.size());
        Assertions.assertEquals(Polygon.class, geom.get(0).getClass());
        Assertions.assertNull(((Polygon) geom.get(0)).getInteriorRingN(0));
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
        Assertions.assertEquals(1, mvtGeoms.size());
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
        Assertions.assertEquals(1, mvtGeoms.size());
    }

    @Test
    public void testBuildings() throws Exception {
        WFSTileGrid grid = new WFSTileGrid(new double[] { -548576, 6291456, -548576 + (8192*256), 6291456 + (8192*256) }, 15);
        Map<String, Object> json;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("buildings.json")) {
            ObjectMapper om = new ObjectMapper();
            json = om.readValue(in, new TypeReference<HashMap<String, Object>>() {});
        }
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3067");
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs);

        SimpleFeatureCollection sfc = GeoJSONReader2.toFeatureCollection(json, schema);

/*
        double[] bbox = grid.getTileExtent(new TileCoord(7, 50, 101));
        List<Geometry> mvtGeoms = SimpleFeaturesMVTEncoder.asMVTGeoms(sfc, bbox, 4096, 256);
        assertEquals(8, mvtGeoms.size()); // wdtinc & no.ecc both 2078 bytes (~150ms)

        //assertEquals("Check size" ,2078, bytes.length);
        //assertTrue("Check time" ,duration < 400);
*/
        double[] bbox = grid.getTileExtent(new TileCoord(7, 50, 102));
        List<Geometry> mvtGeoms = SimpleFeaturesMVTEncoder.asMVTGeoms(sfc, bbox, 4096, 256);
        Assertions.assertEquals(175, mvtGeoms.size()); // wdtinc & no.ecc both 33063 bytes (~200ms)

        long start = System.currentTimeMillis();
        byte[] bytes = SimpleFeaturesMVTEncoder.encodeToByteArray(sfc, "test", bbox, 4096, 256);
        long duration = System.currentTimeMillis() - start;
        System.out.println(duration + "ms -> " + bytes.length);
        Assertions.assertEquals(33063, bytes.length, "Check size");
        Assertions.assertTrue(duration < 400, "Check time"); // Should be around ~200ms but CI might be slower
    }
    @Test
    public void testPolygons() throws Exception {
        WFSTileGrid grid = new WFSTileGrid(new double[] { -548576, 6291456, -548576 + (8192*256), 6291456 + (8192*256) }, 15);
        Map<String, Object> json;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("polygons.json")) {
            ObjectMapper om = new ObjectMapper();
            json = om.readValue(in, new TypeReference<HashMap<String, Object>>() {});
        }
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3067");
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs);

        SimpleFeatureCollection sfc = GeoJSONReader2.toFeatureCollection(json, schema);

        double[] bbox = grid.getTileExtent(new TileCoord(10, 456, 826));
        List<Geometry> mvtGeoms = SimpleFeaturesMVTEncoder.asMVTGeoms(sfc, bbox, 4096, 256);
        Assertions.assertEquals(28, mvtGeoms.size()); // wdtinc 3941bytes (~300ms) / no.ecc 4539bytes (~300ms)

        long start = System.currentTimeMillis();
        byte[] bytes = SimpleFeaturesMVTEncoder.encodeToByteArray(sfc, "test", bbox, 4096, 256);

        long duration = System.currentTimeMillis() - start;
        System.out.println(duration + "ms -> " + bytes.length);
        Assertions.assertEquals(4539, bytes.length, "Check size");
        Assertions.assertTrue(duration < 500, "Check time"); // Should be around ~300ms but CI might be slower
    }
    @Test
    public void testLines() throws Exception {
        WFSTileGrid grid = new WFSTileGrid(new double[] { -548576, 6291456, -548576 + (8192*256), 6291456 + (8192*256) }, 15);
        Map<String, Object> json;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("lines.json")) {
            ObjectMapper om = new ObjectMapper();
            json = om.readValue(in, new TypeReference<HashMap<String, Object>>() {});
        }
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3067");
        SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(json, crs);

        SimpleFeatureCollection sfc = GeoJSONReader2.toFeatureCollection(json, schema);
        double[] bbox = grid.getTileExtent(new TileCoord(10, 459, 838));
        List<Geometry> mvtGeoms = SimpleFeaturesMVTEncoder.asMVTGeoms(sfc, bbox, 4096, 256);
        Assertions.assertEquals(284, mvtGeoms.size()); // wdtinc 3941bytes (~300ms) / no.ecc 32083bytes (~300ms)

        long start = System.currentTimeMillis();
        byte[] bytes = SimpleFeaturesMVTEncoder.encodeToByteArray(sfc, "test", bbox, 4096, 256);

        long duration = System.currentTimeMillis() - start;
        System.out.println(duration + "ms -> " + bytes.length);
        Assertions.assertEquals(32083, bytes.length, "Check size");
        Assertions.assertTrue(duration < 500, "Check time"); // Should be around ~300ms but CI might be slower

    }
}

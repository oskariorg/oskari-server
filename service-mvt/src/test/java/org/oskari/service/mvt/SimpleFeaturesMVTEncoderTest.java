package org.oskari.service.mvt;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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

}

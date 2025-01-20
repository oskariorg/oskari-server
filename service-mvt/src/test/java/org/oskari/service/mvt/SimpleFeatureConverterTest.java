package org.oskari.service.mvt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.stream.Collectors;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Test;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class SimpleFeatureConverterTest {

    @Test
    public void whenFeatureHasComplexFeaturesTheyGetStringifiedToJSON() {
        GeometryFactory gf = new GeometryFactory();
        Point p = gf.createPoint(new Coordinate(1.0, 2.0));

        double[] bbox = { 0, 0, 100, 100 };
        SimpleFeatureTypeBuilder tBuilder = new SimpleFeatureTypeBuilder();
        tBuilder.setName("test");
        tBuilder.add("geom", Point.class);
        tBuilder.add("myComplexProperty", Map.class);
        SimpleFeatureType featureType = tBuilder.buildFeatureType();
        SimpleFeatureBuilder fBuilder = new SimpleFeatureBuilder(featureType);

        List<String> words = Arrays.asList("foo", "bar", "baz");
        Map<String, Object> complexProperty = new HashMap<>();
        complexProperty.put("cool-words", words);

        fBuilder.set("geom", p);
        fBuilder.set("myComplexProperty", complexProperty);

        SimpleFeature f = fBuilder.buildFeature(null);

        DefaultFeatureCollection fc = new DefaultFeatureCollection("test");
        fc.add(f);

        List<Geometry> mvtGeoms = SimpleFeaturesMVTEncoder.asMVTGeoms(fc, bbox, 4096, 256);


        List<Feature> features = mvtGeoms.stream()
                .map(geom -> SimpleFeatureConverter.fromGeometry(geom))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Map<String, Object> props = features.get(0).properties;
        Object value = props.get("$myComplexProperty");
        assertTrue(value instanceof String);
        assertEquals("{'cool-words':['foo','bar','baz']}".replace('\'', '"'), value);
    }

}

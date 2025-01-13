package org.oskari.service.mvt;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Assertions.assertTrue(value instanceof String);
        Assertions.assertEquals("{'cool-words':['foo','bar','baz']}".replace('\'', '"'), value);
    }

}

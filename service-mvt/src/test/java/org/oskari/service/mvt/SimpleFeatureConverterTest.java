package org.oskari.service.mvt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IUserDataConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.JtsAdapter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

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

        MvtLayerProps layerProps = new MvtLayerProps();
        IUserDataConverter converter = new SimpleFeatureConverter();
        JtsAdapter.toFeatures(mvtGeoms, layerProps, converter);

        Iterator<String> keys = layerProps.getKeys().iterator();
        Iterator<Object> vals = layerProps.getVals().iterator();
        while (true) {
            if (!keys.hasNext() || !vals.hasNext()) {
                break;
            }
            String k = keys.next();
            Object v = vals.next();
            if (k.equals("$myComplexProperty")) {
                assertTrue(v instanceof String);
                assertEquals("{'cool-words':['foo','bar','baz']}".replace('\'', '"'), (String) v);
                return;
            }
        }
        fail();
    }

}

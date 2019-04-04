package org.oskari.control.userlayer;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.junit.Test;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.oskari.control.userlayer.UserLayerWFSHelper;
import org.oskari.service.wfs3.geojson.WFS3FeatureCollectionIterator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;

public class UserLayerWFSHelperTest {

    @Test
    public void testRetype() throws Exception {
        DefaultFeatureCollection original = new DefaultFeatureCollection(null, null);
        try (InputStream in = getClass().getResourceAsStream("geojson.json");
                Reader utf8Reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                SimpleFeatureIterator it = new WFS3FeatureCollectionIterator(utf8Reader)) {
            while (it.hasNext()) {
                original.add(it.next());
            }
        }

        SimpleFeature f = null;
        SimpleFeatureCollection retyped = new UserLayerWFSHelper().postProcess(original);
        try (SimpleFeatureIterator it = retyped.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                if ("vuser_layer_data.2970316".equals(feature.getID())) {
                    f = feature;
                    break;
                }
            }
        }

        assertEquals(5, f.getAttributeCount());
        Property geometry = f.getProperty(UserLayerWFSHelper.USERLAYER_ATTR_GEOMETRY);
        Property defaultGeometry = f.getDefaultGeometryProperty();
        Property interpoloi = f.getProperty("INTERPOLOI");
        Property lahdeainei = f.getProperty("LAHDEAINEI");
        Property id = f.getProperty("ID");
        Property laji = f.getProperty("LAJI");

        assertEquals(geometry.getName().getLocalPart(), defaultGeometry.getName().getLocalPart());
        assertEquals(Geometry.class, geometry.getType().getBinding());
        assertEquals(MultiLineString.class, geometry.getValue().getClass());

        assertEquals(Integer.class, interpoloi.getValue().getClass());
        assertEquals(1, interpoloi.getValue());

        assertEquals(String.class, lahdeainei.getValue().getClass());
        assertEquals("0", lahdeainei.getValue());

        assertEquals(Double.class, id.getValue().getClass());
        assertEquals(1.48627129E8, ((Double) id.getValue()).doubleValue(), 1e-8);

        assertEquals(String.class, laji.getValue().getClass());
        assertEquals("696", laji.getValue());
    }
}

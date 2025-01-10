package org.oskari.service.wfs3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.predicate.RectangleIntersects;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;

public class OskariWFS3ClientTest {

    @Disabled("Depends on outside service, results might vary")
    @Test
    public void testGetFeaturesBbox() throws ServiceException, IOException {
        OskariLayer layer = new OskariLayer();
        layer.setUrl("https://demo.ldproxy.net/daraa");
        layer.setName("AeronauticCrv");

        CoordinateReferenceSystem crs = OskariWFS3Client.getCRS84();
        ReferencedEnvelope bbox = new ReferencedEnvelope(36.425, 32.713, 36.426, 32.714, crs);
        SimpleFeatureCollection sfc;

        // This is a bit confusing 
        // If we provide non-null Filter as the 4th parameter
        // the bbox should be ignored -- this is similiar to WFS 1.1/2 clients where 
        // &bbox=<...> can be used if no other filter is provided, otherwise everything
        // has to be packed into a single &filter=<...>

        sfc = OskariWFS3Client.getFeatures(layer, null, crs, Filter.INCLUDE);
        Assertions.assertEquals(20, sfc.size(), "Expect all twenty features if no bbox filter");
        
        sfc = OskariWFS3Client.getFeatures(layer, bbox, crs, null);
        Assertions.assertEquals(3, sfc.size(), "Expect three features to hit our bbox");
    }

    @Disabled("Depends on outside service, results might vary")
    @Test
    public void testGetFeaturesWebMercator() throws Exception {
        OskariLayer layer = new OskariLayer();
        layer.setUrl("https://demo.ldproxy.net/daraa");
        layer.setName("AeronauticCrv");
        layer.setCapabilities(JSONHelper.createJSONObject("crs-uri", new JSONArray(Arrays.asList("http://www.opengis.net/def/crs/EPSG/0/3857"))));
        CoordinateReferenceSystem webmerc = CRS.decode("EPSG:3857");

        ReferencedEnvelope bbox = new ReferencedEnvelope(4054812.45, 3857271.18, 4054923.77, 3857403.49, webmerc);
        RectangleIntersects ri = new RectangleIntersects(JTS.toGeometry(bbox));

        SimpleFeatureCollection sfc = OskariWFS3Client.getFeatures(layer, bbox, webmerc, null);
        Assertions.assertEquals(3, sfc.size(), "Expect three features to hit our bbox");
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                Geometry g = (Geometry) f.getDefaultGeometry();
                Assertions.assertTrue(ri.intersects(g));
            }
        }
    }

}

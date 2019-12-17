package org.oskari.service.wfs3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;

public class OskariWFS3ClientTest {

    @Ignore("Depends on outside service, results might vary")
    @Test
    public void testGetFeaturesHardLimit() throws ServiceException, IOException {
        OskariLayer layer = new OskariLayer();
        layer.setUrl("https://beta-paikkatieto.maanmittauslaitos.fi/geographic-names/wfs3/v1/");
        layer.setName("places");
        CoordinateReferenceSystem crs = OskariWFS3Client.getCRS84();
        Envelope envelope = new Envelope(21.35, 21.40, 61.35, 61.40);
        ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, crs);

        int limit = 10;
        SimpleFeatureCollection sfc = OskariWFS3Client.getFeatures(layer, bbox, crs);
        assertEquals(limit, sfc.size());
        int i = 0;
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                i++;
                it.next();
            }
        }
        assertEquals(limit, i);

        limit = 1000;
        sfc = OskariWFS3Client.getFeatures(layer, bbox, crs);
        assertEquals(29, sfc.size());
        i = 0;
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                i++;
                it.next();
            }
        }
        assertEquals(29, i);
    }

    @Ignore("Depends on outside service, results might vary")
    @Test
    public void testGetFeaturesHardLimitPaging() throws ServiceException, IOException {
        OskariLayer layer = new OskariLayer();
        layer.setUrl("https://beta-paikkatieto.maanmittauslaitos.fi/geographic-names/wfs3/v1/");
        layer.setName("places");
        CoordinateReferenceSystem crs = OskariWFS3Client.getCRS84();
        int limit = OskariWFS3Client.PAGE_SIZE + 10;
        SimpleFeatureCollection sfc = OskariWFS3Client.getFeatures(layer, null, crs);
        assertEquals(limit, sfc.size());
        int i = 0;
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                i++;
                it.next();
            }
        }
        assertEquals(limit, i);
    }

    @Ignore("Depends on outside service, results might vary")
    @Test
    public void testGetFeaturesServiceSupportingCRS_EPSG3067() throws Exception {
        OskariLayer layer = new OskariLayer();
        layer.setUrl("https://beta-paikkatieto.maanmittauslaitos.fi/maastotiedot/wfs3/v1/");
        layer.setName("rakennus");
        layer.setCapabilities(JSONHelper.createJSONObject("crs-uri", new JSONArray(Arrays.asList("http://www.opengis.net/def/crs/EPSG/0/3067"))));
        CoordinateReferenceSystem epsg3067 = CRS.decode("EPSG:3067");
        ReferencedEnvelope bbox = new ReferencedEnvelope(500000, 501000, 6822000, 6823000, epsg3067);
        SimpleFeatureCollection sfc = OskariWFS3Client.getFeatures(layer, bbox, epsg3067);
        assertFalse(sfc.isEmpty());
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                Geometry geometryEnvelope = ((Geometry) it.next().getDefaultGeometry()).getEnvelope();
                assertTrue(geometryEnvelope.within(JTS.toGeometry(bbox)));
            }
        }
    }

}

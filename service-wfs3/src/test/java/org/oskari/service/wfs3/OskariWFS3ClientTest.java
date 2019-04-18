package org.oskari.service.wfs3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.service.ServiceException;

public class OskariWFS3ClientTest {

    @Ignore("Depends on outside service, results might vary")
    @Test
    public void testGetFeaturesHardLimit() throws ServiceException, IOException {
        String endPoint = "https://dev-paikkatieto.maanmittauslaitos.fi/geographic-names/wfs3/v1/";
        String user = null;
        String pass = null;
        String collectionId = "places";
        CoordinateReferenceSystem crs = OskariWFS3Client.getCRS84();
        Envelope envelope = new Envelope(21.35, 21.40, 61.35, 61.40);
        ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, crs);

        int limit = 10;
        SimpleFeatureCollection sfc = OskariWFS3Client.getFeatures(endPoint, user, pass, collectionId, bbox, crs, limit);
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
        sfc = OskariWFS3Client.getFeatures(endPoint, user, pass, collectionId, bbox, crs, limit);
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
        String endPoint = "https://dev-paikkatieto.maanmittauslaitos.fi/geographic-names/wfs3/v1/";
        String user = null;
        String pass = null;
        String collectionId = "places";
        CoordinateReferenceSystem crs = OskariWFS3Client.getCRS84();
        int limit = OskariWFS3Client.PAGE_SIZE + 10;
        SimpleFeatureCollection sfc = OskariWFS3Client.getFeatures(endPoint, user, pass, collectionId, null, crs, limit);
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
    public void testGetFeaturesTransformingToEPSG3067() throws Exception {
        String endPoint = "https://dev-paikkatieto.maanmittauslaitos.fi/geographic-names/wfs3/v1/";
        String user = null;
        String pass = null;
        String collectionId = "places";
        CoordinateReferenceSystem crs84 = OskariWFS3Client.getCRS84();
        Envelope envelope = new Envelope(21.35, 21.40, 61.35, 61.40);
        ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, crs84);
        CoordinateReferenceSystem epsg3067 = CRS.decode("EPSG:3067");
        MathTransform transform = CRS.findMathTransform(crs84, epsg3067);
        Envelope transformedEnvelope = JTS.transform(bbox, transform);
        Geometry transformedEnvelopeGeom = JTS.toGeometry(transformedEnvelope);
        int limit = 10;
        SimpleFeatureCollection sfc = OskariWFS3Client.getFeatures(endPoint, user, pass, collectionId, bbox, epsg3067, limit);
        try (SimpleFeatureIterator it = sfc.features()) {
            while (it.hasNext()) {
                Geometry geometryEnvelope = ((Geometry) it.next().getDefaultGeometry()).getEnvelope();
                assertTrue(geometryEnvelope.within(transformedEnvelopeGeom));
            }
        }
    }

}

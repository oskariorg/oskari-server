package org.oskari.service.wfs3.client;

import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.service.ServiceException;

public class OskariWFS3ClientTest {
    
    @Test
    public void testGetFeatures() throws ServiceException, IOException {
        String endPoint = "https://beta-paikkatieto.maanmittauslaitos.fi/geographic-names/wfs3/v1/";
        String user = null;
        String pass = null;
        String collectionId = "places";
        CoordinateReferenceSystem crs = OskariWFS3Client.getCRS84();
        Envelope envelope = new Envelope(21.35, 21.40, 61.35, 61.40);
        ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, crs);
        int limit = 10;
        SimpleFeatureCollection sfc = OskariWFS3Client.getFeatures(endPoint, user, pass, collectionId, bbox, crs, limit);
        try (SimpleFeatureIterator it = sfc.features()) {
            int i = 0;
            while (it.hasNext()) {
                i++;
                SimpleFeature f = it.next();
                System.out.println(f);
            }
            System.out.println(i);
        }
    }

}

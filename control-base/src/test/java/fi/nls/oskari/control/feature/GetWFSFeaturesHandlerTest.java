package fi.nls.oskari.control.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.service.wfs.client.OskariWFS110Client;

import com.vividsolutions.jts.geom.Envelope;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.layer.PermissionHelper;
import fi.nls.oskari.domain.map.OskariLayer;

public class GetWFSFeaturesHandlerTest {

    private GetWFSFeaturesHandler handler;

    @Before
    public void init() {
        PermissionHelper permissionHelper = new PermissionHelper(null, null);
        handler = new GetWFSFeaturesHandler();
        handler.setPermissionHelper(permissionHelper);
        handler.setWFSClient(new OskariWFS110Client());
        handler.init();
    }

    @Test
    @Ignore("Depends on an outside resource")
    public void testGetFeatures() throws ActionException, NoSuchAuthorityCodeException, FactoryException {
        OskariLayer layer = new OskariLayer();
        layer.setType(OskariLayer.TYPE_WFS);
        layer.setUrl("https://geo.stat.fi/geoserver/tilastointialueet/wfs");
        layer.setName("tilastointialueet:kunta1000k");
        CoordinateReferenceSystem webMercator = CRS.decode("EPSG:3857", true);
        CoordinateReferenceSystem nativeCRS = CRS.decode("EPSG:3067", true);
        Envelope envelope = new Envelope(2775356, 2875356, 8441866, 8541866);
        ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, webMercator);
        SimpleFeatureCollection sfc = handler.getFeatures(layer, bbox, nativeCRS, webMercator);
        CoordinateReferenceSystem actualCRS = sfc.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
        assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
    }

    @Test
    public void testBboxOutOfBounds() throws NoSuchAuthorityCodeException, FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
        try {
            handler.parseBbox("20040000,20040000,20041000,20041000", crs);
            fail();
        } catch (ActionParamsException e) {
            assertEquals(GetWFSFeaturesHandler.ERR_BBOX_OUT_OF_CRS, e.getMessage());
        }
    }

}

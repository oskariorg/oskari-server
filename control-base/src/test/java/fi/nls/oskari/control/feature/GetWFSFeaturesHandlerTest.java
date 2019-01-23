package fi.nls.oskari.control.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.geotools.data.simple.SimpleFeatureCollection;
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
    public void test() throws ActionException, NoSuchAuthorityCodeException, FactoryException {
        OskariLayer layer = new OskariLayer();
        layer.setType(OskariLayer.TYPE_WFS);
        layer.setUrl("https://geo.stat.fi/geoserver/tilastointialueet/wfs");
        layer.setName("tilastointialueet:kunta1000k");
        Envelope bbox3857 = new Envelope(2775356, 2875356, 8441866, 8541866);
        String targetSrsName = "EPSG:3857";
        String requestSrsName = "EPSG:3067";
        SimpleFeatureCollection sfc = handler.getFeatures(layer, bbox3857, requestSrsName, targetSrsName);
        CoordinateReferenceSystem crs = sfc.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
        assertTrue(CRS.equalsIgnoreMetadata(CRS.decode(targetSrsName, true), crs));
    }

    @Test
    public void testBboxOutOfBounds() throws ActionException {
        Envelope bbox3857 = new Envelope(20040000, 20040000, 20041000, 20041000);
        String targetSrsName = "EPSG:3857";
        String requestSrsName = "EPSG:4326";
        try {
            handler.getFeatures(null, bbox3857, requestSrsName, targetSrsName);
            fail();
        } catch (ActionParamsException e) {
            assertEquals(GetWFSFeaturesHandler.ERR_BBOX_OUT_OF_CRS, e.getMessage());
        }
    }

}

package fi.nls.oskari.control.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.wdtinc.mapbox_vector_tile.adapt.jts.MvtReader;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TagKeyValueMapConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsLayer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.model.JtsMvt;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.test.control.JSONActionRouteTest;

public class GetWFSVectorTileHandlerTest extends JSONActionRouteTest {

    private static final String END_POINT = "http://geoserver.hel.fi/geoserver/wfs";
    private static final String LAYER = "seutukartta:Kuntarajat";
    private static final String SRSNAME = "EPSG:3067";
    private static final String VERSION = "1.1.0";

    private static final String END_POINT2 = "http://tampere.navici.com/tampere_wfs_geoserver/ows";
    private static final String LAYER2 = "tampere_ora:KIINTEISTOT_ALUE";

    @Test
    public void foo() throws Exception {
        OskariLayer mock = Mockito.mock(OskariLayer.class);
        Mockito.when(mock.getType()).thenReturn(OskariLayer.TYPE_WFS);
        Mockito.when(mock.getUrl()).thenReturn(END_POINT);
        Mockito.when(mock.getName()).thenReturn(LAYER);
        Mockito.when(mock.getVersion()).thenReturn(VERSION);

        OskariLayer mock2 = Mockito.mock(OskariLayer.class);
        Mockito.when(mock2.getType()).thenReturn(OskariLayer.TYPE_WFS);
        Mockito.when(mock2.getUrl()).thenReturn(END_POINT2);
        Mockito.when(mock2.getName()).thenReturn(LAYER2);
        Mockito.when(mock2.getVersion()).thenReturn(VERSION);
        Mockito.when(mock2.getUsername()).thenReturn("pti_selaaja");
        Mockito.when(mock2.getPassword()).thenReturn("ajaales_itp");

        OskariLayerService layerServiceMock = Mockito.mock(OskariLayerService.class);
        Mockito.when(layerServiceMock.find(1)).thenReturn(mock);
        Mockito.when(layerServiceMock.find(2)).thenReturn(mock2);

        Map<String, String> params = new HashMap<>();
        params.put(ActionConstants.PARAM_ID, "2");
        params.put(ActionConstants.PARAM_SRS, SRSNAME);
        params.put("z", "8");
        params.put("x", "105");
        params.put("y", "65");

        ActionParameters ap = createActionParams(params);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ap.setResponse(mockHttpServletResponse(baos));

        GetWFSVectorTileHandler handler = new GetWFSVectorTileHandler();
        handler.setLayerService(layerServiceMock);

        handler.handleAction(ap);

        byte[] mvt = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(mvt);
        GeometryFactory geomFactory = new GeometryFactory();
        JtsMvt jtsMvt = MvtReader.loadMvt(bais, geomFactory, new TagKeyValueMapConverter());
        JtsLayer jtsLayer = jtsMvt.getLayer(LAYER2);
        assertNotNull(jtsLayer);
        Collection<Geometry> geometries = jtsLayer.getGeometries();
        assertEquals(864, geometries.size());

        long t0 = System.currentTimeMillis();
        handler.handleAction(ap);
        long t1 = System.currentTimeMillis();
        assertTrue("Response gets cached", (t1 - t0) < 100);
    }

}

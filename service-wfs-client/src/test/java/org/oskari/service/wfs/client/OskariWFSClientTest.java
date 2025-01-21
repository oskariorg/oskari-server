package org.oskari.service.wfs.client;

import fi.nls.oskari.domain.map.OskariLayer;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.geotools.api.filter.Filter;

import java.util.Optional;

public class OskariWFSClientTest {
    OskariWFSClient client = new OskariWFSClient();
    private static final String FILTER = "{\"filter\":{\"property\":{\"key\": \"foo\", \"value\": \"bar\"}}}";
    private static final String CAPABILITIES = "{\"geomName\":\"geomName\"}";
    @Test
    public void noFilter() throws Exception {
        OskariLayer layer = new OskariLayer();
        layer.setType(OskariLayer.TYPE_WFS);
        Filter filter = client.getWFSFilter(null, layer, null, Optional.empty());
        Assertions.assertNull(filter, "Layer without filter should get null filter");
    }
    @Test
    public void filter() throws Exception {
        OskariLayer layer = new OskariLayer();
        layer.setType(OskariLayer.TYPE_WFS);
        layer.setCapabilities(new JSONObject(CAPABILITIES));
        layer.setAttributes(new JSONObject(FILTER));
        ReferencedEnvelope bbox = new ReferencedEnvelope(0, 10, 0, 10, DefaultGeographicCRS.WGS84);
        Filter filter = client.getWFSFilter(null, layer, bbox, Optional.empty());
        Assertions.assertNotNull(filter, "Layer should get filter");
        Assertions.assertEquals("foo = 'bar' AND BBOX(geomName, 0.0,0.0,10.0,10.0)", CQL.toCQL(filter));
    }
}

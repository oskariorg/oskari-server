package fi.nls.oskari.control.feature;

import java.util.Optional;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import org.locationtech.jts.geom.Envelope;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GetWFSFeaturesHandlerTest {

    private GetWFSFeaturesHandler handler;

    @BeforeEach
    public void init() {
        handler = new GetWFSFeaturesHandler();
        handler.init();
    }

    @Test
    @Disabled("Depends on an outside resource")
    public void testGetFeatures() throws Exception {
        String id = "10";
        OskariLayer layer = new OskariLayer();
        layer.setId(Integer.parseInt(id));
        layer.setType(OskariLayer.TYPE_WFS);
        layer.setUrl("https://geo.stat.fi/geoserver/tilastointialueet/wfs");
        layer.setName("tilastointialueet:kunta1000k");
        CoordinateReferenceSystem webMercator = CRS.decode("EPSG:3857", true);
        PropertyUtil.addProperty("oskari.native.srs", "EPSG:3067", true);
        Envelope envelope = new Envelope(2775356, 2875356, 8441866, 8541866);
        ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, webMercator);

        SimpleFeatureCollection sfc = handler.featureClient.getFeatures(id, layer, bbox, webMercator, Optional.empty());

        CoordinateReferenceSystem actualCRS = sfc.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
        Assertions.assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
    }

}

package fi.nls.oskari.map.myplaces.service;

import fi.nls.test.util.ResourceHelper;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import org.custommonkey.xmlunit.Diff;

import static org.junit.Assert.*;

/**
 * Created by SMAKINEN on 4.5.2017.
 */
public class GeoServerProxyServiceTest {

    @BeforeClass
    public static void setUp() {
        // use relaxed comparison settings
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreAttributeOrder(true);

    }
    @Test
    public void testBuildQueryToStream() throws Exception {
        GeoServerProxyService service = new GeoServerProxyService();
        final String lon = "4551585.429555906";
        final String lat = "3273782.647749871";
        final int zoom = 11;
        final String layerId = "67";
        final String uuid = "abc-123";
        final String srs = "EPSG:3035";

        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        service.buildQueryToStream(GeoServerProxyService.MY_PLACE_FEATURE_FILTER_XML, lon + " " + lat, zoom, layerId, uuid, outs, srs);
        String xml = outs.toString();
        outs.close();
        String expectedResult = ResourceHelper.readStringResource("GetFeatureInfoMyPlaces-expected.xml", this);
        Diff xmlDiff = new Diff(expectedResult, xml);
        assertTrue("Should get expected query for myplaces GFI " + xmlDiff, xmlDiff.similar());
    }
}
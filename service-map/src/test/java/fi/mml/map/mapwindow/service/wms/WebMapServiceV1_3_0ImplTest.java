package fi.mml.map.mapwindow.service.wms;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import fi.nls.oskari.util.IOHelper;

public class WebMapServiceV1_3_0ImplTest {

    private static final String INSPIRE_CP = "capabilities_cp_1_3_0.xml";
    private static final String CHLORO = "capabilities_chloro_1_3_0.xml";

    @Test
    public void testInspireCP()
            throws IOException, WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        WebMapServiceV1_3_0_Impl wms;
        wms = new WebMapServiceV1_3_0_Impl("http://unit.test/ing", readResource(INSPIRE_CP), "CP.CadastralBoundary");
        assertEquals("http://unit.test/ing", wms.getCapabilitiesUrl());
        assertEquals("1.3.0", wms.getVersion());
        assertEquals(false, wms.queryable);
        assertArrayEquals(new String[] {
                "EPSG:3035", "EPSG:3067", "EPSG:3857", "EPSG:4258", "CRS:84"
        }, wms.getCRSs());
        assertArrayEquals(new String[] {
                "text/plain", "application/vnd.ogc.gml", "text/xml",
                "application/vnd.ogc.gml/3.1.1",
                "text/xml; subtype=gml/3.1.1", "text/html", "application/json"
        }, wms.getFormats());
        assertEquals(0, wms.getTime().size());
    }

    @Test
    public void testChloro()
            throws IOException, WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        WebMapServiceV1_3_0_Impl wms;
        wms = new WebMapServiceV1_3_0_Impl("http://unit.test/ing",
                readResource(CHLORO), "arctic_sdi:Chlorophyll");
        assertEquals("http://unit.test/ing", wms.getCapabilitiesUrl());
        assertEquals("1.3.0", wms.getVersion());
        assertEquals(true, wms.queryable);
        assertArrayEquals(new String[] {
                "EPSG:102017", "EPSG:3408", "EPSG:3575", "EPSG:3857", "EPSG:4326", "CRS:84"
        }, wms.getCRSs());
        assertArrayEquals(new String[] {
                "text/plain", "application/vnd.ogc.gml",
                "application/vnd.ogc.gml/3.1.1",
                "text/html", "application/json"
        }, wms.getFormats());
        List<String> times = wms.getTime();
        assertEquals(138, times.size());
        for (String time : times) {
            ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        }
        assertTrue("Parsing was succesful, no exception was thrown", true);
    }

    @Test
    public void testReadingOnlyAllowedCRS()
            throws IOException, WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        Set<String> allowed = new HashSet<>();
        allowed.add("EPSG:3408");
        allowed.add("EPSG:3857");
        WebMapServiceV1_3_0_Impl wms;
        wms = new WebMapServiceV1_3_0_Impl("http://unit.test/ing",
                readResource(CHLORO), "arctic_sdi:Chlorophyll", allowed);
        assertArrayEquals(new String[] { "EPSG:3408", "EPSG:3857" }, wms.getCRSs());
    }

    private String readResource(String p) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(p)) {
            return new String(IOHelper.readBytes(in), StandardCharsets.UTF_8);
        }
    }

}

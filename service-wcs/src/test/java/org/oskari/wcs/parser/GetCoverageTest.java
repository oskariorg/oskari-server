package org.oskari.wcs.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.utils.xml.XML;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.request.GetCoverage;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class GetCoverageTest {

    @Test
    public void testGetCoverage() throws ParserConfigurationException, SAXException, IOException {
        Document doc;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("capabilities.xml")) {
            doc = XML.readDocument(in);
        }
        Capabilities wcs = CapabilitiesParser.parse(doc);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("coveragedescriptions.xml")) {
            doc = XML.readDocument(in);
        }
        List<CoverageDescription> coverageDescriptions = CoverageDescriptionsParser.parse(doc);
        CoverageDescription desc = coverageDescriptions.get(0);

        Map<String, String[]> params = new GetCoverage(wcs, desc)
        .subset("E", 500000.0, 501000.0)
        .subset("N", 6822000.0, 6823000.0)
        .toKVP();

        Assertions.assertEquals(6, params.keySet().size());
        Assertions.assertEquals("WCS", params.get("service")[0]);
        Assertions.assertEquals("2.0.1", params.get("version")[0]);
        Assertions.assertEquals("GetCoverage", params.get("request")[0]);
        Assertions.assertEquals("korkeusmalli_10m__korkeusmalli_10m", params.get("coverageId")[0]);
        Assertions.assertEquals("image/tiff", params.get("format")[0]);
        Assertions.assertEquals("E(500000.000000,501000.000000)", params.get("subset")[0]);
        Assertions.assertEquals("N(6822000.000000,6823000.000000)", params.get("subset")[1]);
    }

}

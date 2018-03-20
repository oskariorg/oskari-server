package org.oskari.wcs.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.oskari.utils.xml.XML;
import org.oskari.wcs.WCS;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.gml.Envelope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class CoverageDescriptionsParser {

    public static List<CoverageDescription> parse(URL url)
            throws IOException, ParserConfigurationException, SAXException {
        try (InputStream in = url.openStream()) {
            return parse(in);
        }
    }

    public static List<CoverageDescription> parse(InputStream in)
            throws ParserConfigurationException, SAXException, IOException {
        return parse(XML.readDocument(in));
    }

    public static List<CoverageDescription> parse(Document doc)
            throws IllegalArgumentException {
        Element root = doc.getDocumentElement();
        if (!"CoverageDescriptions".equals(root.getLocalName())) {
            throw new IllegalArgumentException("Invalid root element name: "
                    + root.getLocalName());
        }
        if (!WCS.NS_WCS.equals(root.getNamespaceURI())) {
            throw new IllegalArgumentException("Invalid XML root namespace: "
                    + root.getNamespaceURI());
        }

        List<CoverageDescription> coverageDescriptions = new ArrayList<>();
        List<Element> children = XML.getChildren(root);
        for (Element child : children) {
            if ("CoverageDescription".equals(child.getLocalName())) {
                coverageDescriptions.add(parseCoverageDescription(child));
            }
        }

        return coverageDescriptions;
    }

    private static CoverageDescription parseCoverageDescription(Element coverageDescription) {
        String coverageId = XML.getChildText(coverageDescription, "CoverageId")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find CoverageId from CoverageDescription"));

        Element boundedByE = XML.getChild(coverageDescription, "boundedBy")
                .orElseThrow(() -> new IllegalArgumentException(
                        "The coverage element of every OfferedCoverage shall contain a valid gml:boundedBy element"));
        Element envelope = XML.getChild(boundedByE, "Envelope")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find Envelope from boundedBy"));
        Envelope boundedBy = CommonParser.parseEnvelope(envelope);

        Element serviceParameters = XML.getChild(coverageDescription, "ServiceParameters")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find ServiceParameters from CoverageDescription"));
        String coverageSubtype = XML.getChildValue(serviceParameters, "CoverageSubtype");
        String nativeFormat = XML.getChildValue(serviceParameters, "nativeFormat");

        switch (coverageSubtype) {
        case "RectifiedGridCoverage":
            return RectifiedGridCoverageParser.parse(
                    coverageDescription, coverageId, boundedBy, nativeFormat);
        default:
            throw new UnsupportedOperationException(
                    "Unable to parse coverage of subtype " + coverageSubtype);
        }
    }

}

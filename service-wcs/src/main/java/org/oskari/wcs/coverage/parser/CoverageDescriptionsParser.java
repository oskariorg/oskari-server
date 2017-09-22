package org.oskari.wcs.coverage.parser;

import org.oskari.wcs.gml.Envelope;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.util.CommonParser;
import org.oskari.wcs.util.WCS;
import org.oskari.wcs.util.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class CoverageDescriptionsParser {

    public static List<CoverageDescription> parse(URL url) throws IOException,
            ParserConfigurationException, SAXException {
        try (InputStream in = url.openStream()) {
            return parse(in);
        }
    }

    public static List<CoverageDescription> parse(InputStream in)
            throws ParserConfigurationException, SAXException, IOException {
        return parse(XML.readDocument(in));
    }

    public static List<CoverageDescription> parse(Document doc) throws IllegalArgumentException {
        Element root = doc.getDocumentElement();
        if (!"CoverageDescriptions".equals(root.getLocalName())) {
            throw new IllegalArgumentException("Invalid root element name: " + root.getLocalName());
        }
        if (!WCS.NS.equals(root.getNamespaceURI())) {
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
        String coverageId = XML.getChildText(coverageDescription, "CoverageId").orElseThrow(
                () -> new IllegalArgumentException(
                        "Could not find CoverageId from CoverageDescription"));

        Optional<Element> boundedByE = XML.getChild(coverageDescription, "boundedBy");
        Envelope boundedBy = null;
        if (boundedByE.isPresent()) {
            Element envelope = XML.getChild(boundedByE.get(), "Envelope").orElseThrow(
                    () -> new IllegalArgumentException("Could not find Envelope from boundedBy"));
            boundedBy = CommonParser.parseEnvelope(envelope);
        }

        Element serviceParameters = XML.getChild(coverageDescription, "ServiceParameters")
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Could not find ServiceParameters from CoverageDescription"));
        String coverageSubtype = XML.getChildValue(serviceParameters, "CoverageSubtype");
        String nativeFormat = XML.getChildValue(serviceParameters, "nativeFormat");

        switch (coverageSubtype) {
        case "RectifiedGridCoverage":
            return RectifiedGridCoverageParser.parse(coverageDescription, coverageId, boundedBy,
                    nativeFormat);
        default:
            throw new UnsupportedOperationException("Unable to parse coverage of subtype "
                    + coverageSubtype);
        }
    }

}

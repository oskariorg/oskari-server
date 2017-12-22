package org.oskari.wcs.parser;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import org.oskari.utils.xml.XML;
import org.oskari.wcs.WCS;
import org.oskari.wcs.capabilities.BoundingBox;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.capabilities.Contents;
import org.oskari.wcs.capabilities.CoverageSummary;
import org.oskari.wcs.capabilities.Operation;
import org.oskari.wcs.capabilities.OperationsMetadata;
import org.oskari.wcs.capabilities.ServiceIdentification;
import org.oskari.wcs.capabilities.ServiceMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class CapabilitiesParser {

    public static Capabilities parse(URL url)
            throws IOException, ParserConfigurationException, SAXException {
        try (InputStream in = url.openStream()) {
            return parse(in);
        }
    }

    public static Capabilities parse(InputStream in)
            throws ParserConfigurationException, SAXException, IOException {
        return parse(XML.readDocument(in));
    }

    public static Capabilities parse(Document doc) throws IllegalArgumentException {
        Element root = doc.getDocumentElement();
        if (!"Capabilities".equals(root.getLocalName())) {
            throw new IllegalArgumentException("Invalid root element name: "
                    + root.getLocalName());
        }
        if (!WCS.NS_WCS.equals(root.getNamespaceURI())) {
            throw new IllegalArgumentException("Invalid XML root namespace: "
                    + root.getNamespaceURI());
        }
        if (!WCS.VERSION_201.equals(root.getAttribute("version"))) {
            throw new IllegalArgumentException("Invalid attribute 'version': "
                    + root.getAttribute("version"));
        }

        String updateSequence = root.getAttribute("updateSequence");
        ServiceIdentification serviceIdentification = null;
        OperationsMetadata operationsMetadata = null;
        ServiceMetadata serviceMetadata = null;
        Contents contents = null;

        List<Element> children = XML.getChildren(root);
        for (Element child : children) {
            switch (child.getLocalName()) {
            case "ServiceIdentification":
                serviceIdentification = parseServiceIdentification(child);
                break;
            case "OperationsMetadata":
                operationsMetadata = parseOperationsMetadata(child);
                break;
            case "ServiceMetadata":
                serviceMetadata = parseServiceMetadata(child);
                break;
            case "Contents":
                contents = parseContents(child);
                break;
            }
        }

        return new Capabilities(updateSequence, serviceIdentification, operationsMetadata,
                serviceMetadata, contents);
    }

    private static ServiceIdentification parseServiceIdentification(Element e)
            throws IllegalArgumentException {
        String title = XML.getChildValue(e, "Title");
        String serviceType = XML.getChildValue(e, "ServiceType");
        List<String> serviceTypeVersion = XML.getChildrenValues(e, "ServiceTypeVersion");
        List<String> profile = XML.getChildrenValues(e, "Profile");
        return new ServiceIdentification(title, serviceType, serviceTypeVersion, profile);
    }

    private static OperationsMetadata parseOperationsMetadata(Element e)
            throws IllegalArgumentException {
        List<Operation> operations = new ArrayList<>();
        for (Element op : XML.getChildren(e, "Operation")) {
            operations.add(parseOperation(op));
        }
        return new OperationsMetadata(operations);
    }

    private static Operation parseOperation(Element op) {
        String name = op.getAttribute("name");
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Operation did not specify name attribute");
        }
        String get = null;
        String post = null;
        for (Element dcp : XML.getChildren(op, "DCP")) {
            Element http = XML.getChild(dcp, "HTTP").orElseThrow(
                    () -> new IllegalArgumentException("Could not find HTTP inside DCP"));
            Optional<Element> tmp = XML.getChild(http, "Get");
            if (tmp.isPresent()) {
                get = tmp.get().getAttribute("xlink:href");
            } else {
                tmp = XML.getChild(http, "Post");
                if (tmp.isPresent()) {
                    post = tmp.get().getAttribute("xlink:href");
                }
            }
        }
        if (get == null && post == null) {
            throw new IllegalArgumentException("Operation did not specify either GET or POST encoding");
        }
        return new Operation(name, Optional.ofNullable(get), Optional.ofNullable(post));
    }

    private static ServiceMetadata parseServiceMetadata(Element serviceMetadata)
            throws IllegalArgumentException {
        List<String> formatSupported = XML.getChildrenValues(serviceMetadata, "formatSupported");

        Optional<Element> extension = XML.getChild(serviceMetadata, "Extension");
        if (!extension.isPresent()) {
            return new ServiceMetadata(formatSupported, null);
        }

        List<Element> exts = XML.getChildren(extension.get());
        Map<String, List<String>> extensionsByNsLocalName = exts.stream().collect(
                groupingBy(e -> e.getLocalName(),
                        mapping(Element::getTextContent, toList())));

        return new ServiceMetadata(formatSupported, extensionsByNsLocalName);
    }

    private static Contents parseContents(Element e) throws IllegalArgumentException {
        List<Element> coverageSummaries = XML.getChildren(e, "CoverageSummary");
        if (coverageSummaries.isEmpty()) {
            throw new IllegalArgumentException("Zero coverage summaries!");
        }

        List<CoverageSummary> summaries = new ArrayList<>(coverageSummaries.size());
        for (Element coverageSummary : coverageSummaries) {
            summaries.add(parseCoverageSummary(coverageSummary));
        }

        return new Contents(summaries);
    }

    private static CoverageSummary parseCoverageSummary(Element coverageSummary) {
        String coverageId = XML.getChildValue(coverageSummary, "CoverageId");
        String coverageSubtype = XML.getChildValue(coverageSummary, "CoverageSubtype");

        Optional<Element> wgs84 = XML.getChild(coverageSummary, "WGS84BoundingBox");
        BoundingBox wgs84bbox = wgs84.isPresent() ? parseBoundingBbox(wgs84.get()) : null;

        List<Element> e_otherBbox = XML.getChildren(coverageSummary, "BoundingBox");
        List<BoundingBox> otherBbox = null;
        if (!e_otherBbox.isEmpty()) {
            otherBbox = new ArrayList<>(e_otherBbox.size());
            for (Element e : e_otherBbox) {
                otherBbox.add(parseBoundingBbox(e));
            }
        }

        return new CoverageSummary(coverageId, coverageSubtype, wgs84bbox, otherBbox);
    }

    private static BoundingBox parseBoundingBbox(Element e) {
        String crs = e.getAttribute("crs");
        String lowerCorner = XML.getChildValue(e, "LowerCorner");
        int i = lowerCorner.indexOf(' ');
        double lowerCornerLon = Double.parseDouble(lowerCorner.substring(0, i));
        double lowerCornerLat = Double.parseDouble(lowerCorner.substring(i + 1));

        String upperCorner = XML.getChildValue(e, "UpperCorner");
        i = upperCorner.indexOf(' ');
        double upperCornerLon = Double.parseDouble(upperCorner.substring(0, i));
        double upperCornerLat = Double.parseDouble(upperCorner.substring(i + 1));

        return new BoundingBox(crs, lowerCornerLon, lowerCornerLat, upperCornerLon, upperCornerLat);
    }

}

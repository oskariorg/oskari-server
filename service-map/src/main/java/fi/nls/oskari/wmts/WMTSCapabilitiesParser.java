package fi.nls.oskari.wmts;

import fi.nls.oskari.wmts.domain.TileMatrixLink;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.oskari.wms.WMSStyle;
import fi.nls.oskari.wmts.domain.ResourceUrl;
import fi.nls.oskari.wmts.domain.TileMatrix;
import fi.nls.oskari.wmts.domain.TileMatrixLimits;
import fi.nls.oskari.wmts.domain.TileMatrixSet;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class WMTSCapabilitiesParser {

    private static final String KEY_LAYERS_WITH_REMARKS = "layersWithRemarks";

    public static WMTSCapabilities parseCapabilities(String xml)
            throws IllegalArgumentException, XMLStreamException {
        OMElement doc = XmlHelper.parseXML(xml);
        if (doc == null) {
            throw new XMLStreamException("Failed to parse XML");
        }
        return parseCapabilities(doc);
    }

    public static WMTSCapabilities parseCapabilities(InputStream in)
            throws IllegalArgumentException, XMLStreamException {
        OMElement doc = OMXMLBuilderFactory.createOMBuilder(in).getDocumentElement();
        return parseCapabilities(doc);
    }

    public static WMTSCapabilities parseCapabilities(OMElement doc)
            throws IllegalArgumentException, XMLStreamException {
        OMElement contents = XmlHelper.getChild(doc, "Contents");

        Map<String, TileMatrixSet> tileMatrixSets = parseTileMatrixSets(contents);
        Map<String, WMTSCapabilitiesLayer> layers = parseLayers(contents, tileMatrixSets);

        return new WMTSCapabilities(tileMatrixSets, layers);
    }

    private static Map<String, TileMatrixSet> parseTileMatrixSets(OMElement contents)
            throws IllegalArgumentException, XMLStreamException {
        Map<String, TileMatrixSet> tileMatrixSets = new HashMap<>();

        Iterator<OMElement> it = contents.getChildrenWithLocalName("TileMatrixSet");
        while (it.hasNext()) {
            TileMatrixSet tms = parseTileMatrixSet(it.next());
            tileMatrixSets.put(tms.getId(), tms);
        }

        return tileMatrixSets;
    }

    /**
     * Parses a single <element name="TileMatrixSet"> into a POJO
     * @see http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd
     * @param tileMatrixSet XML element describing the <TileMatrixSet>
     * @return parsed TileMatrixSet
     * @throws XMLStreamException if
     * @throws IllegalArgumentException
     */
    private static TileMatrixSet parseTileMatrixSet(OMElement tileMatrixSet)
            throws XMLStreamException, IllegalArgumentException {
        String identifier = XmlHelper.getChildValue(tileMatrixSet, "Identifier");
        String crs = XmlHelper.getChildValue(tileMatrixSet, "SupportedCRS");

        // Keep a Set of TileMatrix id's we've encountered so far - don't allow duplicates
        Set<String> ids = new HashSet<>();
        List<TileMatrix> tileMatrises = new ArrayList<>();

        Iterator<OMElement> it = tileMatrixSet.getChildrenWithLocalName("TileMatrix");
        while (it.hasNext()) {
            OMElement e = it.next();
            String id = XmlHelper.getChildValue(e, "Identifier");
            if (ids.contains(id)) {
                throw new IllegalArgumentException("TileMatrix with id: " + id
                        + " is specified multiple times!");
            }
            double scaleDenominator = Double.parseDouble(XmlHelper.getChildValue(e, "ScaleDenominator"));
            double[] topLeftCorner = parseTopLeftCorner(XmlHelper.getChildValue(e, "TopLeftCorner"));
            int tileWidth = Integer.parseInt(XmlHelper.getChildValue(e, "TileWidth"));
            int tileHeight = Integer.parseInt(XmlHelper.getChildValue(e, "TileHeight"));
            int matrixWidth = Integer.parseInt(XmlHelper.getChildValue(e, "MatrixWidth"));
            int matrixHeight = Integer.parseInt(XmlHelper.getChildValue(e, "MatrixHeight"));
            TileMatrix tm = new TileMatrix(id, scaleDenominator, topLeftCorner,
                    tileWidth, tileHeight, matrixWidth, matrixHeight);
            tileMatrises.add(tm);
            ids.add(id);
        }

        return new TileMatrixSet(identifier, crs, tileMatrises);
    }

    private static double[] parseTopLeftCorner(String topLeftCorner) {
        if (topLeftCorner == null) {
            return null;
        }
        int i = topLeftCorner.indexOf(' ');
        if (i < 0) {
            return null;
        }
        return new double[] {
                Double.parseDouble(topLeftCorner.substring(0, i)),
                Double.parseDouble(topLeftCorner.substring(i + 1))
        };
    }

    private static Map<String, WMTSCapabilitiesLayer> parseLayers(OMElement contents,
            Map<String, TileMatrixSet> tileMatrixSets) throws XMLStreamException {
        Map<String, WMTSCapabilitiesLayer> layers = new HashMap<>();

        Iterator<OMElement> it = contents.getChildrenWithLocalName("Layer");
        while (it.hasNext()) {
            WMTSCapabilitiesLayer l = parseLayer(it.next(), tileMatrixSets);
            layers.put(l.getId(), l);
        }

        return layers;
    }

    private static WMTSCapabilitiesLayer parseLayer(OMElement layer,
            Map<String, TileMatrixSet> tileMatrixSets) throws XMLStreamException {
        String identifier = XmlHelper.getChildValue(layer, "Identifier");
        String title = XmlHelper.getChildValue(layer, "Title");
        List<WMSStyle> styles = parseStyles(layer);
        String defaultStyle = parseDefaultStyle(layer);
        Set<String> formats = getTexts(layer, "Format");
        Set<String> infoFormats = getTexts(layer, "InfoFormat");
        List<ResourceUrl> resourceURLs = parseResourceURLs(layer);
        List<TileMatrixLink> links = parseLinks(layer, tileMatrixSets);
        return new WMTSCapabilitiesLayer(identifier, title, styles, defaultStyle,
                formats, infoFormats, resourceURLs, links);
    }

    private static List<WMSStyle> parseStyles(OMElement layer) throws XMLStreamException {
        List<WMSStyle> styles = new ArrayList<>();

        Iterator<OMElement> it = layer.getChildrenWithLocalName("Style");
        while (it.hasNext()) {
            OMElement e = it.next();
            String identifier = XmlHelper.getChildValue(e, "Identifier");
            WMSStyle style = new WMSStyle();
            style.setTitle(identifier);
            style.setName(identifier);
            styles.add(style);
        }

        return styles;
    }

    private static String parseDefaultStyle(OMElement layer) throws XMLStreamException {
        Iterator<OMElement> it = layer.getChildrenWithLocalName("Style");
        while (it.hasNext()) {
            OMElement e = it.next();
            String identifier = XmlHelper.getChildValue(e, "Identifier");
            String defaultAttr = e.getAttributeValue(new QName("isDefault"));
            if (ConversionHelper.getBoolean(defaultAttr, false)) {
                return identifier;
            }
        }
        return null;
    }

    private static Set<String> getTexts(OMElement layer, String keyword) {
        Set<String> set = new HashSet<>();

        Iterator<OMElement> it = layer.getChildrenWithLocalName(keyword);
        while (it.hasNext()) {
            set.add(it.next().getText());
        }

        return set;
    }

    private static List<ResourceUrl> parseResourceURLs(OMElement layer) {
        List<ResourceUrl> resourceURLs = new ArrayList<>();

        Iterator<OMElement> it = layer.getChildrenWithLocalName("ResourceURL");
        while (it.hasNext()) {
            OMElement e = it.next();
            String format = e.getAttributeValue(new QName("format"));
            String resourceType = e.getAttributeValue(new QName("resourceType"));
            String template = e.getAttributeValue(new QName("template"));
            resourceURLs.add(new ResourceUrl(format, resourceType, template));
        }

        return resourceURLs;
    }

    private static List<TileMatrixLink> parseLinks(OMElement capabilitiesLayer,
            Map<String, TileMatrixSet> tileMatrixSets) throws XMLStreamException {
        List<TileMatrixLink> links = new ArrayList<>();

        Iterator<OMElement> it = capabilitiesLayer.getChildrenWithLocalName("TileMatrixSetLink");
        while (it.hasNext()) {
            OMElement e = it.next();
            String ref = XmlHelper.getChildValue(e, "TileMatrixSet");
            TileMatrixSet tms = tileMatrixSets.get(ref);
            if (tms == null) {
                throw new XMLStreamException("Referred TileMatrixSet " + ref
                        + " does not appear in this GetCapabilities response");
            }
            OMElement eTMSLimits = XmlHelper.getChild(e, "TileMatrixSetLimits");
            List<TileMatrixLimits> tileMatrixSetLimits = parseLimits(eTMSLimits, tms);
            links.add(new TileMatrixLink(tms, tileMatrixSetLimits));
        }

        return links;
    }

    private static List<TileMatrixLimits> parseLimits(OMElement eTMSLimits, TileMatrixSet tms)
            throws XMLStreamException, IllegalArgumentException {
        // <TileMatrixSetLimits> might not exist
        if (eTMSLimits == null) {
            return null;
        }

        List<TileMatrixLimits> limits = new ArrayList<>();

        Map<String, TileMatrix> tileMatrices = tms.getTileMatrixMap();

        Iterator<OMElement> it = eTMSLimits.getChildrenWithLocalName("TileMatrixLimits");
        while (it.hasNext()) {
            OMElement e = it.next();
            String ref = XmlHelper.getChildValue(e, "TileMatrix");
            TileMatrix tm = tileMatrices.get(ref);
            if (tm == null) {
                throw new XMLStreamException("Referred TileMatrix " + ref
                        + " does not appear in specified TileMatrixSet " + tms.getId());
            }
            int minTileRow = Integer.parseInt(XmlHelper.getChildValue(e, "MinTileRow"));
            int maxTileRow = Integer.parseInt(XmlHelper.getChildValue(e, "MaxTileRow"));
            int minTileCol = Integer.parseInt(XmlHelper.getChildValue(e, "MinTileCol"));
            int maxTileCol = Integer.parseInt(XmlHelper.getChildValue(e, "MaxTileCol"));
            limits.add(new TileMatrixLimits(tm, minTileRow, maxTileRow, minTileCol, maxTileCol));
        }

        return limits;
    }

    public static JSONObject asJSON(WMTSCapabilities caps, String url, String currentCrs) {
        // start building result
        JSONObject result = new JSONObject();

        JSONArray layersNode = new JSONArray();
        JSONHelper.putValue(result, "layers", layersNode);
        for (WMTSCapabilitiesLayer layer : caps.getLayers()) {
            JSONObject layerJson = layer.getAsJSON();
            String matrixsetid = getMatrixSetId(layer.getLinks(), currentCrs);
            if (matrixsetid == null) {
                JSONHelper.putValue(layerJson, "title", layer.getTitle() + "  *");
                JSONHelper.putValue(result, KEY_LAYERS_WITH_REMARKS, "true");
            } else {
                JSONHelper.putValue(layerJson, "tileMatrixSetId", matrixsetid);
            }
            if (!layerJson.has("url")) {
                JSONHelper.putValue(layerJson, "url", url);
            }
            layersNode.put(layerJson);

        }

        JSONObject matrixNode = new JSONObject();
        JSONHelper.putValue(result, "matrixSets", matrixNode);
        for (TileMatrixSet matrix : caps.getTileMatrixSets()) {
            JSONHelper.putValue(matrixNode, matrix.getId(), matrix.getAsJSON());
        }

        return result;
    }

    /**
     * Get tile matrix set id of current crs
     * @param caps
     * @param links
     * @param currentCrs
     * @return
     */
    private static String getMatrixSetId(List<TileMatrixLink> links, String currentCrs) {
        for (TileMatrixLink link : links) {
            TileMatrixSet tms = link.getTileMatrixSet();
            String tmsCrs = tms.getCrs();
            String epsg = ProjectionHelper.shortSyntaxEpsg(tmsCrs);
            if (currentCrs.equals(epsg)) {
                return tms.getId();
            }
        }
        return null;
    }

}

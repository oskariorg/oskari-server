package org.oskari.capabilities.ogc.wmts;

//import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.ogc.LayerStyle;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class WMTSCapabilitiesParserHelper {

    private static final Logger LOG = LogFactory.getLogger(WMTSCapabilitiesParserHelper.class);

    public static WMTSCapabilities parseCapabilities(String xml)
            throws IllegalArgumentException, XMLStreamException {
        Element doc = XmlHelper.parseXML(xml);
        if (doc == null) {
            throw new XMLStreamException("Failed to parse XML");
        }
        return parseCapabilities(doc);
    }

    public static WMTSCapabilities parseCapabilities(Element doc)
            throws IllegalArgumentException {
        Element contents = XmlHelper.getFirstChild(doc, "Contents");

        Map<String, TileMatrixSet> tileMatrixSets = parseTileMatrixSets(contents);
        Map<String, WMTSCapabilitiesLayer> layers = parseLayers(contents, tileMatrixSets);

        return new WMTSCapabilities(tileMatrixSets, layers);
    }

    private static Map<String, TileMatrixSet> parseTileMatrixSets(Element contents)
            throws IllegalArgumentException {
        Map<String, TileMatrixSet> tileMatrixSets = new HashMap<>();

        XmlHelper.getChildElements(contents, "TileMatrixSet")
                .map(node -> parseTileMatrixSet(node))
                .forEach(tms -> tileMatrixSets.put(tms.getId(), tms));

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
    private static TileMatrixSet parseTileMatrixSet(Element tileMatrixSet) {
        String identifier = XmlHelper.getChildValue(tileMatrixSet, "Identifier");
        String crs = XmlHelper.getChildValue(tileMatrixSet, "SupportedCRS");

        // Keep a Set of TileMatrix id's we've encountered so far - don't allow duplicates
        Set<String> ids = new HashSet<>();
        List<TileMatrix> tileMatrises = new ArrayList<>();

        XmlHelper.getChildElements(tileMatrixSet, "TileMatrix").forEach(e -> {
            String id = XmlHelper.getChildValue(e, "Identifier");
            if (ids.contains(id)) {
                LOG.error("TileMatrix with id:", id, "is specified multiple times!");
                return;
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
        });

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

    private static Map<String, WMTSCapabilitiesLayer> parseLayers(Element contents,
            Map<String, TileMatrixSet> tileMatrixSets) {
        Map<String, WMTSCapabilitiesLayer> layers = new HashMap<>();

        XmlHelper.getChildElements(contents, "Layer").forEach(e -> {
            WMTSCapabilitiesLayer l = parseLayer(e, tileMatrixSets);
            layers.put(l.getId(), l);
        });

        return layers;
    }

    private static WMTSCapabilitiesLayer parseLayer(Element layer,
            Map<String, TileMatrixSet> tileMatrixSets) {
        String identifier = XmlHelper.getChildValue(layer, "Identifier");
        String title = XmlHelper.getChildValue(layer, "Title");
        List<LayerStyle> styles = parseStyles(layer);
        String defaultStyle = styles.stream()
                .filter(LayerStyle::isDefault)
                .map(LayerStyle::getName)
                .findFirst()
                .orElse(null);
        //String defaultStyle = parseDefaultStyle(layer);
        Set<String> formats = getTexts(layer, "Format");
        Set<String> infoFormats = getTexts(layer, "InfoFormat");
        List<ResourceUrl> resourceURLs = parseResourceURLs(layer);
        List<TileMatrixLink> links = parseLinks(layer, tileMatrixSets);
        return new WMTSCapabilitiesLayer(identifier, title, styles, defaultStyle,
                formats, infoFormats, resourceURLs, links);
    }

    private static List<LayerStyle> parseStyles(Element layer) {
        List<LayerStyle> styles =  XmlHelper.getChildElements(layer, "Style")
                .map(e -> {
                    String identifier = XmlHelper.getChildValue(e, "Identifier");
                    LayerStyle style = new LayerStyle();
                    style.setTitle(identifier);
                    style.setName(identifier);
                    String isDefault = XmlHelper.getAttributesAsMap(e).getOrDefault("isDefault", "false");
                    style.setDefault(ConversionHelper.getBoolean(isDefault, false));
                    return style;
                })
                .collect(Collectors.toList());
        return styles;
    }


    private static Set<String> getTexts(Element layer, String keyword) {
        Set<String> texts =  XmlHelper.getChildElements(layer, keyword)
                .map(Element::getTextContent)
                .collect(Collectors.toSet());
        return texts;
    }

    private static List<ResourceUrl> parseResourceURLs(Element layer) {
        List<ResourceUrl> resourceURLs =  XmlHelper.getChildElements(layer, "ResourceURL")
                .map(e -> {
                    Map<String, String> attrs = XmlHelper.getAttributesAsMap(e);
                    return new ResourceUrl(attrs.get("format"), attrs.get("resourceType"), attrs.get("template"));
                })
                .collect(Collectors.toList());
        return resourceURLs;
    }

    private static List<TileMatrixLink> parseLinks(Element capabilitiesLayer,
            Map<String, TileMatrixSet> tileMatrixSets) {

        List<TileMatrixLink> links =  XmlHelper.getChildElements(capabilitiesLayer, "TileMatrixSetLink")
                .map(e -> {
                    String ref = XmlHelper.getChildValue(e, "TileMatrixSet");
                    TileMatrixSet tms = tileMatrixSets.get(ref);
                    if (tms == null) {
                        LOG.warn("Referred TileMatrixSet", ref, "does not appear in this GetCapabilities response");
                        return null;
                    }
                    Element eTMSLimits = XmlHelper.getFirstChild(e, "TileMatrixSetLimits");
                    List<TileMatrixLimits> tileMatrixSetLimits = parseLimits(eTMSLimits, tms);
                    return new TileMatrixLink(tms, tileMatrixSetLimits);

                })
                .filter(link -> link != null)
                .collect(Collectors.toList());

        return links;
    }

    private static List<TileMatrixLimits> parseLimits(Element eTMSLimits, TileMatrixSet tms)
            throws IllegalArgumentException {
        // <TileMatrixSetLimits> might not exist
        if (eTMSLimits == null) {
            return null;
        }

        List<TileMatrixLimits> limits =  XmlHelper.getChildElements(eTMSLimits, "TileMatrixLimits")
                .map(e -> {
                    String ref = XmlHelper.getChildValue(e, "TileMatrix");
                    TileMatrix tm = findTileMatrix(tms, ref);
                    if (tm == null) {
                        LOG.warn("Referred TileMatrix", ref, "does not appear in specified TileMatrixSet", tms.getId());
                        return null;
                    }
                    int minTileRow = Integer.parseInt(XmlHelper.getChildValue(e, "MinTileRow"));
                    int maxTileRow = Integer.parseInt(XmlHelper.getChildValue(e, "MaxTileRow"));
                    int minTileCol = Integer.parseInt(XmlHelper.getChildValue(e, "MinTileCol"));
                    int maxTileCol = Integer.parseInt(XmlHelper.getChildValue(e, "MaxTileCol"));
                    return new TileMatrixLimits(tm, minTileRow, maxTileRow, minTileCol, maxTileCol);
                })
                .filter(limit -> limit != null)
                .collect(Collectors.toList());
        return limits;
    }

    private static TileMatrix findTileMatrix(TileMatrixSet tms, String ref) {
        Map<String, TileMatrix> tileMatrices = tms.getTileMatrixMap();

        TileMatrix tm = tileMatrices.get(ref);
        if (tm != null) {
            return tm;
        }

        // ref might be prefixed with the id of TileMatrixSet and ':', atleast MapCache does this
        // We need to split from the last ':' as the ref might be something like "EPSG:3067:0"
        int i = ref.lastIndexOf(':');
        if (i > 0 && ref.startsWith(tms.getId())) {
            ref = ref.substring(i + 1);
            tm = tileMatrices.get(ref);
            if (tm != null) {
                return tm;
            }
        }
        return null;
    }

    /**
     * Get tile matrix set id of current crs
     * @param links
     * @param currentCrs
     * @return
     */
    protected static String getMatrixSetId(List<TileMatrixLink> links, String currentCrs) {
        for (TileMatrixLink link : links) {
            TileMatrixSet tms = link.getTileMatrixSet();
            String tmsCrs = tms.getCrs();
            String epsg = CapabilitiesService.shortSyntaxEpsg(tmsCrs);
            // TODO: ^ was previously ProjectionHelper.shortSyntaxEpsg(tmsCrs);
            // but we could let it be the long one here if code for printing is updated
            if (currentCrs.equals(epsg)) {
                return tms.getId();
            }
        }
        return null;
    }

}

package fi.mml.map.mapwindow.service.wms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import fi.mml.capabilities.BoundingBoxDocument.BoundingBox;
import fi.mml.capabilities.KeywordDocument;
import fi.mml.capabilities.LayerDocument.Layer;
import fi.mml.capabilities.LegendURLDocument.LegendURL;
import fi.mml.capabilities.OperationType;
import fi.mml.capabilities.StyleDocument.Style;
import fi.mml.capabilities.WMSCapabilitiesDocument;
import fi.mml.capabilities.KeywordListDocument.KeywordList;

/**
 * 1.3.0 implementation of WMS
 */
public class WebMapServiceV1_3_0_Impl extends AbstractWebMapService {

    public WebMapServiceV1_3_0_Impl(String url, String data, String layerName)
            throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        this(url, data, layerName, null);
    }

    public WebMapServiceV1_3_0_Impl(String url, String data, String layerName, Set<String> allowedCRS)
            throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        super(url);
        parseXML(data, layerName, allowedCRS);
    }

    public String getVersion() {
        return "1.3.0";
    }

    private void parseXML(String data, String layerName, Set<String> allowedCRS)
            throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        try {
            WMSCapabilitiesDocument wms = WMSCapabilitiesDocument.Factory.parse(data);

            Layer layerCapabilities = wms.getWMSCapabilities().getCapability().getLayer();
            BoundingBox bbox = layerCapabilities.getBoundingBoxArray()[0];
            if(bbox != null) {
            	String crs = bbox.getCRS();
        		double maxx = bbox.getMaxx();
            	double maxy = bbox.getMaxy();
            	double minx = bbox.getMinx();
            	double miny = bbox.getMiny();
            	Coordinate topRightCoord = new Coordinate(maxx, maxy);
            	Coordinate lowerRightCoord = new Coordinate(maxx, miny);
            	Coordinate lowerLeftCoord = new Coordinate(minx, miny);
            	Coordinate topLeftCoord = new Coordinate(minx, maxy);
            	Coordinate topRightEndCoord = new Coordinate(maxx, maxy);
            	ArrayList<Coordinate> coords = new ArrayList<>();
            	coords.add(topRightCoord);
            	coords.add(lowerRightCoord);
            	coords.add(lowerLeftCoord);
            	coords.add(topLeftCoord);
            	coords.add(topRightEndCoord);
            	GeometryFactory fact = new GeometryFactory();
            	Polygon poly = fact.createPolygon(coords.toArray(new Coordinate[coords.size()]));
            	CoordinateReferenceSystem sourceCRS = CRS.decode(crs, true);
                CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326", true);
                MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
                Geometry targetGeometry = JTS.transform(poly, transform);
            	this.geom = targetGeometry.toText();
            }
            LinkedList<Layer> path = new LinkedList<>();
            boolean found = find(layerCapabilities, layerName, path, 0);
            if (!found) {
                throw new LayerNotFoundInCapabilitiesException("Could not find layer " + layerName);
            }

            this.styles = new HashMap<>();
            this.legends = new HashMap<>();
            for (Layer layer : path) {
                parseStylesAndLegends(layer, styles, legends);
            }
            this.formats = parseFormats(wms);
            this.CRSs = parseCRSs(layerCapabilities.getCRSArray(), allowedCRS);

            Layer layer = path.getLast();
            this.queryable = layer.getQueryable();
            this.time = Arrays.stream(layer.getDimensionArray())
                    .filter(dimension -> "time".equals(dimension.getName()))
                    .findAny()
                    .map(d -> Arrays.asList(d.getStringValue().split(",")))
                    .orElse(Collections.emptyList());
            this.keywords = parseKeywords(layer);
        } catch (Exception e) {
            throw new WebMapServiceParseException(e);
        }
    }

    /**
     * Traverse layer tree, trying to find one specific layer
     * @param layer layer to inspect
     * @param layerName name of the layer we're looking for
     * @param path holds information of the branches we are in
     * @param lvl how deep we are in the subLayers, if this gets too high we quit
     * @return false if no such layer exists, true otherwise,
     *         LinkedList<Layer> path contains the path from root to the layer
     */
    private boolean find(Layer layer, String layerName, LinkedList<Layer> path, int lvl)
            throws WebMapServiceParseException {
        if (lvl > 5) {
            throw new WebMapServiceParseException(
                    "We tried to parse layers to fifth level of recursion,"
                            + " this is too much. Cancel.");
        }
        if (layerName.equals(layer.getName())) {
            // Add current layer before returning
            path.addLast(layer);
            return true;
        }
        Layer[] subLayers = layer.getLayerArray();
        if (subLayers != null && subLayers.length > 0) {
            // Remember current layer while we check its subLayers
            path.addLast(layer);
            for (Layer subLayer : subLayers) {
                if (find(subLayer, layerName, path, lvl + 1)) {
                    return true;
                }
            }
            // None of the subLayers matched, remove current layer from the correct path
            path.removeLast();
        }
        return false;
    }

    private void parseStylesAndLegends(Layer layer,
            Map<String, String> styles,
            Map<String, String> legends) {
        Style[] stylesArray = layer.getStyleArray();
        if (stylesArray == null) {
            return;
        }
        for (Style style : stylesArray) {
            String styleName = style.getName();
            String styleTitle = style.getTitle();
            if (styleTitle == null || styleTitle.isEmpty()) {
                styleTitle = styleName;
            }
            styles.put(styleName, styleTitle);

            LegendURL[] lurl = style.getLegendURLArray();
            if (lurl == null || lurl.length == 0 || lurl[0].getOnlineResource() == null) {
                continue;
            }
            /* Online resource is in xlink namespace */
            String href = lurl[0].getOnlineResource().newCursor().getAttributeText(XLINK_HREF);
            if (href != null) {
                legends.put(styleName + LEGEND_HASHMAP_KEY_SEPARATOR + styleTitle, href);
            }
        }
    }

    private String[] parseFormats(WMSCapabilitiesDocument wms) {
        OperationType gfi = wms.getWMSCapabilities().getCapability().getRequest().getGetFeatureInfo();
        return gfi == null ? new String[0] : gfi.getFormatArray();
    }

    private String[] parseCRSs(String[] crsArray, Set<String> allowedCRS) {
        if (allowedCRS == null) {
            return crsArray;
        }
        List<String> parsed = new ArrayList<>();
        for (String crs : crsArray) {
            if (allowedCRS.contains(crs)) {
                parsed.add(crs);
            }
        }
        return parsed.toArray(new String[parsed.size()]);
    }

    private String[] parseKeywords(Layer layer) {
        KeywordList keywordList = layer.getKeywordList();
        if (keywordList == null) {
            return new String[0];
        }
        KeywordDocument.Keyword[] words = keywordList.getKeywordArray();
        if (words == null) {
            return new String[0];
        }
        String[] keywords = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            keywords[i] = words[i].getStringValue();
        }
        return keywords;
    }

}

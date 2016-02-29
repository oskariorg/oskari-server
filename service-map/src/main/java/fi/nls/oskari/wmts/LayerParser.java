package fi.nls.oskari.wmts;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.XmlHelper;
import fi.nls.oskari.wms.WMSStyle;
import fi.nls.oskari.wmts.domain.ResourceUrl;
import fi.nls.oskari.wmts.domain.TileMatrixLimits;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
import org.apache.axiom.om.OMElement;

import java.util.*;

/**
 * NOTE!!! Initial scripblings for WMTS capabilities parsing. Not used anywhere and might change without notice!!
 */
public class LayerParser {

    private Logger log = LogFactory.getLogger(LayerParser.class);


    public static WMTSCapabilitiesLayer parse(final OMElement capabilitiesLayer) throws Exception {
        if(capabilitiesLayer == null) {
            return null;
        }
        WMTSCapabilitiesLayer result = new WMTSCapabilitiesLayer();
        result.setId(XmlHelper.getChildValue(capabilitiesLayer, "Identifier"));
        result.setTitle(XmlHelper.getChildValue(capabilitiesLayer, "Title"));

        // setup styles
        Iterator<OMElement> styleIterator = capabilitiesLayer.getChildrenWithLocalName("Style");
        while(styleIterator.hasNext()) {
            OMElement styleElem = styleIterator.next();

            WMSStyle style = new WMSStyle();
            final String id = XmlHelper.getChildValue(styleElem, "Identifier");
            style.setTitle(id);
            style.setName(id);
            if(ConversionHelper.getBoolean(XmlHelper.getAttributeValue(styleElem, "isDefault"), false)) {
                result.setDefaultStyle(id);
            }
            result.addStyle(style);
        }

        // setup formats
        Iterator<OMElement> formatIterator = capabilitiesLayer.getChildrenWithLocalName("Format");
        while(formatIterator.hasNext()) {
            OMElement elem = formatIterator.next();
            result.addFormat(elem.getText());
        }
        // setup infoformats
        Iterator<OMElement> infoFormatIterator = capabilitiesLayer.getChildrenWithLocalName("InfoFormat");
        while(infoFormatIterator.hasNext()) {
            OMElement elem = infoFormatIterator.next();
            result.addInfoFormat(elem.getText());
        }

        // setup resource urls
/*
        <ResourceURL format="image/jpeg" resourceType="tile"
        template="http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/ortokuva_vaaravari/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.jpg" />
*/
        Iterator<OMElement> urlIterator = capabilitiesLayer.getChildrenWithLocalName("ResourceURL");
        while(urlIterator.hasNext()) {
            OMElement elem = urlIterator.next();
            Map<String, String> attrs = XmlHelper.getAttributesAsMap(elem);
            ResourceUrl url = new ResourceUrl();
            url.setFormat(attrs.get("format"));
            url.setType(attrs.get("resourceType"));
            url.setTemplate(attrs.get("template"));
            result.addResourceUrl(url);
        }

        // setup matrix link and limits
        Map<String, Set<TileMatrixLimits>> links = parseLinks(capabilitiesLayer);
        result.setLinks(links);
        return result;
    }

    private static Map<String, Set<TileMatrixLimits>> parseLinks(final OMElement capabilitiesLayer) throws Exception {
        Iterator<OMElement> matrixLinkIterator = capabilitiesLayer.getChildrenWithLocalName("TileMatrixSetLink");
        Map<String, Set<TileMatrixLimits>> links = new HashMap<String, Set<TileMatrixLimits>>();
        while(matrixLinkIterator.hasNext()) {
            OMElement elem = matrixLinkIterator.next();
            final String name = XmlHelper.getChildValue(elem, "TileMatrixSet");
            final OMElement setLimitsElem = XmlHelper.getChild(elem, "TileMatrixSetLimits");
            final Set<TileMatrixLimits> limits = parseLimits(setLimitsElem);
            links.put(name, limits);
        }
        return links;
    }
    private static Set<TileMatrixLimits> parseLimits(final OMElement elem) throws Exception {
        final Set<TileMatrixLimits> limits = new HashSet<TileMatrixLimits>();
        if(elem == null) {
            return limits;
        }
        Iterator<OMElement> limitsIterator = elem.getChildrenWithLocalName("TileMatrixLimits");
        while(limitsIterator.hasNext()) {
            final OMElement limitElem = limitsIterator.next();
            final TileMatrixLimits limit = new TileMatrixLimits();
            limit.setTileMatrix(XmlHelper.getChildValue(limitElem, "TileMatrix"));
            limit.setMinTileRow(ConversionHelper.getInt(XmlHelper.getChildValue(limitElem, "MinTileRow"), limit.getMinTileRow()));
            limit.setMaxTileRow(ConversionHelper.getInt(XmlHelper.getChildValue(limitElem, "MaxTileRow"), limit.getMaxTileRow()));
            limit.setMinTileCol(ConversionHelper.getInt(XmlHelper.getChildValue(limitElem, "MinTileCol"), limit.getMinTileCol()));
            limit.setMaxTileCol(ConversionHelper.getInt(XmlHelper.getChildValue(limitElem, "MaxTileCol"), limit.getMaxTileCol()));
            limits.add(limit);
        }
        return limits;
    }
}

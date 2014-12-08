package fi.nls.oskari.printout.input.layers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import fi.nls.oskari.printout.config.ConfigValue;
import fi.nls.oskari.printout.input.layers.LayerDefinition.Style;
import fi.nls.oskari.printout.input.maplink.MapLink;
import fi.nls.oskari.printout.input.maplink.MapLinkParser;
import fi.nls.oskari.printout.output.map.MetricScaleResolutionUtils;

/**
 * This class parses layer json and sets up layerdefinition objects. Uses
 * maplink parser to sort out maplink oriented arguments picked up from JSON.
 * 
 */
public class MapLayerJSONParser {

    private Properties props;

    MapLinkParser mapLinkParser;

    final WKTReader wktReader = new WKTReader();

    public MapLayerJSONParser(Properties props) {
        this.props = props;

        String scaleResolverId = ConfigValue.SCALE_RESOLVER.getConfigProperty(
                props, "m_ol212");

        Integer zoomOffset = ConfigValue.MAPLINK_ZOOM_OFFSET.getConfigProperty(
                props, 0);

        this.mapLinkParser = new MapLinkParser(
                MetricScaleResolutionUtils.getScaleResolver(scaleResolverId),
                zoomOffset);
    }

    @SuppressWarnings("unchecked")
    private void adjustData(LayerDefinition layerDefinition,
            Map<String, ?> layerObj) {

        Object data = layerObj.get(".data");

        if (data instanceof FeatureCollection) {
            layerDefinition
                    .setData((FeatureCollection<SimpleFeatureType, SimpleFeature>) data);
        }

    }

    private void adjustGeom(LayerDefinition layerDefinition,
            Map<String, ?> layerObj) throws ParseException {

        String geomAsWkt = (String) layerObj.get("geom");

        if (geomAsWkt == null) {
            return;
        }

        layerDefinition.setGeom(wktReader.read(geomAsWkt));

    }

    public void adjustLayerWmsNameAndUrl(LayerDefinition layerDefinition)
            throws IOException {
        String layerURL = null;
        String layersParam = null;
        String type = layerDefinition.getLayerType();
        if (isMyPlacesType(layerDefinition)) {

            if (layerDefinition.getWmsurl() != null
                    && layerDefinition.getWmsname() != null) {
                layerURL = ConfigValue.LAYER_URLTEMPLATE_MYPLACES
                        .getConfigProperty(props).split("\\?")[0]
                        + "?"
                        + layerDefinition.getWmsurl().split("\\?")[1];
                layersParam = URLEncoder.encode(
                        ConfigValue.LAYER_URLTEMPLATE_MYPLACES_LAYERS
                                .getConfigProperty(props), "UTF-8");
            } else {

                String[] splitter = layerDefinition.getLayerid().split("_");
                layerURL = String.format(ConfigValue.LAYER_URLTEMPLATE_MYPLACES
                        .getConfigProperty(props), splitter[1]);
                layersParam = URLEncoder.encode(
                        ConfigValue.LAYER_URLTEMPLATE_MYPLACES_LAYERS
                                .getConfigProperty(props), "UTF-8");
            }
        } else if ("wmslayer".equals(type)) {
            layerURL = layerDefinition.getWmsurl();
            layersParam = URLEncoder.encode(layerDefinition.getWmsname(),
                    "UTF-8");
        } else if ("base".equals(type) || "groupMap".equals(type)) {
            layerURL = layerDefinition.getWmsurl();
            if (layerDefinition.getWmsname() != null) {
                layersParam = URLEncoder.encode(layerDefinition.getWmsname(),
                        "UTF-8");
            }

        } else if ("wfslayer".equals(type)) {
            layerURL = String.format(ConfigValue.LAYER_URLTEMPLATE_WFSLAYER
                    .getConfigProperty(props), layerDefinition.getLayerid());
            layersParam = URLEncoder.encode(layerDefinition.getLayerid(),
                    "UTF-8");
        } else if ("statslayer".equals(type)) {
            layerURL = String.format(ConfigValue.LAYER_URLTEMPLATE_STATSLAYER
                    .getConfigProperty(props), layerDefinition.getLayerid());
            layersParam = URLEncoder.encode(layerDefinition.getLayerid(),
                    "UTF-8");
        } else if ("geojson".equals(type)) {

        } else if ("wmtslayer".equals(type)) {
            layerURL = layerDefinition.getWmsurl();
            layersParam = URLEncoder.encode(layerDefinition.getWmsname(),
                    "UTF-8");
            if (layerURL != null) {
                if (layerURL.indexOf('?') != -1) {
                    layerURL = layerURL.substring(0, layerURL.indexOf('?'));
                }
            }
        } else {
            throw new IOException("Unknown layertype " + type);
        }

        layerDefinition.setWmsname(layersParam);
        layerDefinition.setWmsurl(layerURL);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void adjustStyles(LayerDefinition layerDefinition,
            Map<String, ?> layerObj) {
        Object styles = layerObj.get("styles");

        if (styles == null) {
            /* no style */
        } else if (styles instanceof Map) {
            /* one style */
            Map<String, ?> styledef = (Map<String, ?>) styles;
            String name = (String) styledef.get("name");
            if (name == null) {
                return;
            }
            String title = (String) styledef.get("title");
            String legend = (String) styledef.get("legend");
            String sld = (String) styledef.get("styledLayerDescriptor");

            Style style = new LayerDefinition.Style();
            style.setName(name);
            style.setTitle(title);
            style.setLegend(legend);
            style.setSld(sld);
            style.setStyleMap((Map<String, ?>) styledef.get("styleMap"));

            layerDefinition.getStyles().put(name, style);

        } else if (styles instanceof List) {
            /*
             * N styles
             */
            List<Map<String, ?>> stylelist = (List) styles;
            for (Map<String, ?> styledef : stylelist) {

                String name = (String) styledef.get("name");
                if (name == null) {
                    name = (String) styledef.get("identifier");
                }
                String title = (String) styledef.get("title");
                String legend = (String) styledef.get("legend");
                String sld = (String) styledef.get("styledLayerDescriptor");

                Style style = new LayerDefinition.Style();
                style.setName(name);
                style.setTitle(title);
                style.setLegend(legend);
                style.setSld(sld);
                style.setStyleMap((Map<String, ?>) styledef.get("styleMap"));

                layerDefinition.getStyles().put(name, style);
            }
        }

    }

    String fixWmsUrl(String url) {
        if (url == null) {
            return url;
        }

        if (url.indexOf(",http") != -1) {
            return url.substring(0, url.indexOf(",http"));
        }
        return url;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map<String, ?> getJSONFromInputStream(InputStream inp)
            throws IOException {
        flexjson.JSONDeserializer deserializer = new flexjson.JSONDeserializer();

        InputStreamReader reader = new InputStreamReader(inp);

        Map<String, ?> obj = (Map<String, ?>) deserializer.deserialize(reader);

        return obj;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map<String, ?> getJSONFromURL(URL url) throws IOException {

        flexjson.JSONDeserializer deserializer = new flexjson.JSONDeserializer();

        InputStream inp = url.openStream();
        try {
            InputStreamReader reader = new InputStreamReader(inp);

            Map<String, ?> obj = (Map<String, ?>) deserializer
                    .deserialize(reader);

            return obj;
        } finally {
            inp.close();
        }

    }

    public MapLinkParser getMapLinkParser() {
        return mapLinkParser;
    }

    public boolean isMyPlacesType(LayerDefinition layerDefinition) {
        return layerDefinition.getLayerType() != null
                && "myplaces".equals(layerDefinition.getLayerType())
                || layerDefinition.getLayerid().indexOf("myplaces") != -1;
    }

    public Map<String, LayerDefinition> parse(URL url) throws IOException,
            ParseException {

        Map<String, ?> obj = getJSONFromURL(url);

        return parseLayersFromJSON(obj);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map<String, LayerDefinition> parseLayersFromJSON(
            Map<String, ?> obj) throws IOException, ParseException {
        Map<String, LayerDefinition> layerDefs = new HashMap<String, LayerDefinition>();

        final Set<String> cacheExclusions = new HashSet<String>();
        final String exclusions = ConfigValue.LAYER_CACHE_EXCLUDE
                .getConfigProperty(props);
        if (exclusions != null) {
            String[] parts = exclusions.split(",");
            if (parts != null) {
                for (String p : parts) {
                    cacheExclusions.add(p);
                }
            }
        }

        final List<Map<String, ?>> layerList = (List<Map<String, ?>>) obj
                .get("layers");

        for (Map<String, ?> layerObj : layerList) {

            Map<String, ?> optionsMap = (Map<String, ?>) layerObj
                    .get("options");
            Map<String, ?> paramsMap = (Map<String, ?>) layerObj.get("params");

            String type = (String) layerObj.get("type");

            if (!("wfslayer".equals(type) || "wmslayer".equals(type)
                    || "statslayer".equals(type) || "base".equals(type)
                    || "groupMap".equals(type) || "myplaces".equals(type)
                    || "geojson".equals(type) || "wmtslayer".equals(type))) {
                continue;
            }

            Object xolayerid = layerObj.get("id");
            String layerid = xolayerid.toString();
            String wmsname = (String) layerObj.get("wmsName");
            Number minScale = (Number) layerObj.get("minScale");
            Number maxScale = (Number) layerObj.get("maxScale");
            String wmsurl = (String) layerObj.get("wmsUrl");

            String url = (String) layerObj.get("url");
            if (url != null) {
                wmsurl = url;
            }

            String wmsversion = (String) layerObj.get("version");

            List<Map<String, ?>> tiles = (List<Map<String, ?>>) layerObj
                    .get("tiles");

            LayerDefinition layerDefinition = new LayerDefinition();

            if (maxScale != null) {
                layerDefinition.setMaxScale(maxScale.doubleValue());
            }
            if (minScale != null) {
                layerDefinition.setMinScale(minScale.doubleValue());
            }
            layerDefinition.setWmsname(wmsname);
            layerDefinition.setWmsurl(fixWmsUrl(wmsurl));
            if (wmsversion != null) {
                layerDefinition.setWmsVersion(wmsversion);
            } else {
                layerDefinition.setWmsVersion("1.1.0");
            }

            layerDefinition.setLayerid(layerid);
            layerDefinition.setLayerType(type);
            layerDefinition.setTiles(tiles);

            if (isMyPlacesType(layerDefinition)) {
                layerDefinition.setCacheable(false);
            } else if (tiles != null) {
                layerDefinition.setCacheable(false);
            } else {
                if (cacheExclusions.contains(layerid)) {
                    layerDefinition.setCacheable(false);
                }

            }

            layerDefinition.setTileMatrixSetId((String) layerObj
                    .get("tileMatrixSetId"));

            adjustLayerWmsNameAndUrl(layerDefinition);

            if (isMyPlacesType(layerDefinition)) {
                layerDefinition
                        .setCredentials(ConfigValue.LAYERDEFINITION_CREDENTIALS_MYPLACES
                                .getConfigProperty(props));
            }

            List<Map<String, ?>> subLayer = (List) layerObj.get("subLayer");
            if (subLayer != null) {
                for (Map<String, ?> subLayerObj : subLayer) {
                    Map<String, ?> sloptionsMap = (Map<String, ?>) layerObj
                            .get("options");
                    Map<String, ?> slparamsMap = (Map<String, ?>) layerObj
                            .get("params");

                    Object slolayerid = subLayerObj.get("id");
                    String sllayerid = slolayerid.toString();
                    String slwmsname = (String) subLayerObj.get("wmsName");
                    String slwmsversion = (String) subLayerObj.get("version");
                    Number slminScale = (Number) subLayerObj.get("minScale");
                    Number slmaxScale = (Number) subLayerObj.get("maxScale");
                    String slwmsurl = (String) subLayerObj.get("wmsUrl");
                    String slurl = (String) subLayerObj.get("url");
                    if (slurl != null && !slurl.isEmpty()) {
                        slwmsurl = slurl;
                    }
                    String sltype = (String) subLayerObj.get("type");

                    LayerDefinition subLayerDefinition = new LayerDefinition();

                    if (slmaxScale != null) {
                        subLayerDefinition
                                .setMaxScale(slmaxScale.doubleValue());
                    }
                    if (slminScale != null) {
                        subLayerDefinition
                                .setMinScale(slminScale.doubleValue());
                    }
                    subLayerDefinition.setWmsname(slwmsname);
                    subLayerDefinition.setWmsurl(fixWmsUrl(slwmsurl));
                    if (slwmsversion != null) {
                        subLayerDefinition.setWmsVersion(slwmsversion);
                    } else {
                        subLayerDefinition.setWmsVersion("1.1.0");
                    }
                    subLayerDefinition.setLayerid(sllayerid);
                    subLayerDefinition.setLayerType(sltype != null ? sltype
                            : type);
                    subLayerDefinition.setFormat(layerDefinition.getFormat());
                    adjustLayerWmsNameAndUrl(subLayerDefinition);
                    adjustStyles(subLayerDefinition, subLayerObj);
                    adjustGeom(subLayerDefinition, subLayerObj);
                    adjustData(subLayerDefinition, subLayerObj);
                    /* format selection by default or by spec for wmts */
                    subLayerDefinition.setFormat("image/png");
                    if (sloptionsMap != null
                            && sloptionsMap.get("format") != null) {
                        subLayerDefinition.setFormat((String) sloptionsMap
                                .get("format"));
                    }

                    if ("wmtslayer".equals(type)) {
                        adjustTileMatrixSetInfo(subLayerDefinition,
                                (Map<String, ?>) subLayerObj
                                        .get("tileMatrixSetData"));
                    }
                    layerDefinition.getSubLayers().add(subLayerDefinition);
                }

            }

            adjustStyles(layerDefinition, layerObj);
            adjustGeom(layerDefinition, layerObj);

            adjustData(layerDefinition, layerObj);

            /* format selection by default or by spec for wmts */
            layerDefinition.setFormat("image/png");

            if (optionsMap != null && optionsMap.get("format") != null) {
                layerDefinition.setFormat((String) optionsMap.get("format"));
            }

            if (paramsMap != null && paramsMap.get("singleTile") != null) {
                layerDefinition.setSingleTile("true".equals(paramsMap
                        .get("singleTile")));
            }

            if ("wmtslayer".equals(type)) {
                adjustTileMatrixSetInfo(layerDefinition,
                        (Map<String, ?>) layerObj.get("tileMatrixSetData"));
            }

            layerDefs.put(layerDefinition.getLayerid(), layerDefinition);

        }

        return layerDefs;
    }

    Object jsonPathHelper(String... parts) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private void adjustTileMatrixSetInfo(LayerDefinition layerDefinition,
            Map<String, ?> layerObj) {

        if (layerObj == null) {
            return;
        }

        Map<String, ?> contents = (Map<String, ?>) layerObj.get("contents");
        if (contents == null) {
            return;
        }

        /* find REST url information for selected layer */
        /* tileMatrixSetData may contain other layer specs as well */

        Object rawLayers = contents.get("layers");
        if (rawLayers == null) {
            return;
        }

        Map<String, ?> wmtsLayerSpec = null;
        if (rawLayers instanceof List) {
            String layerName = layerDefinition.getWmsname();
            List<Map<String, ?>> wmtsLayers = (List<Map<String, ?>>) rawLayers;

            for (Map<String, ?> wmtsLayerListEl : wmtsLayers) {
                if (!layerName.equals(wmtsLayerListEl.get("identifier"))) {
                    continue;
                }

                wmtsLayerSpec = wmtsLayerListEl;
                break;
            }

        } else {
            wmtsLayerSpec = (Map<String, ?>) rawLayers;
        }

        if (wmtsLayerSpec == null) {
            return;
        }

        /* */
        {
            Map<String, ?> resourceUrl = (Map<String, ?>) wmtsLayerSpec
                    .get("resourceUrl");
            if (resourceUrl == null) {
                return;
            }
            Map<String, ?> resourceTileInfo = (Map<String, ?>) wmtsLayerSpec
                    .get("tile");
            if (resourceTileInfo != null) {

                String format = (String) resourceTileInfo.get("format");
                if (format != null) {
                    layerDefinition.setFormat(format);
                    String template = (String) resourceTileInfo.get("template");
                    if (template != null) {
                        layerDefinition.setUrlTemplate(format, template);
                    }
                }
            }
        }

        List<Map<String, ?>> resourceUrls = (List<Map<String, ?>>) wmtsLayerSpec
                .get("resourceUrls");
        if (resourceUrls == null) {
            return;
        }

        for (Map<String, ?> resourceTileInfo : resourceUrls) {
            String format = (String) resourceTileInfo.get("format");
            if (format != null) {
                if (layerDefinition.getFormat() == null) {
                    layerDefinition.setFormat(format);
                }
                String template = (String) resourceTileInfo.get("template");
                if (template != null) {
                    layerDefinition.setUrlTemplate(format, template);
                }
            }
        }

        /*
         * "resourceUrl": { "tile": { "format": "image/png", "template":
         * "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/taustakartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png"
         * , "resourceType": "tile" } }, "resourceUrls": [ { "format":
         * "image/png", "template":
         * "http://karttamoottori.maanmittauslaitos.fi/maasto/wmts/1.0.0/taustakartta/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png"
         * , "resourceType": "tile" } ]
         */

    }

    public MapLink parseMapLinkJSON(InputStream inp, GeometryFactory gf,
            double[] resolutions) throws IOException, ParseException {

        MapLink mapLink = null;
        Map<String, ?> obj = getJSONFromInputStream(inp);

        Map<String, LayerDefinition> layerDefs = parseLayersFromJSON(obj);
        MapLayerJSON layerJson = new MapLayerJSON(layerDefs);

        mapLink = mapLinkParser.parseJSONMapLink(obj, layerJson, gf,
                resolutions);

        return mapLink;
    }

    public MapLink parseMapLinkJSON(Map<String, ?> obj, GeometryFactory gf,
            double[] resolutions) throws IOException, ParseException {

        MapLink mapLink = null;

        Map<String, LayerDefinition> layerDefs = parseLayersFromJSON(obj);
        MapLayerJSON layerJson = new MapLayerJSON(layerDefs);

        mapLink = mapLinkParser.parseJSONMapLink(obj, layerJson, gf,
                resolutions);

        return mapLink;
    }

    public MapLink parseMapLinkJSON(URL url, GeometryFactory gf,
            double[] resolutions) throws IOException, ParseException {
        MapLink mapLink = null;
        Map<String, ?> obj = getJSONFromURL(url);

        Map<String, LayerDefinition> layerDefs = parseLayersFromJSON(obj);
        MapLayerJSON layerJson = new MapLayerJSON(layerDefs);

        mapLink = mapLinkParser.parseJSONMapLink(obj, layerJson, gf,
                resolutions);

        return mapLink;

    }

}

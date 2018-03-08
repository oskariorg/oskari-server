package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.view.modifier.bundle.MapfullHandler;
import fi.nls.oskari.control.view.modifier.param.CoordinateParamHandler;
import fi.nls.oskari.control.view.modifier.param.LayersParamHandler;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layout.OskariLayoutWorker;
import fi.nls.oskari.service.ProxyService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.util.*;

import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

@OskariActionRoute("GetPreview")
public class GetPreviewHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetPreviewHandler.class);

    private static final String PARM_COORD = "coord";
    private static final String PARM_ZOOMLEVEL = "zoomLevel";
    private static final String PARM_MAPLAYERS = "mapLayers";
    private static final String PARM_FORMAT = "format";
    private static final String PARM_GEOJSON = "geojson";
    private static final String PARM_TILES = "tiles";
    private static final String PARM_TABLE = "tabledata";
    private static final String PARM_SAVE = "saveFile";
    private static final String PARM_TABLETEMPLATE = "tableTemplate";

    private static final String KEY_LAYERS = "layers";
    private static final String KEY_MAPLINK = "maplink";
    private static final String KEY_PRINTOUT = "printout";
    private static final String KEY_LAYER_ID = "id";
    // Tiles json param keys
    private static final String KEY_TILES = "tiles";
    private static final String KEY_BBOX = "bbox";
    private static final String KEY_IS_STATS = "action_route=GetStatsTile";
    private static final String KEY_URL = "url";
    private static final String VALUE_STATSLAYER = "statslayer";

    // geojson param keys
    private static final String KEY_GJS_ID = "id";
    private static final String KEY_GJS_STYLES = "styles";
    private static final String KEY_GJS_STYLE = "style";
    private static final String KEY_GJS_STYLEMAP = "styleMap";
    private static final String KEY_GJS_FILLOPACITY = "fillOpacity";
    private static final String KEY_GJS_OPACITY = "opacity";
    private static final String KEY_GJS_NAME = "name";
    private static final String KEY_GJS_DEFAULT = "default";
    private static final String VALUE_CONTENTTYPE = "application/json; charset=UTF-8";

    // Print info param keys
    private static final String KEY_FEATURES = "features";
    private static final String KEY_GEOMETRY = "geometry";
    private static final String KEY_COORDINATES = "coordinates";
    private static final String KEY_PROPERTIES = "properties";
    private static final String KEY_TARGETHEIGHT = "targetHeight";
    private static final String KEY_TARGETWIDTH = "targetWidth";

    // layer config generation needs these (only for debugging)
    private static final long PRINT_VIEW = 2;
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final List<String> ACCEPTED_FORMATS = new ArrayList<String>();
    private static final List<String> EXTRA_PARAMS = new ArrayList<String>();
    private static String printBaseURL;
    private static String printBaseGeojsURL;
    private static String printSaveFilePath;

    public void init() {
        printBaseURL = PropertyUtil.get("service.print.maplink.json.url");
        printBaseGeojsURL = PropertyUtil.get("service.print.maplink.geojson.url");
        printSaveFilePath = PropertyUtil.get("service.print.saveFilePath");

        ACCEPTED_FORMATS.add("application/pdf");
        ACCEPTED_FORMATS.add("image/png");

        EXTRA_PARAMS.add(PARM_GEOJSON);
        EXTRA_PARAMS.add(PARM_TILES);
        EXTRA_PARAMS.add(PARM_TABLE);
        EXTRA_PARAMS.add(PARM_TABLETEMPLATE);
        EXTRA_PARAMS.add(PARM_SAVE);



        ProxyService.init();
    }

    public void handleAction(ActionParameters params) throws ActionException {

        final HttpServletResponse response = params.getResponse();
        final HttpServletRequest httpRequest = params.getRequest();
        // default print format is application/pdf
        final String pformat = params.getHttpParam(PARM_FORMAT,
                "application/pdf");
        final JSONObject jsonprint = getPrintJSON(params);
        String file_save = params.getHttpParam(PARM_SAVE, "");

        final HttpURLConnection con = getConnection(pformat, !params.getHttpParam(PARM_GEOJSON, "").isEmpty());
        for (Enumeration<String> e = httpRequest.getHeaderNames(); e
                .hasMoreElements();) {
            final String key = e.nextElement();
            final String value = httpRequest.getHeader(key);
            con.setRequestProperty(key, value);
        }
        try {
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            HttpURLConnection.setFollowRedirects(false);
            con.setUseCaches(false);
            con.setRequestProperty(HEADER_CONTENT_TYPE, "application/json");
            con.connect();
            if (log.isDebugEnabled()) {
                log.debug(jsonprint.toString(2));
            }

            IOHelper.writeToConnection(con, jsonprint.toString());

            final byte[] presponse = IOHelper.readBytes(con.getInputStream());
            // Save plot for future use
            if (!file_save.isEmpty()) savePdfPng(presponse, file_save, pformat);

            final String contentType = con.getHeaderField(HEADER_CONTENT_TYPE);
            response.addHeader(HEADER_CONTENT_TYPE, contentType);

            response.getOutputStream().write(presponse, 0, presponse.length);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            throw new ActionException("Couldn't proxy request to print server",
                    e);
        } finally {
            con.disconnect();
        }
    }

    private HttpURLConnection getConnection(String pformat, boolean geojsCase)
            throws ActionException {
        try {
            if (geojsCase)
                // Test service for print + geojs print
                return IOHelper.getConnection(getPrintGeojsUrl(pformat));
            else
                // Normal print without geojs data
                return IOHelper.getConnection(getPrintUrl(pformat));

        } catch (Exception e) {
            throw new ActionException(
                    "Couldnt get connection to print service", e);
        }
    }

    private JSONObject getPrintJSON(ActionParameters params)
            throws ActionException {
        final JSONObject jsonprint = new JSONObject();
        try {
            final HttpServletRequest httpRequest = params.getRequest();

            // copy parameters
            JSONObject jsparamdata = new JSONObject();
            for (Object key : httpRequest.getParameterMap().keySet()) {
                String keyStr = (String) key;
                // not geojson, tiles, tabledata param ..
                if (!EXTRA_PARAMS.contains(keyStr)) {
                    jsparamdata.put(keyStr, params.getHttpParam(keyStr));
                }
            }
            jsonprint.put(KEY_MAPLINK, jsparamdata);

            // Table data
            final JSONObject jsTableData = this.populateTableData(params);
            if(jsTableData != null) jsonprint.put(KEY_PRINTOUT, jsTableData);


            // construct state
            final JSONObject jsonstatedata = new JSONObject();

            final String[] coords = CoordinateParamHandler.parseParam(params
                    .getHttpParam(PARM_COORD));
            if (coords.length == 2) {
                try {
                    final double east = ConversionHelper.getDouble(coords[0],
                            -1);
                    final double north = ConversionHelper.getDouble(coords[1],
                            -1);
                    if (east == -1 || north == -1) {
                        throw new IllegalArgumentException(
                                "Coordinates not set: "
                                        + params.getHttpParam(PARM_COORD));
                    }
                    jsonstatedata.put(ViewModifier.KEY_EAST, east);
                    jsonstatedata.put(ViewModifier.KEY_NORTH, north);
                } catch (Exception ex) {
                    throw new ActionException(
                            "Could not set coordinates from URL param.", ex);
                }
            }
            jsonstatedata.put(ViewModifier.KEY_ZOOM, ConversionHelper.getInt(params
                    .getHttpParam(PARM_ZOOMLEVEL), 10));

            final String[] layers = params.getHttpParam(PARM_MAPLAYERS).split(
                    ",");
            final JSONArray configLayers = new JSONArray();
            final JSONArray selectedlayers = new JSONArray();

            // final String referer =
            // RequestHelper.getDomainFromReferer(params.getHttpHeader("Referer"));
            for (String layerString : layers) {
                final String[] layerProps = layerString.split(" ");
                final JSONObject layer = LayersParamHandler.getLayerJson(
                        layerProps, "paikkatietoikkuna.fi");
                if (layer != null) {
                    selectedlayers.put(layer);
                    configLayers.put(layer);
                }
            }

            // GeoJson graphics layers to selected layers
            final String geojs64 = params.getHttpParam(PARM_GEOJSON, "");
             
            JSONArray geojs = null;
            if (!geojs64.isEmpty()) {
                // decoding geojson
                byte[] decoded = Base64.decodeBase64(geojs64.getBytes());

                geojs = new JSONArray(new String(decoded));

                JSONArray jslays = getGeojsonLayers(geojs);
                for (int i = 0; i < jslays.length(); i++) {
                    selectedlayers.put(jslays.getJSONObject(i));
                }

            }
            jsonstatedata.put(ViewModifier.KEY_SELECTEDLAYERS, selectedlayers);
            jsonprint.put(ViewModifier.KEY_STATE, jsonstatedata);

            // printservice uses direct urls to myplaces instead of servletfilter/actionroute proxy
            final boolean useDirectURLForMyplaces = true;
            // populate layer details
            final JSONArray fullLayersConfigJson = MapfullHandler
                    .getFullLayerConfig(configLayers, params.getUser(), params
                            .getLocale().getLanguage(),
                            PRINT_VIEW, ViewTypes.PRINT, Collections.EMPTY_SET, useDirectURLForMyplaces, params.getHttpParam(PARAM_SRS));

            // GeoJson graphics layers + styles
            if (geojs != null) {
                // Add geojson geometry and styles to layers section
                for (int i = 0; i < geojs.length(); i++) {
                    fullLayersConfigJson.put(geojs.getJSONObject(i));
                }
            }

            // Add tiles, (statslayer, wfs)
      //      if (fullLayersConfigJson.toString().contains(VALUE_STATSLAYER)) {
                // Tiles in params ?
                if (!params.getHttpParam(PARM_TILES, "").isEmpty()) {

                    JSONArray tiles = getTilesJSON(params);
                    addTiles2Layers(fullLayersConfigJson, tiles);

                }
        //    }

            jsonprint.put(KEY_LAYERS, fullLayersConfigJson);

        } catch (Exception e) {
            throw new ActionException("Failed to create image", e);
        }

        return jsonprint;
    }

    private JSONArray getTilesJSON(ActionParameters params)
            throws ActionException {
        JSONArray tilesjs = null;
        try {
            // GeoJson graphics layers to selected layers
            final String tiles = params.getHttpParam(PARM_TILES, "");
           
            if (!tiles.isEmpty()) {
                tilesjs = new JSONArray(tiles);
            }

            // Get print area bbox
            final String response = ProxyService.proxy("print", params);
            JSONObject printbbox = JSONHelper.createJSONObject(response);
            // Fix bbox to print size area only for statslayer
            fixBbox(printbbox, tilesjs);

        } catch (Exception e) {
            throw new ActionException("Failed to get tiles json ", e);
        }

        return tilesjs;
    }

    private String getPrintUrl(final String format) throws ActionException {
        final String lowerCaseFormat = format.toLowerCase();
        if (!ACCEPTED_FORMATS.contains(lowerCaseFormat)) {
            throw new ActionException("Unknown print type requested " + format);
        }
        return printBaseURL + "." + format.split("/")[1] + "?";
    }

    private String getPrintGeojsUrl(final String format) throws ActionException {
        final String lowerCaseFormat = format.toLowerCase();
        if (!ACCEPTED_FORMATS.contains(lowerCaseFormat)) {
            throw new ActionException("Unknown print type requested " + format);
        }
        return printBaseGeojsURL + "." + format.split("/")[1] + "?";
    }

    private JSONArray getGeojsonLayers(final JSONArray geojs)
            throws ActionException {
        JSONArray geojslayers = new JSONArray();

        try {
            // loop geojson layers and get layer name, opacity and style id for
            // print server request
            for (int i = 0; i < geojs.length(); i++) {
                JSONObject layer = geojs.getJSONObject(i);
                JSONObject geojslayer = new JSONObject();
                Double opa = 0.0;

                Iterator<?> keys = layer.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();

                    if (KEY_GJS_ID.toUpperCase().equals(key.toUpperCase())) {
                        // geojs layer id
                        geojslayer.accumulate(key, layer.get(key).toString());
                    } else if (KEY_GJS_STYLES.toUpperCase().equals(
                            key.toUpperCase())) {
                        if (layer.get(key) instanceof JSONArray) {
                            JSONArray styles = layer.getJSONArray(key);
                            if (styles.length() > 0) {
                                JSONObject style = styles.getJSONObject(0);
                                Iterator<?> skeys = style.keys();

                                while (skeys.hasNext()) {
                                    String skey = (String) skeys.next();

                                    if (KEY_GJS_NAME.toUpperCase().equals(
                                            skey.toUpperCase())) {
                                        // Geojs default style id
                                        geojslayer.accumulate(KEY_GJS_STYLE,
                                                style.get(skey).toString());
                                    } else if (KEY_GJS_STYLEMAP.toUpperCase()
                                            .equals(skey.toUpperCase())) {
                                        if (style.get(skey) instanceof JSONObject) {
                                            JSONObject stylemap = style
                                                    .getJSONObject(skey);

                                            // Get opacity out of openlayers
                                            // style property fillOpacity
                                            if (stylemap.has(KEY_GJS_DEFAULT)) {
                                                JSONObject defa = stylemap
                                                        .getJSONObject(KEY_GJS_DEFAULT);
                                                if (defa
                                                        .has(KEY_GJS_FILLOPACITY))
                                                    opa = defa
                                                            .getDouble(KEY_GJS_FILLOPACITY);
                                            }
                                        }
                                    }

                                }
                            }

                        }

                    }

                }
                if (opa > -1.0 && opa < 1.001) {
                    opa = opa * 100;
                    geojslayer.accumulate(KEY_GJS_OPACITY, Integer.toString(opa
                            .intValue())); // fillOpacity
                } else {
                    geojslayer.accumulate(KEY_GJS_OPACITY, "100"); // if not
                    // available
                }
                // front
                geojslayers.put(geojslayer);
            }

        } catch (Exception e) {
            throw new ActionException("Failed to read geojson", e);
        }
        return geojslayers;
    }

    private void fixBbox(JSONObject printbbox, JSONArray tilesjs)
            throws ActionException {

        log.debug("fixing bbox:\n", printbbox, "\n\ntilesjson:\n", tilesjs);
        try {
            if (printbbox.get(KEY_FEATURES) instanceof JSONArray) {
                JSONArray features = printbbox.getJSONArray(KEY_FEATURES);

                JSONArray coordinates = features.getJSONObject(0)
                        .getJSONObject(KEY_GEOMETRY).getJSONArray(
                                KEY_COORDINATES);
                JSONArray minxy = coordinates.getJSONArray(0).getJSONArray(0);
                JSONArray maxxy = coordinates.getJSONArray(0).getJSONArray(2);
                int targetHeight = features.getJSONObject(0).getJSONObject(
                        KEY_PROPERTIES).getInt(KEY_TARGETHEIGHT);
                int targetWidth = features.getJSONObject(0).getJSONObject(
                        KEY_PROPERTIES).getInt(KEY_TARGETWIDTH);

                // replace bbox and widt / height in Oskari tiles (statslayer)
                //Loop tiles - there are tiles for statslayers and wfs layers
                for (int ii = 0; ii < tilesjs.length(); ii++) {
                    JSONObject tile = tilesjs.getJSONObject(ii);
                    if (tile.toString().contains(KEY_IS_STATS)) {
                        Iterator<?> keys = tile.keys();
                        // There is only one key
                        if (keys.hasNext()) {
                            String key = (String) keys.next();
                            // Stats layer in one tile
                            JSONObject tile0 = tile.getJSONArray(key).getJSONObject(0);
                            List boxpoints = new ArrayList();
                            boxpoints.add(minxy.getInt(0));
                            boxpoints.add(minxy.getInt(1));
                            boxpoints.add(maxxy.getInt(0));
                            boxpoints.add(maxxy.getInt(1));

                            tile0.remove(KEY_BBOX);
                            tile0.put(KEY_BBOX, boxpoints);

                            // replace &BBOX, WIDTH and HEIGHT
                            // &BBOX=-508380,5983042,1543620,7765042&WIDTH=1539&HEIGHT=1336
                            String myurl = tile0.getString(KEY_URL);
                            String[] tem1 = myurl.split("&");
                            for (int i = 0; i < tem1.length; i++) {
                                if (tem1[i].contains("BBOX")) {
                                    tem1[i] = "BBOX=" + boxpoints.get(0).toString()
                                            + "," + boxpoints.get(1).toString() + ","
                                            + boxpoints.get(2).toString() + ","
                                            + boxpoints.get(3).toString();
                                } else if (tem1[i].contains("WIDTH")) {
                                    tem1[i] = "WIDTH=" + Integer.toString(targetWidth);
                                } else if (tem1[i].contains("HEIGHT")) {
                                    tem1[i] = "HEIGHT="
                                            + Integer.toString(targetHeight);
                                }
                            }
                            // new url
                            myurl = "";
                            for (int i = 0; i < tem1.length; i++) {
                                myurl = myurl + tem1[i] + "&";
                            }
                            myurl = myurl.substring(0, myurl.length() - 1);

                            tile0.remove(KEY_URL);
                            tile0.put(KEY_URL, myurl);

                        }
                    }
                }

            }
 
        } catch (Exception e) {
            throw new ActionException("Failed to read print info geojson", e);
        }
    }

    private void savePdfPng(byte[] presponse, String filename, String pformat)
            throws ActionException {

        try {
            String[] formats = pformat.split("/");
            String saveFilePath = printSaveFilePath + filename + "."+ formats[formats.length - 1];

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
            outputStream.write(presponse);
            outputStream.close();

        } catch (Exception e) {
            throw new ActionException("Failed to save pdf/png file", e);

        }
    }

    private void addTiles2Layers(JSONArray layers, JSONArray tilesjs)
            throws ActionException {

        try {

            // Loop Layers
            for (int i = 0; i < layers.length(); i++) {
                JSONObject layer = layers.getJSONObject(i);
                addTile2Layer(layer, tilesjs);
            }

        } catch (Exception e) {
            throw new ActionException("Failed to put tiles into layer json", e);
        }
    }

    private void addTile2Layer(JSONObject layer, JSONArray tilesjs)
            throws ActionException {

        try {
            //Loop tiles - there are tiles for statslayers and wfs layers
            for (int ii = 0; ii < tilesjs.length(); ii++) {
                JSONObject tileroot = tilesjs.getJSONObject(ii);
                Iterator<?> keys = tileroot.keys();
                // There is only one key
                if (keys.hasNext()) {
                    String layer_id = (String) keys.next();
                    if (layer.optString(KEY_LAYER_ID).equals(layer_id))
                        layer.put(KEY_TILES, tileroot.getJSONArray(layer_id));

                }

            }

        } catch (Exception e) {
            throw new ActionException("Failed to put tiles into layer json", e);
        }
    }
    private JSONObject populateTableData(ActionParameters params)
            throws ActionException {

        try {
            // Get template
            final String tableLayout = params.getHttpParam(PARM_TABLETEMPLATE, "");
            if(tableLayout.isEmpty()) return null;
            String tabledata = OskariLayoutWorker.getTableTemplate(tableLayout);
            if(tabledata == null) return null;

            JSONObject jstable = JSONHelper.createJSONObject(tabledata);
            if(jstable == null) return null;

            // Get table row data
            final String rows = params.getHttpParam(PARM_TABLE, "");
            JSONObject jsrows = JSONHelper.createJSONObject(rows);
            if(jsrows == null) return null;

            JSONObject jsprint = jstable.optJSONObject(KEY_PRINTOUT);

            // Add data rows

            return OskariLayoutWorker.fillTables(jsprint, jsrows);

            // Get table row data
            // final String rows = params.getHttpParam(PARM_TABLE, "");

        } catch (Exception e) {
            throw new ActionException("Failed to populate printout table data", e);
        }

    }

}
